package com.dorck.doodle.ink.engine.data

import android.graphics.Color

/**
 * 笔刷的配置信息
 */
data class BrushData(
    var type: BrushType,
    var color: Int = Color.RED,
    var size: Float = 12F,
    var alpha: Int = 255,
    var texImg: Int = -1
)
