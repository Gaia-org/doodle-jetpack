package com.dorck.doodle.playground

import android.annotation.SuppressLint
import android.content.Context
import android.view.SurfaceView
import android.view.View.OnTouchListener
import com.dorck.doodle.ink.engine.render.FastGLFrontRenderer

class InkSurfaceView(context: Context, private val frontRenderer: FastGLFrontRenderer): SurfaceView(context) {

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, motionEvent ->
        if (frontRenderer.getTouchEventHandler() != null) {
            frontRenderer.getTouchEventHandler()!!.handleTouchEvent(motionEvent!!)
        }
        true
    }

    init {
        setOnTouchListener(mTouchListener)
//        setBackgroundColor(Color.WHITE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        frontRenderer.attach(this)
    }

    override fun onDetachedFromWindow() {
        frontRenderer.detach()
        super.onDetachedFromWindow()
    }
}