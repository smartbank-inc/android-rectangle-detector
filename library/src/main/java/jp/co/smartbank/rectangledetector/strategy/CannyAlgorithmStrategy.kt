package jp.co.smartbank.rectangledetector.strategy

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

/**
 * An implementation of [ContourDetectionStrategy] using canny edge detection.
 */
internal class CannyAlgorithmStrategy: ContourDetectionStrategy() {
    override fun detectContours(originalImageMat: Mat): List<MatOfPoint> {
        val edgeMat = markEdges(originalImageMat)
        val edgeClosedMat = closeEdges(edgeMat)
        return findContours(edgeClosedMat, allowanceRatioToArcLength = 0.04)
    }

    private fun markEdges(mat: Mat): Mat {
        val result = Mat()
        Imgproc.blur(mat, mat, org.opencv.core.Size(3.0, 3.0))
        Imgproc.Canny(mat, result, 250.0, 100.0, 3, true)
        return result
    }

    private fun closeEdges(mat: Mat): Mat {
        val result = Mat()
        Imgproc.morphologyEx(
            mat, result, Imgproc.MORPH_CLOSE,
            Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, org.opencv.core.Size(5.0, 5.0))
        )
        return result
    }
}
