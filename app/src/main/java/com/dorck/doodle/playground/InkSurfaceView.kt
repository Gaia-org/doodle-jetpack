package com.dorck.doodle.playground

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View.OnTouchListener
import com.dorck.doodle.ink.engine.render.FastGLFrontRenderer

class InkSurfaceView(context: Context, private val frontRenderer: FastGLFrontRenderer): SurfaceView(context), SurfaceHolder.Callback {

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, motionEvent ->
        if (frontRenderer.getTouchEventHandler() != null) {
            frontRenderer.getTouchEventHandler()!!.handleTouchEvent(motionEvent!!, view)
        }
        true
    }

    init {
        setOnTouchListener(mTouchListener)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        holder.addCallback(this)
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged, w: $width, h: $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed")
    }

    companion object {
        private const val TAG = "InkSurfaceView"
    }
}