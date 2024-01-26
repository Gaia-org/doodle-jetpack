package com.dorck.doodle.ink.engine.touch

import android.view.MotionEvent

abstract class AbsTouchHandler {
    abstract fun handleTouchEvent(event: MotionEvent): Boolean
}