package com.dorck.doodle.ink.engine.render

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import android.view.SurfaceView
import androidx.annotation.WorkerThread
import androidx.graphics.lowlatency.BufferInfo
import androidx.graphics.lowlatency.GLFrontBufferedRenderer
import androidx.graphics.opengl.egl.EGLManager
import androidx.graphics.surface.SurfaceControlCompat
import androidx.input.motionprediction.MotionEventPredictor
import com.dorck.doodle.ink.engine.data.BrushData
import com.dorck.doodle.ink.engine.data.DrawCurve
import com.dorck.doodle.ink.engine.data.DrawPoint
import com.dorck.doodle.ink.engine.data.DrawSegment
import com.dorck.doodle.ink.engine.model.InkDrawingModel
import com.dorck.doodle.ink.engine.touch.DrawInkTouchEventHandler

class FastGLFrontRenderer : GLFrontBufferedRenderer.Callback<DrawSegment> {
    private val mvpMatrix = FloatArray(16)
    private val projection = FloatArray(16)
    private var mFrontBufferRenderer: GLFrontBufferedRenderer<DrawSegment>? = null
    private var mMotionEventPredictor: MotionEventPredictor? = null
    private var mDrawTouchEventHandler: DrawInkTouchEventHandler? = null
    private var inkRenderer: InkRenderer = InkRenderer()
    @Volatile
    private var isClearState = false

    @WorkerThread // GLThread
    private fun obtainRenderer(): InkRenderer {
//        Log.d(TAG, "obtainRenderer, thread: ${Thread.currentThread().name} => ${Thread.currentThread().id}")
        return if (inkRenderer.isInitialized) {
            inkRenderer
        } else {
            inkRenderer
                .apply {
                    initialize()
                }
        }
    }

    override fun onDrawFrontBufferedLayer(
        eglManager: EGLManager,
        bufferInfo: BufferInfo,
        transform: FloatArray,
        param: DrawSegment
    ) {
        Log.i(TAG, "onDrawFrontBufferedLayer, buffer info: ${bufferInfo.width}, ${bufferInfo.height}")
        val bufferWidth = bufferInfo.width
        val bufferHeight = bufferInfo.height
        GLES30.glViewport(0, 0, bufferWidth, bufferHeight)
        // Map Android coordinates to GL coordinates
        Matrix.orthoM(
            mvpMatrix,
            0,
            0f,
            bufferWidth.toFloat(),
            0f,
            bufferHeight.toFloat(),
            -1f,
            1f
        )

        Matrix.multiplyMM(projection, 0, mvpMatrix, 0, transform, 0)
        // Render part of points from one touch behavior.
        Log.d(TAG, "onDrawFrontBufferedLayer, draw points size: ${param.points.size}")
        obtainRenderer().apply {
            drawPoints(param.points, projection)
        }
    }

    override fun onDrawDoubleBufferedLayer(
        eglManager: EGLManager,
        bufferInfo: BufferInfo,
        transform: FloatArray,
        params: Collection<DrawSegment>
    ) {
        Log.i(TAG, "onDrawDoubleBufferedLayer, buffer info: ${bufferInfo.width}, ${bufferInfo.height}")
        val bufferWidth = bufferInfo.width
        val bufferHeight = bufferInfo.height
        // define the size of the rectangle for rendering
        GLES30.glViewport(0, 0, bufferWidth, bufferHeight)
        // Computes the ModelViewProjection Matrix
        Matrix.orthoM(
            mvpMatrix,
            0,
            0f,
            bufferWidth.toFloat(),
            0f,
            bufferHeight.toFloat(),
            -1f,
            1f
        )
        // perform matrix multiplication to transform the Android data to OpenGL reference
        Matrix.multiplyMM(projection, 0, mvpMatrix, 0, transform, 0)

        // Clear the screen with black
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        // Construct full curve from points
        val curvePoints = mutableListOf<DrawPoint>()
        for (curveSegment in params) {
            curvePoints.addAll(curveSegment.points)
        }
        if (curvePoints.isEmpty()) {
            return
        }
        val curve = DrawCurve(curvePoints, time = System.currentTimeMillis())
        InkDrawingModel.insertCurve(curve)
        Log.d(TAG, "onDrawDoubleBufferedLayer, points size in curve: ${curvePoints.size}")
        // TODO 优化单次绘制数据，实现局部绘制并且能够保存历史数据（只绘制新增区域的内容?）
        InkDrawingModel.getAllCurves().forEach {
            Log.d(TAG, "onDrawDoubleBufferedLayer, draw curve: ${it.id}")
            obtainRenderer().drawCurve(it, projection)
        }
    }

    override fun onDoubleBufferedLayerRenderComplete(
        frontBufferedLayerSurfaceControl: SurfaceControlCompat,
        transaction: SurfaceControlCompat.Transaction
    ) {
        super.onDoubleBufferedLayerRenderComplete(frontBufferedLayerSurfaceControl, transaction)
        Log.d(TAG, "onDoubleBufferedLayerRenderComplete")
    }

    fun attach(surfaceView: SurfaceView) {
        mFrontBufferRenderer = GLFrontBufferedRenderer(surfaceView, this)
        mMotionEventPredictor = MotionEventPredictor.newInstance(surfaceView)
        mDrawTouchEventHandler = DrawInkTouchEventHandler(mFrontBufferRenderer!!, mMotionEventPredictor!!)
    }

    fun detach() {
        mFrontBufferRenderer?.release(true) {
            obtainRenderer().onDestroy()
        }
    }

    fun clearCanvas() {
        isClearState = true
//        mFrontBufferRenderer?.execute() {
//            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
//            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
//            mFrontBufferRenderer?.clear()
//        }
        InkDrawingModel.clearAllCurves()
        mFrontBufferRenderer?.commit()
    }

    fun getTouchEventHandler(): DrawInkTouchEventHandler? {
        return mDrawTouchEventHandler
    }

    fun applyBrushConfig(brushData: BrushData) {
        inkRenderer.applyBrushConfig(brushData)
    }

    fun getBrushRenderer() = inkRenderer

    companion object {
        const val TAG = "FastGlFrontRenderer"
    }
}