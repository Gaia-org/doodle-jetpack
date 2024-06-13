package com.dorck.doodle.ink.engine.data

data class DrawSegment(
    var points: MutableList<DrawPoint>,
    val time: Long = 0L,
    var predictPoints: List<DrawPoint> = emptyList(),
)
