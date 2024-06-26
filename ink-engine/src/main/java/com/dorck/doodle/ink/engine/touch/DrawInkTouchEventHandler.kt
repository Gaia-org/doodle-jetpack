package com.dorck.doodle.ink.engine.touch

import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.graphics.lowlatency.GLFrontBufferedRenderer
import androidx.input.motionprediction.MotionEventPredictor
import com.dorck.doodle.ink.engine.data.DrawPoint
import com.dorck.doodle.ink.engine.data.DrawSegment
import com.dorck.doodle.ink.engine.interpolator.CurvePointInterpolator
import kotlinx.coroutines.delay

/**
 * 收集和处理点数据，为后续笔记渲染做数据支撑
 * TODO 基于责任链改造
 */
class DrawInkTouchEventHandler(
    private val frontRenderer: GLFrontBufferedRenderer<DrawSegment>,
    private val motionEventPredictor: MotionEventPredictor
) : AbsTouchHandler() {
    private var mPreviousX: Float = 0f
    private var mPreviousY: Float = 0f
    private var mCurrentX: Float = 0f
    private var mCurrentY: Float = 0f

    override fun handleTouchEvent(event: MotionEvent, view: View): Boolean {
        motionEventPredictor.record(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "handleTouchEvent, down")
//                view.requestUnbufferedDispatch(event)
                mCurrentX = event.x
                mCurrentY = event.y

                val point = DrawPoint(
                    mCurrentX, mCurrentY,
                    pressure = event.pressure,
                    tilt = event.getAxisValue(MotionEvent.AXIS_TILT),
                    orientation = event.orientation,
                    time = event.eventTime
                )
                frontRenderer.renderFrontBufferedLayer(DrawSegment(mutableListOf(point), System.currentTimeMillis()))
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "handleTouchEvent, move")
                mPreviousX = mCurrentX
                mPreviousY = mCurrentY
                mCurrentX = event.x
                mCurrentY = event.y
                val point = DrawPoint(
                    mCurrentX, mCurrentY,
                    pressure = event.pressure,
                    tilt = event.getAxisValue(MotionEvent.AXIS_TILT),
                    orientation = event.orientation,
                    time = event.eventTime
                )
                val points = mutableListOf<DrawPoint>().apply {
                    add(point)
                }
                // TODO 预测点应该只用于快速预览阶段，真实上屏时应该剔除
                val predictedMotionEvent = motionEventPredictor.predict()
                val predictPoints = mutableListOf<DrawPoint>()
                if (predictedMotionEvent != null) {
                    Log.d(TAG, "handleTouchEvent, has predict point: ${predictedMotionEvent.x}, ${predictedMotionEvent.y}")
                    predictPoints.add(
                        DrawPoint(
                            predictedMotionEvent.x,
                            predictedMotionEvent.y,
                            pressure = predictedMotionEvent.pressure,
                            tilt = predictedMotionEvent.getAxisValue(MotionEvent.AXIS_TILT),
                            orientation = predictedMotionEvent.orientation,
                            time = predictedMotionEvent.eventTime
                        )
                    )
                }
                frontRenderer.renderFrontBufferedLayer(DrawSegment(points, System.currentTimeMillis(), predictPoints))
            }

            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "handleTouchEvent, up")
                frontRenderer.commit()
            }

            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "handleTouchEvent, cancel")
            }
        }
        return true
    }

    companion object {
        val TAG = DrawInkTouchEventHandler::class.java.simpleName
    }
}