package com.dorck.doodle.ink.engine.data

data class DrawCurve(
    var drawPoints: List<DrawPoint>,
    var id: Int = -1,
    var state: Int = NORMAL,
    var time: Long = 0L,
) {
    companion object {
        const val NORMAL = 0
        const val RENDERING = 1
        const val ON_SCREEN = 2
        const val INVISIBLE = 3
    }
}
