package com.dorck.doodle.ink.engine.interpolator

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import com.dorck.doodle.ink.engine.data.DrawCurve
import com.dorck.doodle.ink.engine.data.DrawPoint
import com.dorck.doodle.ink.engine.data.DrawPoint.Companion.clonePoint
import kotlin.math.abs

/**
 * Implement point interpolation processing of curves.
 *
 * @author Dorck
 */
class CurvePointInterpolator(private val drawPoints: MutableList<DrawPoint>, private val curveBendingThreshold: Float = 2.0f) {
    private var mStartPoint: DrawPoint = DrawPoint(0f, 0f)
    private var mProcessedPoints: MutableList<DrawPoint> = mutableListOf()
    private var mPath: Path = Path()
    private var mPathMeasure: PathMeasure = PathMeasure()
    private var mMatrix: Matrix = Matrix()
    private var mPreviousPathDistance = 0f
    private var mMatrixValues: FloatArray = FloatArray(9)

    fun processCurveFitting() {
        synchronized(drawPoints) {
            Log.d(TAG, "processCurveFitting, before processing points size: ${drawPoints.size}")
            if (drawPoints.isEmpty()) {
                return
            }
            initializeForProcessing()
            for (index in 1 until drawPoints.size) {
                fillCurvePoints(drawPoints[index])
            }
            endCurveFitting()
        }
    }

    private fun endCurveFitting() {
        if (mProcessedPoints.isEmpty() && !mStartPoint.isValid()) {
            mProcessedPoints.add(mStartPoint)
        }
        Log.d(TAG, "endCurveFitting, points size: ${mProcessedPoints.size}")
    }

    private fun initializeForProcessing() {
        val firstPoint = drawPoints.first()
        mStartPoint.x = firstPoint.x
        mStartPoint.y = firstPoint.y
        mPath.reset()
        mPath.moveTo(firstPoint.x, firstPoint.y)
        mProcessedPoints.add(firstPoint.clonePoint())
    }

    fun fillCurvePoints(drawPoint: DrawPoint, interpolationInterval: Float = 0.10f) {
        // 1.先将目标点添加到 mPath 构建路径 (根据点距离采用线性或贝塞尔曲线构建)
        val centerX = (drawPoint.x + mStartPoint.x) / 2.0f
        val centerY = (drawPoint.y + mStartPoint.y) / 2.0f
        val xDistance = abs(drawPoint.x - mStartPoint.x)
        val yDistance = abs(drawPoint.y - mStartPoint.y)
        if (xDistance * xDistance + yDistance * yDistance > curveBendingThreshold * curveBendingThreshold) {
            // 超过了点之间的最大距离阙值，需要采取曲线拟合(取上次的点与中间点)
            mPath.quadTo(mStartPoint.x, mStartPoint.y, centerX, centerY)
        } else {
            mPath.lineTo(drawPoint.x, drawPoint.y)
        }
        mStartPoint.x = drawPoint.x
        mStartPoint.y = drawPoint.y
        // 2.根据构建的Path在曲线上线性取点
        var tempPressure = drawPoint.pressure
        var tempTilt = drawPoint.tilt
        var tempOrientation = drawPoint.orientation
        var tempVelocity = drawPoint.velocity
        var tempRotation = drawPoint.rotation
        val pressureDiff = drawPoint.pressure - mStartPoint.pressure
        val tiltDiff = drawPoint.tilt - mStartPoint.tilt
        val orientationDiff = drawPoint.orientation - mStartPoint.orientation
        val velocityDiff = drawPoint.velocity - mStartPoint.velocity
        val rotationDiff = drawPoint.rotation - mStartPoint.rotation
        mPathMeasure.setPath(mPath, false)
        val pathLength = mPathMeasure.length
        while (mPreviousPathDistance + interpolationInterval <= pathLength) {
            val nextSamplingPointLen = mPreviousPathDistance + interpolationInterval // 下一个采样的位置点
            if (mPathMeasure.getMatrix(nextSamplingPointLen, mMatrix, PathMeasure.TANGENT_MATRIX_FLAG or PathMeasure.POSITION_MATRIX_FLAG)) {
                mPreviousPathDistance = nextSamplingPointLen
                mMatrix.getValues(mMatrixValues)
                val fittingPoint = DrawPoint(mMatrixValues[2], mMatrixValues[5], drawPoint.state,
                    tempPressure, tempTilt, tempOrientation, tempVelocity, tempRotation, drawPoint.time)
                mProcessedPoints.add(fittingPoint)
            }
            val ratio = nextSamplingPointLen / pathLength
            tempPressure = pressureDiff * ratio
            tempTilt = tiltDiff * ratio
            tempOrientation = orientationDiff * ratio
            tempVelocity = velocityDiff * ratio
            tempRotation = rotationDiff * ratio
        }
    }

    fun getProcessingPoints(): List<DrawPoint> = mProcessedPoints

    fun addPointToPath(drawPoint: DrawPoint) {
        mProcessedPoints.clear()
        drawPoints.add(drawPoint)
        fillCurvePoints(drawPoint)
        mStartPoint = drawPoint
    }

    companion object {
        private val TAG = CurvePointInterpolator::class.java.simpleName
    }
}