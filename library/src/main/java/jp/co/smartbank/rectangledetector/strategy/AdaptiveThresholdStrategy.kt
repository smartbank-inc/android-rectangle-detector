package jp.co.smartbank.rectangledetector.strategy

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

/**
 * An implementation of [ContourDetectionStrategy] thresholding images.
 */
internal class AdaptiveThresholdStrategy : ContourDetectionStrategy() {
    override fun detectContours(originalImageMat: Mat): List<MatOfPoint> {
        val grayScaleMat = convertToGrayScale(originalImageMat)
        val thresholdingMat = thresholdImage(grayScaleMat)
        val noiseReducedMat = reduceNoises(thresholdingMat)
        return findContours(noiseReducedMat, allowanceRatioToArcLength = 0.02)
    }

    private fun convertToGrayScale(mat: Mat): Mat {
        val result = Mat()
        Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY)
        return result
    }

    private fun thresholdImage(mat: Mat): Mat {
        val result = Mat()
        Imgproc.adaptiveThreshold(
            mat, result, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY, 21, 3.0
        )
        return result
    }

    private fun reduceNoises(mat: Mat): Mat {
        val result = Mat()
        Imgproc.medianBlur(mat, result, 9)
        return result
    }
}
