package jp.co.smartbank.rectangledetector.strategy

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc

abstract class ContourDetectionStrategy {
    abstract fun detectContours(originalImageMat: Mat): List<MatOfPoint>

    protected fun findContours(mat: Mat, allowanceRatioToArcLength: Double): List<MatOfPoint> {
        val contours = arrayListOf<MatOfPoint>()
        val hierarchy = Mat.zeros(org.opencv.core.Size(5.0, 5.0), CvType.CV_8UC1)
        Imgproc.findContours(
            mat,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_NONE
        )

        return contours.mapNotNull {
            // Ignore extremely small size rectangles.
            if (Imgproc.contourArea(it) < mat.size().area() / 100) {
                return@mapNotNull null
            }

            val pointMat = MatOfPoint2f(*it.toArray())
            val approx = MatOfPoint2f()
            val approx2 = MatOfPoint()

            val arcLength = Imgproc.arcLength(pointMat, true)
            val allowanceLength = arcLength * allowanceRatioToArcLength
            Imgproc.approxPolyDP(pointMat, approx, allowanceLength, true)
            approx.convertTo(approx2, CvType.CV_32S)
            if (approx2.size().area() == 4.0) {
                approx2
            } else {
                null
            }
        }
    }
}

