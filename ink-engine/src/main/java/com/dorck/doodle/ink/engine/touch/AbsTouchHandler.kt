package com.dorck.doodle.ink.engine.touch

import android.view.MotionEvent
import android.view.View

abstract class AbsTouchHandler {
    abstract fun handleTouchEvent(event: MotionEvent, view: View): Boolean
}