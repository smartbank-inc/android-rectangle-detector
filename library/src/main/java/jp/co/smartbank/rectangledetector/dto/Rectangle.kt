package jp.co.smartbank.rectangledetector.dto

import android.graphics.Point
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Rectangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomLeft: Point,
    val bottomRight: Point
) {
    private val topWidth: Float
        get() = topLeft.distance(topRight)
    private val bottomWidth: Float
        get() = bottomLeft.distance(bottomRight)
    private val leftHeight: Float
        get() = topLeft.distance(bottomLeft)
    private val rightHeight: Float
        get() = topRight.distance(bottomRight)

    internal val points: List<Point>
        get() = listOf(topLeft, topRight, bottomRight, bottomLeft)

    internal val horizontalDistortionRatio: Float
        get() = if (leftHeight > rightHeight) {
            leftHeight / rightHeight
        } else {
            rightHeight / leftHeight
        }

    internal val verticalDistortionRatio: Float
        get() = if (topWidth > bottomWidth) {
            topWidth / bottomWidth
        } else {
            bottomWidth / topWidth
        }

    internal val circumferenceLength: Float
        get() = topWidth + bottomWidth + leftHeight + rightHeight

    internal fun scaled(ratio: Float) = Rectangle(
        topLeft = Point(
            (topLeft.x * ratio).roundToInt(),
            (topLeft.y * ratio).roundToInt()
        ),
        topRight = Point(
            (topRight.x * ratio).roundToInt(),
            (topRight.y * ratio).roundToInt()
        ),
        bottomLeft = Point(
            (bottomLeft.x * ratio).roundToInt(),
            (bottomLeft.y * ratio).roundToInt()
        ),
        bottomRight = Point(
            (bottomRight.x * ratio).roundToInt(),
            (bottomRight.y * ratio).roundToInt()
        )
    )

    internal fun isApproximated(other: Rectangle, distanceTolerance: Float): Boolean {
        return topLeft.distance(other.topLeft) <= distanceTolerance
                && topRight.distance(other.topRight) <= distanceTolerance
                && bottomLeft.distance(other.bottomLeft) <= distanceTolerance
                && bottomRight.distance(other.bottomRight) <= distanceTolerance
    }

    private fun Point.distance(other: Point): Float {
        val diffX = abs(other.x - x).toFloat()
        val diffY = abs(other.y - y).toFloat()
        return sqrt((diffX * diffX) + (diffY * diffY))
    }

    companion object {
        fun from(points: List<Point>): Rectangle {
            require(points.size == 4)

            val sortedPoints = points.sortedBy { it.y }
            val (topLeft, topRight) = if (sortedPoints[0].x < sortedPoints[1].x) {
                Pair(sortedPoints[0], sortedPoints[1])
            } else {
                Pair(sortedPoints[1], sortedPoints[0])
            }
            val (bottomLeft, bottomRight) = if (sortedPoints[2].x < sortedPoints[3].x) {
                Pair(sortedPoints[2], sortedPoints[3])
            } else {
                Pair(sortedPoints[3], sortedPoints[2])
            }

            return Rectangle(topLeft, topRight, bottomLeft, bottomRight)
        }
    }
}
