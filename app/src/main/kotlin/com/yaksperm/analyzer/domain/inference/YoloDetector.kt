package com.yaksperm.analyzer.domain.inference

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.domain.model.SpermDetection
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

/**
 * YOLO 11n-OBB TFLite inference wrapper.
 *
 * Model input:  [1, 640, 640, 3] — normalized Float32 in [0,1]
 * Model output: [1, num_classes+5, num_anchors] or [1, num_detections, 6]
 *               Each detection row: [x_center, y_center, w, h, angle, confidence]
 *               Coordinates are normalized to [0,1] in model-input space.
 *
 * Post-processing: filter by CONFIDENCE_THRESHOLD, NMS with IOU_THRESHOLD,
 *                  then scale back to original frame dimensions.
 *
 * GPU delegate is attempted first; falls back to CPU if unavailable.
 */
class YoloDetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val inputSize = Constants.MODEL_INPUT_SIZE

    init {
        tryLoadModel()
    }

    private fun tryLoadModel() {
        try {
            val model = loadModelFile()
            val options = Interpreter.Options()
            try {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate!!)
                Log.i(TAG, "YoloDetector: GPU delegate enabled")
            } catch (e: Exception) {
                Log.w(TAG, "GPU delegate unavailable, falling back to CPU: ${e.message}")
                gpuDelegate = null
            }
            options.numThreads = 4
            interpreter = Interpreter(model, options)
            Log.i(TAG, "YoloDetector: model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load YOLO model: ${e.message}")
            interpreter = null
        }
    }

    /** Returns true if the model was loaded successfully. */
    val isReady: Boolean get() = interpreter != null

    /**
     * Run inference on [bitmap] and return filtered, NMS-suppressed detections
     * with coordinates scaled to the original bitmap's dimensions.
     */
    fun detect(bitmap: Bitmap, frameIndex: Int): List<SpermDetection> {
        val interp = interpreter ?: return emptyList()

        val origW = bitmap.width.toFloat()
        val origH = bitmap.height.toFloat()

        // ── 1. Preprocess ─────────────────────────────────────────────────────
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = bitmapToBuffer(resized)
        resized.recycle()

        // ── 2. Infer ──────────────────────────────────────────────────────────
        // Dynamically determine output shape
        val outputTensorShape = interp.getOutputTensor(0).shape()
        // Expected: [1, N, 6]  where N = number of raw detections/anchors
        val numDetections = if (outputTensorShape.size >= 2) outputTensorShape[1] else 8400
        val featLen       = if (outputTensorShape.size >= 3) outputTensorShape[2] else 6

        val outputBuffer = Array(1) { Array(numDetections) { FloatArray(featLen) } }
        interp.run(inputBuffer, outputBuffer)

        // ── 3. Parse & Filter ─────────────────────────────────────────────────
        val candidates = mutableListOf<SpermDetection>()
        val rawOut = outputBuffer[0]

        for (i in 0 until numDetections) {
            // Some exporters store [xc, yc, w, h, conf, ...class...] and others
            // store transposed. Try both layouts.
            val conf: Float
            val xc: Float; val yc: Float; val w: Float; val h: Float; val angle: Float

            if (featLen >= 6) {
                // Layout: [xc, yc, w, h, angle, conf]
                xc    = rawOut[i][0]
                yc    = rawOut[i][1]
                w     = rawOut[i][2]
                h     = rawOut[i][3]
                angle = rawOut[i][4]
                conf  = rawOut[i][5]
            } else {
                continue
            }

            if (conf < Constants.CONFIDENCE_THRESHOLD) continue

            // Scale back to original dimensions
            val scaledX = xc * origW
            val scaledY = yc * origH
            val scaledW = w  * origW
            val scaledH = h  * origH

            candidates.add(
                SpermDetection(
                    x = scaledX, y = scaledY,
                    width = scaledW, height = scaledH,
                    angle = angle, confidence = conf,
                    frameIndex = frameIndex
                )
            )
        }

        // ── 4. NMS ────────────────────────────────────────────────────────────
        return nonMaxSuppression(candidates)
    }

    /** Convert ARGB bitmap to normalized FLOAT32 ByteBuffer [1, H, W, 3] */
    private fun bitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (px in pixels) {
            buffer.putFloat(((px shr 16) and 0xFF) / 255.0f)  // R
            buffer.putFloat(((px shr 8)  and 0xFF) / 255.0f)  // G
            buffer.putFloat(( px         and 0xFF) / 255.0f)  // B
        }
        buffer.rewind()
        return buffer
    }

    /** Greedy NMS on OBBs using center-distance IoU approximation. */
    private fun nonMaxSuppression(dets: List<SpermDetection>): List<SpermDetection> {
        if (dets.isEmpty()) return emptyList()
        val sorted = dets.sortedByDescending { it.confidence }.toMutableList()
        val keep   = mutableListOf<SpermDetection>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            keep.add(best)
            sorted.removeAll { iou(best, it) > Constants.IOU_THRESHOLD }
        }
        return keep
    }

    /** Approximate IoU using axis-aligned bounding boxes (fast, sufficient for CASA). */
    private fun iou(a: SpermDetection, b: SpermDetection): Float {
        val ax1 = a.x - a.width  / 2; val ax2 = a.x + a.width  / 2
        val ay1 = a.y - a.height / 2; val ay2 = a.y + a.height / 2
        val bx1 = b.x - b.width  / 2; val bx2 = b.x + b.width  / 2
        val by1 = b.y - b.height / 2; val by2 = b.y + b.height / 2

        val interW = max(0f, min(ax2, bx2) - max(ax1, bx1))
        val interH = max(0f, min(ay2, by2) - max(ay1, by1))
        val inter  = interW * interH
        if (inter == 0f) return 0f

        val areaA = a.width * a.height
        val areaB = b.width * b.height
        return inter / (areaA + areaB - inter)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFd = context.assets.openFd(Constants.MODEL_FILE_NAME)
        val stream  = FileInputStream(assetFd.fileDescriptor)
        val channel = stream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, assetFd.startOffset, assetFd.declaredLength)
    }

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
    }

    companion object {
        private const val TAG = "YoloDetector"
    }
}
