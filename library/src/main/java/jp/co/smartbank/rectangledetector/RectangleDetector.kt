package jp.co.smartbank.rectangledetector

import android.graphics.Bitmap
import jp.co.smartbank.rectangledetector.dto.DetectionResult

/**
 * Detector for the four vertices of rectangles in a [Bitmap] graphic object.
 */
interface RectangleDetector {
    fun detectRectangles(bitmap: Bitmap): DetectionResult

    companion object {
        fun getInstance(detectionAccuracy: DetectionAccuracy = DetectionAccuracy.Passive): RectangleDetector {
            return RectangleDetectorImpl(detectionAccuracy)
        }
    }
}
