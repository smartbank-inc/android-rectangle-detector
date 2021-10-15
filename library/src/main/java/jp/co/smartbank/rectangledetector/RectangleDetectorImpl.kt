package jp.co.smartbank.rectangledetector

import android.graphics.Bitmap
import android.graphics.Point
import android.util.Size
import jp.co.smartbank.rectangledetector.dto.DetectionResult
import jp.co.smartbank.rectangledetector.dto.Rectangle
import kotlin.math.max
import kotlin.math.roundToInt
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc

internal class RectangleDetectorImpl : RectangleDetector {
    init {
        System.loadLibrary("opencv_java4")
    }

    override fun detectRectangles(bitmap: Bitmap): DetectionResult {
        // Use a scaled Bitmap image to reduce execution speed.
        val scaleRatio = MAX_PROCESSING_IMAGE_SIZE.toFloat() / max(bitmap.width, bitmap.height)
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scaleRatio).roundToInt(),
            (bitmap.height * scaleRatio).roundToInt(),
            false
        )

        val mat = Mat().also { Utils.bitmapToMat(scaledBitmap, it) }
        val edgeMat = markEdgesWithCannyAlgorithm(mat)
        val edgeClosedMat = closeEdges(edgeMat)
        val contours = detectContours(edgeClosedMat)

        // Combine Rectangles approximated to other into one.
        val rectangles = contourToRectangles(contours).map { it.scaled(1 / scaleRatio) }
        val reducedRectangles = rectangles.fold(emptyList<Rectangle>()) { result, rectangle ->
            val approximatedRectangle = result.firstOrNull { it.isApproximated(rectangle, 8f) }
            if (approximatedRectangle != null) {
                result - approximatedRectangle + rectangle.average(approximatedRectangle)
            } else {
                result + rectangle
            }
        }

        return DetectionResult(
            imageSize = Size(bitmap.width, bitmap.height),
            rectangles = reducedRectangles
        )
    }

    private fun markEdgesWithCannyAlgorithm(mat: Mat): Mat {
        val result = Mat()
        Imgproc.blur(mat, result, org.opencv.core.Size(5.0, 5.0))
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

    private fun detectContours(mat: Mat): List<MatOfPoint> {
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
            Imgproc.approxPolyDP(pointMat, approx, 0.03 * arcLength, true)
            approx.convertTo(approx2, CvType.CV_32S)
            if (approx2.size().area() == 4.0) {
                approx2
            } else {
                null
            }
        }
    }

    private fun contourToRectangles(contour: List<MatOfPoint>): List<Rectangle> = contour.map {
        val points = it.toList().map { point ->
            Point(point.x.roundToInt(), point.y.roundToInt())
        }
        Rectangle.from(points)
    }

    companion object {
        private const val MAX_PROCESSING_IMAGE_SIZE = 480
    }
}
