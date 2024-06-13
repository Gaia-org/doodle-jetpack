package com.dorck.doodle.playground.demo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mPaint = Paint().apply {
        color = Color.parseColor("#F83567")
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
    }

    private val mOriginPaint = Paint().apply {
        color = Color.RED
        alpha = 80
    }

    private val mTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
    }

    private val mMatrix = Matrix()

    private val rectF = RectF(0f, 0f, 300f, 300f) // 裁剪区域
    private val drawRectF = RectF(100f, 100f, 200f, 200f) // 裁剪区域

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 先绘制为变换前的原始裁剪矩形
//        canvas.drawRect(rectF, mOriginPaint)
//        // 设置裁剪区域
//        canvas.clipRect(rectF)
//
//        // 设置一个缩放变换矩阵
//        mMatrix.postScale(0.5f, 0.5f, 0f, 0f)

//        canvas.save()
        // 应用矩阵变换
//        canvas.concat(mMatrix)

        canvas.drawText("人生自古谁五四，留取丹心照汗青", 0f, 200f, mTextPaint)
        // 绘制一个矩形，它将受到裁剪和变换的影响
        canvas.drawRect(rectF, mPaint)
        // 应用的matrix对drawColor不会生效
//        canvas.drawColor(Color.GREEN)
//        canvas.restore()
    }
}