package com.dorck.doodle.ink.engine.render

import android.opengl.GLES30
import com.dorck.doodle.ink.engine.data.BrushType
import com.dorck.doodle.ink.engine.data.DrawCurve
import com.dorck.doodle.ink.engine.data.DrawPoint

class InkRenderer : AbsBrushRenderer() {

    override val mVertexShader: String
        get() = """
            #version 300 es

            layout(location = 0) in vec4 aPosition;
            layout(location = 1) uniform vec4 aColor;
            layout(location = 2) uniform mat4 uMVPMatrix;
            out vec4 vColor;
            out float vCenterScale;

            void main() {
                vColor = aColor;
                vec4 pos = vec4(aPosition.xy, 1.0, 1.0);
                pos = uMVPMatrix * pos;
                gl_Position = vec4(pos.xy, 0.0, 1.0);
                vCenterScale = aPosition.w;
                gl_PointSize = aPosition.z;
            }
        """.trimIndent()
    override val mFragShader: String
        get() = """
            #version 300 es

            precision mediump float;
            in vec4 vColor;
            in float vCenterScale;
            out vec4 FragColor;

            void main() {
                float dist = length(gl_PointCoord - vec2(0.5));
                float alpha = smoothstep(vCenterScale, 0.5, dist); // 使用 1.0-dist 可实现粒子文字效果
                alpha = vColor.a * (1.0 - alpha);

                if (dist <= 0.5) { 
                    FragColor = vec4(vColor.rgb, alpha);
                } else {
                    FragColor = vec4(0.0);
                }
            }          
        """.trimIndent()


    override fun getType(): BrushType = BrushType.INK

    override fun drawPoints(points: List<DrawPoint>, matrix: FloatArray) {
        GLES30.glUseProgram(mProgramId)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glUniformMatrix4fv(2, 1, false, matrix, 0)
        GLES30.glUniform4f(1, mColors!!.get(0), mColors!!.get(1), mColors!!.get(2), mColors!!.get(3))
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        // 启用颜色混合及预乘实现标准透明混合（更贴近现实效果）
        GLES30.glBlendFuncSeparate(
            GLES30.GL_SRC_ALPHA,
            GLES30.GL_ONE_MINUS_SRC_ALPHA,
            1,
            GLES30.GL_ONE_MINUS_SRC_ALPHA
        )
        val pointFloatBuffer = createFloatBuffer(points.size * 4)
        val pointData = FloatArray(points.size * 4)
        // Transform to gl coordinates
        for (pointIndex in points.indices) {
            val point = points[pointIndex]
            pointData[pointIndex * 4] = point.x
            pointData[pointIndex * 4 + 1] = point.y
            // pointData[pointIndex * 4 + 2] = mBrushSize / 4.0f + mBrushSize * point.pressure
            pointData[pointIndex * 4 + 2] = mBrushSize * 1.5f
            pointData[pointIndex * 4 + 3] = 0.5f
        }
        pointFloatBuffer.put(pointData)
        pointFloatBuffer.position(0)
        GLES30.glVertexAttribPointer(0, 4, GLES30.GL_FLOAT, false, 16, pointFloatBuffer)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, pointFloatBuffer.capacity() / 4)
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisableVertexAttribArray(0)
    }

    override fun drawCurve(curve: DrawCurve, matrix: FloatArray) {
        drawPoints(curve.drawPoints, matrix)
    }

    companion object {
        private const val TAG = "InkRenderer"
    }

}