package jp.co.smartbank.rectangledetector

import android.graphics.Bitmap
import android.graphics.Point
import android.util.Size
import jp.co.smartbank.rectangledetector.dto.DetectionResult
import jp.co.smartbank.rectangledetector.dto.Rectangle
import jp.co.smartbank.rectangledetector.extension.scaled
import jp.co.smartbank.rectangledetector.strategy.CannyAlgorithmStrategy
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint

internal class RectangleDetectorImpl : RectangleDetector {
    private val strategy = CannyAlgorithmStrategy()

    init {
        System.loadLibrary("opencv_java4")
    }

    override fun detectRectangles(bitmap: Bitmap): DetectionResult {
        // Use a scaled Bitmap image to reduce execution speed.
        val scaleRatio = min(1f, MAX_PROCESSING_IMAGE_SIZE.toFloat() / max(bitmap.width, bitmap.height))
        val scaledBitmap = bitmap.scaled(scaleRatio, true)

        val mat = Mat().also { Utils.bitmapToMat(scaledBitmap, it) }
        val contours = strategy.detectContours(mat)

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
