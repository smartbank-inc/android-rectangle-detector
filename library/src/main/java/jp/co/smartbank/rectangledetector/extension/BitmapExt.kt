package jp.co.smartbank.rectangledetector.extension

import android.graphics.Bitmap
import kotlin.math.roundToInt

internal fun Bitmap.scaled(ratio: Float, filter: Boolean) = if (ratio != 1f) {
    Bitmap.createScaledBitmap(this, (width * ratio).roundToInt(), (height * ratio).roundToInt(), filter)
} else {
    this
}
