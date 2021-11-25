package jp.co.smartbank.rectangledetector.strategy

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * An implementation of [ContourDetectionStrategy] using canny edge detection.
 */
internal class CannyAlgorithmStrategy(private val level: Level) : ContourDetectionStrategy() {
    override fun detectContours(originalImageMat: Mat): List<MatOfPoint> {
        val edgeMat = markEdges(originalImageMat)
        val edgeClosedMat = closeEdges(edgeMat)
        return findContours(edgeClosedMat, allowanceRatioToArcLength = 0.05)
    }

    private fun markEdges(mat: Mat): Mat {
        val result = Mat()
        if (level.preBlurSize > 0) {
            Imgproc.medianBlur(mat, mat, level.preBlurSize)
        }
        Imgproc.Canny(mat, result, level.cannyThreshold1, level.cannyThreshold2)
        return result
    }

    private fun closeEdges(mat: Mat): Mat {
        val result = Mat()
        Imgproc.morphologyEx(
            mat, result, Imgproc.MORPH_CLOSE,
            Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(level.closingSize, level.closingSize))
        )
        return result
    }

    enum class Level(
        val preBlurSize: Int,
        val cannyThreshold1: Double,
        val cannyThreshold2: Double,
        val closingSize: Double
    ) {
        /**
         * A normal threshold level.
         */
        Normal(
            preBlurSize = 1,
            cannyThreshold1 = 100.0,
            cannyThreshold2 = 250.0,
            closingSize = 8.0
        ),

        /**
         * Strict thresholds level to avoid being affected by miscellaneous backgrounds, etc.
         */
        Strict(
            preBlurSize = 0,
            cannyThreshold1 = 250.0,
            cannyThreshold2 = 600.0,
            closingSize = 8.0
        )
    }
}
