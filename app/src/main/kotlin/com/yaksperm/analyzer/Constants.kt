package com.yaksperm.analyzer

/**
 * App-wide constants — empirically validated for yak semen CASA.
 * Do NOT modify calibration values without field validation.
 */
object Constants {
    // ── Calibration ──────────────────────────────────────────────────────────
    const val PIXEL_TO_UM = 0.603f         // µm per pixel — empirical FOV calibration (10× obj)
    const val BIOLOGICAL_FPS = 30.0f       // Always 30.0 regardless of what MediaMetadataRetriever reports

    // ── Model ─────────────────────────────────────────────────────────────────
    const val MODEL_INPUT_SIZE = 640        // YOLO input resolution (square)
    const val MODEL_FILE_NAME = "yolo11n_obb.tflite"
    const val CONFIDENCE_THRESHOLD = 0.35f
    const val IOU_THRESHOLD = 0.45f

    // ── Tracker ───────────────────────────────────────────────────────────────
    const val MIN_TRACK_LENGTH = 5         // Minimum frames to include a track in CASA
    const val MAX_TRACK_AGE = 8            // Frames before a track is deleted if unmatched
    const val MAX_MATCH_DISTANCE = 40f     // Pixels — Hungarian matching gate

    // ── CASA Kinematic Thresholds (Hamilton IVOS-II) ──────────────────────────
    const val PROGRESSIVE_VAP_THRESHOLD = 40f  // µm/s — VAP required for progressive classification
    const val PROGRESSIVE_STR_THRESHOLD = 60f  // % — STR required for progressive classification
    const val IMMOTILE_VCL_THRESHOLD = 5f       // µm/s — VCL below this → immotile

    // ── Reference Ranges (yak-specific) ──────────────────────────────────────
    const val REF_VCL_MIN = 40f
    const val REF_VAP_MIN = 25f
    const val REF_VSL_MIN = 20f
    const val REF_LIN_MIN = 30f
    const val REF_STR_MIN = 60f
    const val REF_WOB_MIN = 50f

    // ── UKF Parameters ───────────────────────────────────────────────────────
    const val UKF_ALPHA = 0.1
    const val UKF_BETA = 2.0
    const val UKF_KAPPA = 0.0
    const val UKF_STATE_DIM = 6             // [x, y, vx, vy, ax, ay]
    const val UKF_MEAS_DIM = 2              // [x, y] observed

    // ── PDF ───────────────────────────────────────────────────────────────────
    const val PDF_PAGE_WIDTH = 595          // A4 at 72 dpi
    const val PDF_PAGE_HEIGHT = 842
    const val APP_VERSION = "1.0.0"
    const val BUILD_DATE = "2026"
}
