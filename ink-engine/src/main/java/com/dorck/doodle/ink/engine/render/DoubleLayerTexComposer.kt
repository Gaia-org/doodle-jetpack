package com.dorck.doodle.ink.engine.render

import android.graphics.RectF
import android.opengl.GLES30
import com.dorck.doodle.ink.engine.util.GLRenderUtil
import com.dorck.doodle.ink.engine.util.OpenGLShaderUtil
import java.nio.FloatBuffer

class DoubleLayerTexComposer {
    private val vertexShader = """
        #version 300 es
        layout(location = 0) in vec3 aPosition;
        layout(location = 1) in vec2 aTexCoord;

        layout(location = 3) uniform mat4 uMVPMatrix;

        out vec2 vTexCoord;

        void main() {
            vec4 matrixPos = uMVPMatrix * vec4(aPosition.x, aPosition.y, 1.0, 1.0);
            gl_Position = vec4(matrixPos.x, matrixPos.y, 0.0, 1.0);
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragShader = """
        #version 300 es
        precision highp float;

        in vec2 vTexCoord;
        layout(location = 0) uniform sampler2D uTexture1;
        layout(location = 1) uniform sampler2D uTexture2;
        
        out vec4 FragColor;

        void main() {
            float aAlpha = 1.0;
            vec4 aboveColor = texture(uTexture1, vTexCoord);
            vec4 belowColor = texture(uTexture2, vTexCoord);
        
            aboveColor = vec4(aboveColor.rgb * aAlpha, aboveColor.a * aAlpha);
            vec3 result = vec3(aboveColor.rgb + (1.0 - aboveColor.a) * belowColor.rgb);
            vec3 result2 = vec3((1.0 - belowColor.a) * aboveColor.rgb + belowColor.rgb);
            vec3 finalColor = min(result, result2);
            float blendAlpha = aboveColor.a + belowColor.a - aboveColor.a * belowColor.a;
            FragColor = vec4(finalColor.r / blendAlpha, finalColor.g / blendAlpha, finalColor.b / blendAlpha, blendAlpha);
           
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

    fun drawTexture(texId1: Int, texId2: Int, matrix: FloatArray) {
        GLES30.glUseProgram(mProgramId)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
//        GLES30.glBlendFuncSeparate(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA, 1, GLES30.GL_ONE_MINUS_SRC_ALPHA);
//        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ZERO)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId1)
        GLES30.glUniform1i(0, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId2)
        GLES30.glUniform1i(1, 1)
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
}