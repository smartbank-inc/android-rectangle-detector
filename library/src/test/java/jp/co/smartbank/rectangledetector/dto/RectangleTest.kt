package jp.co.smartbank.rectangledetector.dto

import android.graphics.Point
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RectangleTest {
    @Test
    fun testInitializeFromPoints() {
        val rectangle = Rectangle.from(
            listOf(
                Point(300, 100),
                Point(100, 200),
                Point(400, 100),
                Point(200, 150),
            )
        )
        assertEquals(Point(300, 100), rectangle.topLeft)
        assertEquals(Point(400, 100), rectangle.topRight)
        assertEquals(Point(100, 200), rectangle.bottomLeft)
        assertEquals(Point(200, 150), rectangle.bottomRight)
    }

    @Test
    fun testDistortionRatio() {
        val rectangle = Rectangle(
            topLeft = Point(100, 200),
            topRight = Point(500, 200),
            bottomLeft = Point(100, 500),
            bottomRight = Point(500, 500),
        )
        assertEquals(1f, rectangle.horizontalDistortionRatio)
        assertEquals(1f, rectangle.verticalDistortionRatio)

        val distortedRectangle = Rectangle(
            topLeft = Point(150, 150),
            topRight = Point(400, 250),
            bottomLeft = Point(100, 450),
            bottomRight = Point(550, 500),
        )
        assertEquals(1.0431851f, distortedRectangle.horizontalDistortionRatio)
        assertEquals(1.6815428f, distortedRectangle.verticalDistortionRatio)
    }

    @Test
    fun testScaled() {
        val rectangle = Rectangle(
            topLeft = Point(100, 200),
            topRight = Point(500, 200),
            bottomLeft = Point(100, 500),
            bottomRight = Point(500, 500),
        )
        val scaledRectangle = rectangle.scaled(2f)
        assertEquals(Point(200, 400), scaledRectangle.topLeft)
        assertEquals(Point(1000, 400), scaledRectangle.topRight)
        assertEquals(Point(200, 1000), scaledRectangle.bottomLeft)
        assertEquals(Point(1000, 1000), scaledRectangle.bottomRight)
    }

    @Test
    fun testAverage() {
        val rectangle = Rectangle(
            topLeft = Point(100, 200),
            topRight = Point(500, 200),
            bottomLeft = Point(100, 500),
            bottomRight = Point(500, 500),
        )
        val rectangle2 = Rectangle(
            topLeft = Point(150, 250),
            topRight = Point(550, 250),
            bottomLeft = Point(150, 550),
            bottomRight = Point(550, 550),
        )
        val averageRectangle = rectangle.average(rectangle2)
        assertEquals(Point(125, 225), averageRectangle.topLeft)
        assertEquals(Point(525, 225), averageRectangle.topRight)
        assertEquals(Point(125, 525), averageRectangle.bottomLeft)
        assertEquals(Point(525, 525), averageRectangle.bottomRight)
    }

    @Test
    fun testIsApproximated() {
        val rectangle = Rectangle(
            topLeft = Point(100, 200),
            topRight = Point(500, 200),
            bottomLeft = Point(100, 500),
            bottomRight = Point(500, 500),
        )
        val rectangle2 = Rectangle(
            topLeft = Point(150, 250),
            topRight = Point(550, 250),
            bottomLeft = Point(150, 550),
            bottomRight = Point(550, 550),
        )
        assertTrue(rectangle.isApproximated(rectangle2, 50f * sqrt(2f)))
        assertFalse(rectangle.isApproximated(rectangle2, 49f * sqrt(2f)))
    }
}
