package jp.co.smartbank.rectangledetector.sample.extension

import android.annotation.SuppressLint
import android.graphics.*
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Convert [ImageProxy] which has an image in JPEG or YUV_420 (NV21) format to [Bitmap].
 *
 * https://stackoverflow.com/questions/52726002/camera2-captured-picture-conversion-from-yuv-420-888-to-nv21
 */
internal fun ImageProxy.toBitmap(): Bitmap = when (format) {
    ImageFormat.JPEG -> toBitmapFromJpegFormatImage()
    ImageFormat.YUV_420_888 -> toBitmapFromYUVFormatImage()
    else -> throw UnsupportedOperationException("ImageProxy has unsupported image format: $format")
}

private fun ImageProxy.toBitmapFromJpegFormatImage(): Bitmap {
    assert(format == ImageFormat.JPEG)

    val planeProxy = planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap.cropped(cropRect).rotated(imageInfo.rotationDegrees)
}

@SuppressLint("UnsafeOptInUsageError")
private fun ImageProxy.toBitmapFromYUVFormatImage(): Bitmap {
    val nv21 = toNV21ByteArray()
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, cropRect.width(), cropRect.height(), null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 80, out)
    val imageBytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return bitmap.rotated(imageInfo.rotationDegrees)
}

private fun ImageProxy.toNV21ByteArray(): ByteArray {
    assert(format == ImageFormat.YUV_420_888)

    val pixelCount = cropRect.width() * cropRect.height()
    val ySize = cropRect.width() * cropRect.height()
    val uvSize = cropRect.width() * cropRect.height() / 4
    val nv21 = ByteArray(ySize + uvSize * 2)
    planes.forEachIndexed { planeIndex, plane ->
        val outputStride: Int
        var outputOffset: Int
        when (planeIndex) {
            0 -> {
                outputStride = 1
                outputOffset = 0
            }
            1 -> {
                outputStride = 2
                outputOffset = pixelCount + 1
            }
            2 -> {
                outputStride = 2
                outputOffset = pixelCount
            }
            else -> return@forEachIndexed
        }

        val planeBuffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        val planeCrop = if (planeIndex == 0) {
            cropRect
        } else {
            Rect(
                cropRect.left / 2,
                cropRect.top / 2,
                cropRect.right / 2,
                cropRect.bottom / 2
            )
        }

        val planeWidth = planeCrop.width()
        val planeHeight = planeCrop.height()
        val rowBuffer = ByteArray(plane.rowStride)

        val rowLength = if (pixelStride == 1 && outputStride == 1) {
            planeWidth
        } else {
            (planeWidth - 1) * pixelStride + 1
        }

        for (row in 0 until planeHeight) {
            planeBuffer.position(
                (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride
            )

            if (pixelStride == 1 && outputStride == 1) {
                planeBuffer.get(nv21, outputOffset, rowLength)
                outputOffset += rowLength
            } else {
                planeBuffer.get(rowBuffer, 0, rowLength)
                for (col in 0 until planeWidth) {
                    nv21[outputOffset] = rowBuffer[col * pixelStride]
                    outputOffset += outputStride
                }
            }
        }
    }

    return nv21
}

private fun Bitmap.cropped(cropRect: Rect) = if (width != cropRect.width() || height != cropRect.height()) {
    Bitmap.createBitmap(this, cropRect.left, cropRect.top, cropRect.width(), cropRect.height())
} else {
    this
}

private fun Bitmap.rotated(rotationDegrees: Int) = if (rotationDegrees != 0) {
    Bitmap.createBitmap(
        this, 0, 0, width, height,
        Matrix().apply { postRotate(rotationDegrees.toFloat()) }, true
    )
} else {
    this
}
