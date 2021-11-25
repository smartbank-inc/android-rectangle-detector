package jp.co.smartbank.rectangledetector

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import jp.co.smartbank.rectangledetector.dto.DetectionResult
import jp.co.smartbank.rectangledetector.dto.Rectangle
import jp.co.smartbank.rectangledetector.extension.scaled
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint

internal class RectangleDetectorImpl(detectionAccuracy: DetectionAccuracy) : RectangleDetector {
    private val strategy = detectionAccuracy.buildContourStrategy()

    init {
        System.loadLibrary("opencv_java4")
    }

    override fun detectRectangles(bitmap: Bitmap): DetectionResult {
        // Use a scaled Bitmap image to reduce execution speed.
        val scaleRatio = min(1f, MAX_PROCESSING_IMAGE_SIZE.toFloat() / max(bitmap.width, bitmap.height))
        val scaledBitmap = bitmap.scaled(scaleRatio, true)
        val rectangles = detectRectanglesInternal(scaledBitmap)
        return DetectionResult(
            imageSize = Size(bitmap.width, bitmap.height),
            rectangles = rectangles.map { it.scaled(1 / scaleRatio) }
        )
    }

    private fun detectRectanglesInternal(bitmap: Bitmap): List<Rectangle> {
        val mat = Mat().also { Utils.bitmapToMat(bitmap, it) }
        val contours = strategy.detectContours(mat)

        // Filter out heavily distorted rectangles.
        val rectangles = contourToRectangles(contours)
            .filter { it.isValidForDetection(bitmap.width, bitmap.height) }

        // Filter out Rectangles approximated to other.
        val distanceTolerance = max(bitmap.width, bitmap.height) * 0.02f
        return rectangles.fold(emptyList()) { result, rectangle ->
            val approximatedRectangle = result.firstOrNull { it.isApproximated(rectangle, distanceTolerance) }
            if (approximatedRectangle != null) {
                val largerRectangle = listOf(rectangle, approximatedRectangle)
                    .maxByOrNull { it.circumferenceLength } ?: approximatedRectangle
                result - approximatedRectangle + largerRectangle
            } else {
                result + rectangle
            }
        }
    }

    private fun contourToRectangles(contour: List<MatOfPoint>): List<Rectangle> = contour.map {
        val points = it.toList().map { point ->
            Point(point.x.roundToInt(), point.y.roundToInt())
        }
        Rectangle.from(points)
    }

    private fun Rectangle.isValidForDetection(imageWidth: Int, imageHeight: Int): Boolean {
        val isValidDistortionRatio = horizontalDistortionRatio < MAX_RECTANGLE_DISTORTION_RATIO
                && verticalDistortionRatio < MAX_RECTANGLE_DISTORTION_RATIO
        val undetectableEdgeAreaWidth = (imageWidth * UNDETECTABLE_EDGE_AREA_RATIO).toInt()
        val undetectableEdgeAreaHeight = (imageHeight * UNDETECTABLE_EDGE_AREA_RATIO).toInt()
        val detectableArea = Rect(
            undetectableEdgeAreaWidth,
            undetectableEdgeAreaHeight,
            imageWidth - undetectableEdgeAreaWidth,
            imageHeight - undetectableEdgeAreaHeight
        )
        val isValidPosition = points.all { detectableArea.contains(it.x, it.y) }
        return isValidDistortionRatio && isValidPosition
    }

    companion object {
        private const val MAX_PROCESSING_IMAGE_SIZE = 480
        private const val MAX_RECTANGLE_DISTORTION_RATIO = 1.5f
        private const val UNDETECTABLE_EDGE_AREA_RATIO = 0.01f
    }
}
