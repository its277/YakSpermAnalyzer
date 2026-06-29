package com.yaksperm.analyzer.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.domain.model.CasaResult
import com.yaksperm.analyzer.domain.model.MotilityGrade
import com.yaksperm.analyzer.domain.model.interpretation
import com.yaksperm.analyzer.domain.model.label
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportGenerator @Inject constructor(private val context: Context) {

    // Page: A4 at 72 dpi
    private val pageW = Constants.PDF_PAGE_WIDTH.toFloat()
    private val pageH = Constants.PDF_PAGE_HEIGHT.toFloat()
    private val margin = 40f

    // ── Colors (PDF uses ARGB ints) ───────────────────────────────────────────
    private val colorBg       = Color.parseColor("#07111F")
    private val colorPrimary  = Color.parseColor("#00C2D4")
    private val colorGreen    = Color.parseColor("#22D98A")
    private val colorAmber    = Color.parseColor("#F5A623")
    private val colorRed      = Color.parseColor("#FF4757")
    private val colorTextMain = Color.parseColor("#D8E8F0")
    private val colorTextDim  = Color.parseColor("#5A7A8A")
    private val colorBorder   = Color.parseColor("#162B3D")
    private val colorCard     = Color.parseColor("#0D1F33")
    private val colorWhite    = Color.WHITE

    fun generate(result: CasaResult, institutionName: String = "Research Institute"): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            Constants.PDF_PAGE_WIDTH, Constants.PDF_PAGE_HEIGHT, 1
        ).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Dark background
        canvas.drawColor(colorBg)

        var y = drawHeader(canvas, result, institutionName)
        y = drawSeparator(canvas, y)
        y = drawSampleInfo(canvas, result, y)
        y = drawSeparator(canvas, y)
        y = drawKinematicTable(canvas, result, y)
        y = drawSeparator(canvas, y)
        y = drawMotilitySection(canvas, result, y)
        y = drawSeparator(canvas, y)
        y = drawGradeSection(canvas, result, y)
        drawFooter(canvas, institutionName)

        document.finishPage(page)

        val fileName = "YakSperm_${result.sampleId}_${result.timestamp}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Section Drawers
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawHeader(canvas: Canvas, result: CasaResult, institution: String): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Institution name
        paint.color = colorTextDim
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(institution.uppercase(), margin, 35f, paint)

        // App title
        paint.color = colorPrimary
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("YakSperm Analyzer", margin, 60f, paint)

        // Subtitle
        paint.color = colorTextMain
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Computer-Assisted Sperm Analysis Report", margin, 78f, paint)

        // Date / time on the right
        val dateStr = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
            .format(Date(result.timestamp))
        paint.color = colorTextDim
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(dateStr, pageW - margin, 60f, paint)

        paint.textAlign = Paint.Align.LEFT
        return 90f
    }

    private fun drawSeparator(canvas: Canvas, y: Float): Float {
        val paint = Paint()
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawLine(margin, y + 5f, pageW - margin, y + 5f, paint)
        return y + 18f
    }

    private fun drawSampleInfo(canvas: Canvas, result: CasaResult, startY: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = startY

        drawSectionLabel(canvas, "SAMPLE INFORMATION", y)
        y += 18f

        val pairs = listOf(
            "Sample ID"     to result.sampleId,
            "Technician"    to result.technicianName,
            "Duration"      to "%.1f s".format(result.durationSeconds),
            "Frames"        to result.framesProcessed.toString()
        )

        pairs.chunked(2).forEach { row ->
            var x = margin
            row.forEach { (label, value) ->
                paint.textSize = 8f
                paint.color = colorTextDim
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(label, x, y, paint)

                paint.textSize = 11f
                paint.color = colorTextMain
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(value.ifBlank { "—" }, x, y + 14f, paint)

                x += (pageW - 2 * margin) / 2f
            }
            y += 32f
        }
        return y
    }

    private fun drawKinematicTable(canvas: Canvas, result: CasaResult, startY: Float): Float {
        var y = startY
        drawSectionLabel(canvas, "KINEMATIC PARAMETERS", y)
        y += 18f

        // Table header
        val colW = (pageW - 2 * margin) / 4f
        val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f
            color = colorPrimary
            typeface = Typeface.DEFAULT_BOLD
        }
        val headers = listOf("PARAMETER", "VALUE", "UNIT", "REFERENCE")
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, margin + i * colW, y, hPaint)
        }
        y += 4f
        drawSeparator(canvas, y)
        y += 6f

        data class Row(val param: String, val full: String, val value: Float, val unit: String, val ref: String)
        val rows = listOf(
            Row("VCL", "Curvilinear Velocity",   result.vclMean, "µm/s", "> ${Constants.REF_VCL_MIN.toInt()}"),
            Row("VAP", "Average Path Velocity",  result.vapMean, "µm/s", "> ${Constants.REF_VAP_MIN.toInt()}"),
            Row("VSL", "Straight-Line Velocity", result.vslMean, "µm/s", "> ${Constants.REF_VSL_MIN.toInt()}"),
            Row("LIN", "Linearity",              result.linMean, "%",    "> ${Constants.REF_LIN_MIN.toInt()}"),
            Row("STR", "Straightness",           result.strMean, "%",    "> ${Constants.REF_STR_MIN.toInt()}"),
            Row("WOB", "Wobble",                 result.wobMean, "%",    "> ${Constants.REF_WOB_MIN.toInt()}")
        )

        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rows.forEach { row ->
            val isLow = isLow(row.param, row.value)
            val valueColor = if (isLow) colorRed else colorGreen

            // Parameter abbreviation
            cellPaint.textSize = 10f
            cellPaint.color = colorPrimary
            cellPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(row.param, margin, y, cellPaint)

            // Value
            cellPaint.color = valueColor
            canvas.drawText("%.1f".format(row.value), margin + colW, y, cellPaint)

            // Unit
            cellPaint.color = colorTextDim
            cellPaint.textSize = 9f
            cellPaint.typeface = Typeface.DEFAULT
            canvas.drawText(row.unit, margin + 2 * colW, y, cellPaint)

            // Reference
            cellPaint.color = colorTextDim
            canvas.drawText(row.ref, margin + 3 * colW, y, cellPaint)

            y += 18f
        }
        return y
    }

    private fun drawMotilitySection(canvas: Canvas, result: CasaResult, startY: Float): Float {
        var y = startY
        drawSectionLabel(canvas, "MOTILITY BREAKDOWN", y)
        y += 18f

        val bars = listOf(
            Triple("Progressive",     result.progressiveMotilityPct,    colorGreen),
            Triple("Non-Progressive", result.nonProgressiveMotilityPct, colorPrimary),
            Triple("Immotile",        100f - result.totalMotilityPct,   colorTextDim)
        )

        val barW = pageW - 2 * margin
        val barH = 10f

        bars.forEach { (label, pct, color) ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = colorCard
            canvas.drawRoundRect(margin, y, margin + barW, y + barH, 4f, 4f, paint)

            val filled = (barW * (pct / 100f)).coerceAtLeast(0f)
            paint.color = color
            if (filled > 0f) canvas.drawRoundRect(margin, y, margin + filled, y + barH, 4f, 4f, paint)

            paint.color = colorTextMain
            paint.textSize = 9f
            canvas.drawText("$label  ${pct.toInt()}%", margin, y + barH + 12f, paint)
            y += barH + 22f
        }

        // Count summary
        y += 4f
        val summary = "Total Detected: ${result.totalDetected}   |   " +
                      "Tracked: ${result.totalTracked}   |   " +
                      "Progressive: ${result.progressiveMotile}   |   " +
                      "Non-Prog: ${result.nonProgressiveMotile}   |   " +
                      "Immotile: ${result.immotile}"
        val sp = Paint(Paint.ANTI_ALIAS_FLAG)
        sp.textSize = 8f; sp.color = colorTextDim
        canvas.drawText(summary, margin, y, sp)
        return y + 14f
    }

    private fun drawGradeSection(canvas: Canvas, result: CasaResult, startY: Float): Float {
        var y = startY
        drawSectionLabel(canvas, "OVERALL GRADE", y)
        y += 18f

        val gradeColor = when (result.grade) {
            MotilityGrade.EXCELLENT -> colorGreen
            MotilityGrade.GOOD      -> colorPrimary
            MotilityGrade.FAIR      -> colorAmber
            MotilityGrade.POOR      -> colorRed
        }

        val gp = Paint(Paint.ANTI_ALIAS_FLAG)
        gp.textSize = 28f
        gp.color = gradeColor
        gp.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(result.grade.label().uppercase(), margin, y + 26f, gp)

        gp.textSize = 10f
        gp.color = colorTextMain
        gp.typeface = Typeface.DEFAULT
        canvas.drawText(result.grade.interpretation(), margin, y + 44f, gp)

        return y + 60f
    }

    private fun drawFooter(canvas: Canvas, institution: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawLine(margin, pageH - 35f, pageW - margin, pageH - 35f, paint)

        paint.textSize = 7.5f
        paint.color = colorTextDim
        canvas.drawText(
            "YakSperm Analyzer v${Constants.APP_VERSION}  •  For research use only, not for clinical diagnosis.",
            margin, pageH - 22f, paint
        )
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("© ${Constants.BUILD_DATE} $institution", pageW - margin, pageH - 22f, paint)
    }

    private fun drawSectionLabel(canvas: Canvas, label: String, y: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.textSize = 8f
        p.color = colorPrimary
        p.typeface = Typeface.DEFAULT_BOLD
        p.letterSpacing = 0.15f
        canvas.drawText(label, margin, y, p)
    }

    private fun isLow(param: String, value: Float): Boolean = when (param) {
        "VCL" -> value < Constants.REF_VCL_MIN
        "VAP" -> value < Constants.REF_VAP_MIN
        "VSL" -> value < Constants.REF_VSL_MIN
        "LIN" -> value < Constants.REF_LIN_MIN
        "STR" -> value < Constants.REF_STR_MIN
        "WOB" -> value < Constants.REF_WOB_MIN
        else  -> false
    }
}
