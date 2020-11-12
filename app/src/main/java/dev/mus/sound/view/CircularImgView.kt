package dev.mus.sound.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircularImgView(ctx: Context, attributeSet: AttributeSet) :
    AppCompatImageView(ctx, attributeSet) {
    private val path = Path()
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        val radius = (this.height / 2).toFloat()
        rect.set(0f, 0f, this.width.toFloat(), this.height.toFloat())
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}