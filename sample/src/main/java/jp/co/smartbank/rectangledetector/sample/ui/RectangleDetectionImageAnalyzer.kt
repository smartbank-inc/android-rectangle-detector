package jp.co.smartbank.rectangledetector.sample.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import jp.co.smartbank.rectangledetector.RectangleDetector
import jp.co.smartbank.rectangledetector.dto.DetectionResult
import jp.co.smartbank.rectangledetector.sample.extension.toBitmap

class RectangleDetectionImageAnalyzer(
    private val listener: (DetectionResult) -> Unit
) : ImageAnalysis.Analyzer {
    private val detector = RectangleDetector.getInstance()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()

        val startTime = System.currentTimeMillis()
        val rectangles = detector.detectRectangles(bitmap)
        Log.d("RectangleDetection", "The detection took ${System.currentTimeMillis() - startTime}ms.")

        listener(rectangles)
        imageProxy.close()
    }
}
