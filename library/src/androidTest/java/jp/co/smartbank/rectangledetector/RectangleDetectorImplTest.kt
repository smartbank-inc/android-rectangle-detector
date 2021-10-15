package jp.co.smartbank.rectangledetector

import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.smartbank.rectangledetector.dto.Rectangle
import jp.co.smartbank.rectangledetector.test.R
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RectangleDetectorImplTest : TestCase() {
    private val detector = RectangleDetectorImpl()
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun testDetectRectanglesInEmptyCase() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rectangle_detector_empty)
        val result = detector.detectRectangles(bitmap)
        assertEquals(Size(bitmap.width, bitmap.height), result.imageSize)
        assertTrue(result.rectangles.isEmpty())
    }

    @Test
    fun testDetectRectanglesInSingleRectCase() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rectangle_detector_single_rect)
        val result = detector.detectRectangles(bitmap)
        assertEquals(Size(bitmap.width, bitmap.height), result.imageSize)
        assertEquals(1, result.rectangles.size)

        val expectedRectangle = Rectangle(
            topLeft = Point(100, 200),
            topRight = Point(300, 200),
            bottomLeft = Point(100, 300),
            bottomRight = Point(300, 300),
        )
        assertTrue(result.rectangles[0].isApproximated(expectedRectangle, 8f))
    }

    @Test
    fun testDetectRectanglesInMultipleRectCase() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rectangle_detector_multiple_rect)
        val result = detector.detectRectangles(bitmap)
        assertEquals(Size(bitmap.width, bitmap.height), result.imageSize)
        assertEquals(3, result.rectangles.size)

        val expectedRectangle1 = Rectangle(
            topLeft = Point(350, 50),
            topRight = Point(450, 50),
            bottomLeft = Point(350, 250),
            bottomRight = Point(450, 250)
        )
        val expectedRectangle2 = Rectangle(
            topLeft = Point(100, 300),
            topRight = Point(300, 300),
            bottomLeft = Point(100, 400),
            bottomRight = Point(300, 400)
        )
        val expectedRectangle3 = Rectangle(
            topLeft = Point(400, 450),
            topRight = Point(600, 450),
            bottomLeft = Point(400, 650),
            bottomRight = Point(600, 650)
        )
        assertTrue(result.rectangles.any { it.isApproximated(expectedRectangle1, 8f) })
        assertTrue(result.rectangles.any { it.isApproximated(expectedRectangle2, 8f) })
        assertTrue(result.rectangles.any { it.isApproximated(expectedRectangle3, 8f) })
    }

    @Test
    fun testDetectRectanglesInDistortedRectCase() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rectangle_detector_distorted_rect)
        val result = detector.detectRectangles(bitmap)
        assertEquals(Size(bitmap.width, bitmap.height), result.imageSize)
        assertEquals(1, result.rectangles.size)

        val expectedRectangle = Rectangle(
            topLeft = Point(200, 300),
            topRight = Point(400, 250),
            bottomLeft = Point(150, 450),
            bottomRight = Point(450, 400),
        )
        assertTrue(result.rectangles[0].isApproximated(expectedRectangle, 8f))
    }
}
