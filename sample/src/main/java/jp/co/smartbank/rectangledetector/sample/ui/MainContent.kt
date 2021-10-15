package jp.co.smartbank.rectangledetector.sample.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import jp.co.smartbank.rectangledetector.dto.DetectionResult

@Composable
fun MainContent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Rectangle Detector") }) },
        backgroundColor = MaterialTheme.colors.background
    ) {
        CameraPermissionRequestScreen {
            var previewSize by remember { mutableStateOf(Size.Zero) }
            var detectionResult: DetectionResult? by remember { mutableStateOf(null) }
            CameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { previewSize = it.size.toSize() },
                imageAnalyzer = RectangleDetectionImageAnalyzer {
                    detectionResult = it
                    Log.d("RectangleDetector", "Rectangles detected: $it")
                }
            )

            DetectedRectangleLayer(
                modifier = Modifier.fillMaxSize(),
                previewSize = previewSize,
                detectionResult = detectionResult
            )
        }
    }
}
