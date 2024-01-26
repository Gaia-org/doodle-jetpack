package com.dorck.doodle.ink.engine.data

data class DrawPoint(
    var x: Float,
    var y: Float,
    val state: Int = 0,
    val pressure: Float = 0F,
    val tilt: Float = 0F,
    val orientation: Float = 0F,
    val velocity: Float = 0F,
    val rotation: Float = 0F,
    val time: Long = 0L
    ) {
        fun isValid(): Boolean {
            return x == 0f && y == 0f && pressure == 0f && tilt == 0f
                    && orientation == 0f && velocity == 0f && rotation == 0f && time == 0L
        }


        companion object {
            const val VISIBLE = 0
            const val INVISIBLE = 1

            fun DrawPoint.clonePoint(): DrawPoint {
                return DrawPoint(
                    x, y, state, pressure, tilt, orientation, velocity, rotation, time
                )
            }
        }
    }
