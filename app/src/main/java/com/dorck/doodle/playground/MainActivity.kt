package com.dorck.doodle.playground

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dorck.doodle.ink.engine.data.BrushData
import com.dorck.doodle.ink.engine.data.BrushType
import com.dorck.doodle.ink.engine.render.FastGLFrontRenderer
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val container = findViewById<FrameLayout>(R.id.renderContainer)
//        val brushManager = BrushRendererManager().apply {
//            switchCurrentRenderer(BrushData(BrushType.INK, Color.BLUE, size = 6f))
//        }

        val brushConfig = BrushData(BrushType.INK, color = Color.WHITE, size = 16f)
        val fastRenderer = FastGLFrontRenderer().apply {
            applyBrushConfig(brushConfig)
        }
        val inkSurfaceView = InkSurfaceView(this, fastRenderer)
        container.addView(inkSurfaceView)
        val colorButton = findViewById<TextView>(R.id.buttonColor)
        colorButton.setOnClickListener {
            val genColor = generateRandomColor()
            colorButton.setTextColor(genColor)

            fastRenderer.applyBrushConfig(brushConfig.copy(color = genColor))
        }
        findViewById<TextView>(R.id.buttonClear).setOnClickListener {
            fastRenderer.clearCanvas()
        }
    }

    private fun generateRandomColor(): Int {
        val random = Random()

        // 生成随机的 RGB 分量
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)

        // 使用 Color.rgb() 方法创建颜色
        return Color.rgb(red, green, blue)
    }
}