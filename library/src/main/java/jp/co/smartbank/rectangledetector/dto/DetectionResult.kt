package jp.co.smartbank.rectangledetector.dto

import android.util.Size

/**
 * An object containing detected [Rectangle]s.
 */
data class DetectionResult(
    val imageSize: Size,
    val rectangles: List<Rectangle> = emptyList()
)
