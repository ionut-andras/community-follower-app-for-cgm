package ionut.andras.community.cgm.follower.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class BitmapConversion {
    fun createBitmapFromString(inputNumber: String): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = 100F
        paint.textAlign = Paint.Align.CENTER
        val textBounds = Rect()
        paint.getTextBounds(inputNumber, 0, inputNumber.length, textBounds)
        val bitmap = Bitmap.createBitmap(
            textBounds.width() + 10, 90,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawText(inputNumber, (textBounds.width() / 2 + 5).toFloat(), 70F, paint)
        return bitmap
    }
}