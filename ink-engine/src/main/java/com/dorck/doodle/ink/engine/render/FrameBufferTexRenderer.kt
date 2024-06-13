package com.dorck.doodle.ink.engine.render

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import com.dorck.doodle.ink.engine.util.GLRenderUtil
import com.dorck.doodle.ink.engine.util.OpenGLShaderUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Draw textures from buf.
 */
class FrameBufferTexRenderer {
    private val vertexShader = """
        #version 300 es
        layout(location = 0) in vec3 aPosition;
        layout(location = 1) in vec2 aTexCoord;

        layout(location = 3) uniform mat4 uMVPMatrix;

        out vec2 vTexCoord;

        void main() {
            vec4 matrixPos = uMVPMatrix * vec4(aPosition.x, aPosition.y, 1.0, 1.0);
            gl_Position = vec4(matrixPos.x, matrixPos.y, 1.0, 1.0);
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragShader = """
        #version 300 es
        precision highp float;

        in vec2 vTexCoord;
        layout(location = 0) uniform float alpha;
        layout(location = 1) uniform int override;
        layout(location = 2) uniform sampler2D uTexture;

        out vec4 FragColor;

        void main() {
            vec4 color = texture(uTexture, vTexCoord);
            if (override == 1) {
                FragColor = color;
            } else if (override == 2) {
                FragColor = color;
            } else {
                FragColor = vec4(color.r / color.a, color.g / color.a, color.b / color.a, color.a * alpha);
            }
        }
    """.trimIndent()

    var isInitialized = false
        private set
    private var mVertexShaderId = -1
    private var mFragShaderId = -1
    private var mProgramId = -1
    private lateinit var mPosBuffer: FloatBuffer
    private lateinit var mTexCoordsBuffer: FloatBuffer
    private var mPositions = FloatArray(12)
    private var mTexCoords = FloatArray(8)

    fun initialize() {
        mVertexShaderId = OpenGLShaderUtil.createShader(GLES30.GL_VERTEX_SHADER, vertexShader)
        mFragShaderId = OpenGLShaderUtil.createShader(GLES30.GL_FRAGMENT_SHADER, fragShader)
        mProgramId = OpenGLShaderUtil.createProgram(mVertexShaderId, mFragShaderId)
        isInitialized = true
    }

    fun setUpData(positionRectF: RectF, texCoordRectF: RectF) {
        mPositions = floatArrayOf(
            positionRectF.left, positionRectF.top, 0.0f,
            positionRectF.left, positionRectF.bottom, 0.0f,
            positionRectF.right, positionRectF.top, 0.0f,
            positionRectF.right, positionRectF.bottom, 0.0f
        )
        mTexCoords = floatArrayOf(
            texCoordRectF.left, texCoordRectF.top,
            texCoordRectF.left, texCoordRectF.bottom,
            texCoordRectF.right, texCoordRectF.top,
            texCoordRectF.right, texCoordRectF.bottom
        )
        mPosBuffer = GLRenderUtil.createFloatBuffer(12)
        mPosBuffer.put(mPositions)
        mPosBuffer.position(0)
        mTexCoordsBuffer = GLRenderUtil.createFloatBuffer(8)
        mTexCoordsBuffer.put(mTexCoords)
        mTexCoordsBuffer.position(0)
    }

    @JvmOverloads
    fun drawTexture(texId: Int, matrix: FloatArray, alpha: Int = 255, blendMode: Int = BLEND_MODE_MIX) {
        GLES30.glUseProgram(mProgramId)
        GLES30.glEnable(GLES30.GL_BLEND)
        when (blendMode) {
            BLEND_MODE_OVERRIDE -> {
                GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ZERO)
                GLES30.glUniform1i(1, 1)
            }
            BLEND_MODE_OVERLAY -> {
                GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)
                GLES30.glUniform1i(1, 2)
            }
            BLEND_MODE_MIX -> {
                GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
                GLES30.glBlendFuncSeparate(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA, 1, GLES30.GL_ONE_MINUS_SRC_ALPHA)
                GLES30.glUniform1i(1, 0)
            }
            else -> {
            }
        }
        GLES30.glUniform1f(0, alpha / 255.0f)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
        GLES30.glUniform1i(2, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, mPosBuffer)
        GLES30.glUniformMatrix4fv(3, 1, false, matrix, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexCoordsBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mPosBuffer.capacity() / 3)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    @JvmOverloads
    fun blendingTexture(texId: Int, matrix: FloatArray, alpha: Int = 255, blendMode: Int = BLEND_MODE_MIX, renderBlock: () -> Unit) {
        drawTexture(texId, matrix, alpha, blendMode)
        renderBlock.invoke()
    }

    fun overridePixelsWithLatest(baseTex: Int, srcTex: Int, targetRect: Rect, renderBlock: (() -> Unit)) {
        Log.d(TAG, "overridePixelsWithLatest, base: $baseTex, src: $srcTex, rect: $targetRect")
        GLES20.glBindFramebuffer(
            GLES30.GL_FRAMEBUFFER,
            baseTex
        )
        GLES30.glClearColor(1.0f, 0.0f, 0.0f, 0.66f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        renderBlock.invoke()
        GLES30.glFinish()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(
            GLES30.GL_TEXTURE_2D,
            srcTex
        )
        GLES30.glCopyTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, targetRect.left, targetRect.top, 0, 0,  targetRect.width(), targetRect.height())
        GLES30.glBindFramebuffer(
            GLES30.GL_FRAMEBUFFER,
            srcTex
        )
    }

    private fun extractBitmapFromDrawRect(rect: Rect): Bitmap {
        val byteBuffer = ByteBuffer.allocate(rect.width() * rect.height() * Int.SIZE_BYTES)
        GLES30.glReadPixels(0, 0, rect.width(), rect.height(), GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        return bitmap
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
    }

    private fun createFloatBuffer(floatArray: FloatArray): FloatBuffer = ByteBuffer
        .allocateDirect(floatArray.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(floatArray)
            position(0)
        }

    companion object {
        private val TAG = FrameBufferTexRenderer::class.java.simpleName

        const val BLEND_MODE_OVERRIDE = 1
        const val BLEND_MODE_OVERLAY = 2
        const val BLEND_MODE_MIX = 3
    }
}