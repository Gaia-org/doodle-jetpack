package com.dorck.doodle.ink.engine.render

import android.opengl.GLES30
import com.dorck.doodle.ink.engine.data.BrushData
import com.dorck.doodle.ink.engine.data.BrushType
import com.dorck.doodle.ink.engine.data.DrawCurve
import com.dorck.doodle.ink.engine.data.DrawPoint
import com.dorck.doodle.ink.engine.util.OpenGLShaderUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class AbsBrushRenderer {
    open var isInitialized = false
    open var mBrushData: BrushData? = null
    open var mColors: FloatArray? = null
    open var mBrushSize: Float = 6f
    open var mAlpha: Int = 255

    abstract val mVertexShader: String
    abstract val mFragShader: String

    var mProgramId = 0
    var mVertexShaderId = 0
    var mFragShaderId = 0

    fun initialize() {
        onDestroy()
        mVertexShaderId = OpenGLShaderUtil.createShader(GLES30.GL_VERTEX_SHADER, mVertexShader)
        mFragShaderId = OpenGLShaderUtil.createShader(GLES30.GL_FRAGMENT_SHADER, mFragShader)
        mProgramId = OpenGLShaderUtil.createProgram(mVertexShaderId, mFragShaderId)
        onInitialize()
        isInitialized = true
    }

    open fun onInitialize() {

    }

    fun onDestroy() {
        if (mVertexShaderId != -1) {
            GLES30.glDeleteShader(mVertexShaderId)
            mVertexShaderId = -1
        }
        if (mFragShaderId != -1) {
            GLES30.glDeleteShader(mFragShaderId)
            mFragShaderId = -1
        }
        if (mProgramId != -1) {
            GLES30.glDeleteProgram(mProgramId)
            mProgramId = -1
        }
//        isInitialized = false
    }

    fun drawDebugLines() {

    }

    abstract fun getType(): BrushType

    open fun applyBrushConfig(config: BrushData) {
        this.mBrushData = config
        mBrushSize = config.size
        mColors = toRGBA(config.color)
        mAlpha = config.alpha
    }

    abstract fun drawPoints(points: List<DrawPoint>, matrix: FloatArray)

    abstract fun drawCurve(curve: DrawCurve, matrix: FloatArray)

    open fun toRGBA(i: Int): FloatArray {
        return floatArrayOf(
            (i shr 16 and 255) / 255.0f,
            (i shr 8 and 255) / 255.0f,
            (i and 255) / 255.0f,
            (i ushr 24) / 255.0f
        )
    }

    open fun createFloatBuffer(size: Int): FloatBuffer {
        val allocateDirect = ByteBuffer.allocateDirect(size * 4)
        allocateDirect.order(ByteOrder.nativeOrder())
        return allocateDirect.asFloatBuffer()
    }
}