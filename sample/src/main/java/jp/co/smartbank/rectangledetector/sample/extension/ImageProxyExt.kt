package jp.co.smartbank.rectangledetector.sample.extension

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException

/**
 * Convert [ImageProxy] which has an image in JPEG or YUV (NV21) format to [Bitmap].
 *
 * https://stackoverflow.com/questions/52726002/camera2-captured-picture-conversion-from-yuv-420-888-to-nv21
 */
internal fun ImageProxy.toBitmap(): Bitmap? = when (format) {
    ImageFormat.JPEG -> toBitmapFromJpegFormatImage()
    ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888 -> toBitmapFromYUVFormatImage()
    else -> throw UnsupportedOperationException("ImageProxy has unsupported image format: $format")
}

private fun ImageProxy.toBitmapFromJpegFormatImage(): Bitmap {
    val planeProxy = planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return if (imageInfo.rotationDegrees == 0) {
        bitmap
    } else {
        rotateBitmap(bitmap, imageInfo.rotationDegrees)
    }
}

@SuppressLint("UnsafeOptInUsageError", "UnsafeExperimentalUsageError")
private fun ImageProxy.toBitmapFromYUVFormatImage(): Bitmap? {
    val nv21 = image?.toNV21ByteArray() ?: return null
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return if (imageInfo.rotationDegrees == 0) {
        bitmap
    } else {
        rotateBitmap(bitmap, imageInfo.rotationDegrees)
    }
}

private fun Image.toNV21ByteArray(): ByteArray? {
    val ySize = width * height
    val uvSize = width * height / 4
    val nv21 = ByteArray(ySize + uvSize * 2)
    val yBuffer: ByteBuffer = planes[0].buffer
    val uBuffer: ByteBuffer = planes[1].buffer
    val vBuffer: ByteBuffer = planes[2].buffer
    var rowStride = planes[0].rowStride
    assert(planes[0].pixelStride == 1)

    var position = 0
    if (rowStride == width) {
        yBuffer.get(nv21, 0, ySize)
        position += ySize
    } else {
        var yBufferPos = -rowStride.toLong()
        while (position < ySize) {
            yBufferPos += rowStride.toLong()
            yBuffer.position(yBufferPos.toInt())
            yBuffer.get(nv21, position, width)
            position += width
        }
    }

    rowStride = planes[2].rowStride
    val pixelStride = planes[2].pixelStride

    assert(rowStride == planes[1].rowStride)
    assert(pixelStride == planes[1].pixelStride)

    if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
        val savePixel: Byte = vBuffer.get(1)
        try {
            vBuffer.put(1, savePixel.inc())
            if (uBuffer.get(0) == savePixel.inc()) {
                vBuffer.put(1, savePixel)
                vBuffer.position(0)
                uBuffer.position(0)
                vBuffer.get(nv21, ySize, 1)
                uBuffer.get(nv21, ySize + 1, uBuffer.remaining())
                return nv21
            }
        } catch (ex: ReadOnlyBufferException) {
            return null
        }

        vBuffer.put(1, savePixel)
    }

    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            val vuPos = col * pixelStride + row * rowStride
            nv21[position++] = vBuffer.get(vuPos)
            nv21[position++] = uBuffer.get(vuPos)
        }
    }
    return nv21
}

private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int) = Bitmap.createBitmap(
    bitmap, 0, 0,
    bitmap.width, bitmap.height,
    Matrix().apply { postRotate(rotationDegrees.toFloat()) },
    true
)
