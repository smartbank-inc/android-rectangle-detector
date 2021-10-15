package jp.co.smartbank.rectangledetector.sample.ui

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture? = null,
    imageAnalyzer: ImageAnalysis.Analyzer? = null
) {
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider.value = cameraProviderFuture.get()
                    bindCameraUseCases(
                        previewView = this,
                        cameraProvider = cameraProviderFuture.get(),
                        lifecycleOwner = lifecycleOwner,
                        cameraExecutor = cameraExecutor,
                        imageCapture = imageCapture,
                        imageAnalyzer = imageAnalyzer
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        }
    )

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun bindCameraUseCases(
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    imageCapture: ImageCapture?,
    imageAnalyzer: ImageAnalysis.Analyzer?,
) {
    val width = previewView.measuredWidth
    val height = previewView.measuredHeight
    if (width == 0 || height == 0) {
        return
    }

    val previewRatio = width.toDouble() / height
    val targetResolution = arrayListOf(
        Size(1080, 1920),
        Size(1080, 1440),
        Size(1080, 1080),
        Size(1440, 1080),
        Size(1920, 1080)
    ).minByOrNull { abs(previewRatio - (it.width / it.height)) } ?: Size(1080, 1080)

    val rotation = previewView.display.rotation
    imageCapture?.targetRotation = previewView.display.rotation

    val preview = androidx.camera.core.Preview.Builder()
        .setTargetRotation(rotation)
        .setTargetResolution(targetResolution)
        .build()

    cameraProvider.unbindAll()

    try {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setTargetResolution(targetResolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalyzer?.let { imageAnalysis.setAnalyzer(cameraExecutor, it) }

        val useCases = arrayOf(preview, imageCapture, imageAnalysis).filterNotNull()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, *useCases.toTypedArray())
        preview.setSurfaceProvider(previewView.surfaceProvider)
    } catch (e: Exception) {
    }
}
