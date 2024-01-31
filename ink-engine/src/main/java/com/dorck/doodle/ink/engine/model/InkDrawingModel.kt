package com.dorck.doodle.ink.engine.model

import android.util.Log
import com.dorck.doodle.ink.engine.data.DrawCurve
import com.dorck.doodle.ink.engine.interpolator.CurvePointInterpolator

/**
 * 管理所有笔迹、拟合处理等
 * @author Dorck
 */
object InkDrawingModel : BaseModel() {
    private const val TAG = "InkDrawingModel"
    private val mCurves: MutableList<DrawCurve> = mutableListOf()
    private var mCurCurve: DrawCurve? = null
    @Volatile
    private var mTotalPointsSize: Int = 0

    fun insertCurve(drawCurve: DrawCurve) {
        if (!mCurves.contains(drawCurve)) {
            drawCurve.id = mCurves.size + 1
            mCurves.add(drawCurve)
        }
        val originSize = drawCurve.drawPoints.size
        mCurCurve = drawCurve
        // 曲线拟合、插值处理
        val curvePointInterpolator = CurvePointInterpolator(drawCurve.drawPoints).apply {
            processCurveFitting()
            drawCurve.drawPoints = getProcessingPoints()
            mTotalPointsSize += drawCurve.drawPoints.size
        }
        Log.i(TAG, "insertCurve, curves size: " + originSize + ", inserted curve points: ${curvePointInterpolator.getProcessingPoints().size}, total point size: $mTotalPointsSize")
    }

    fun removeCurve(drawCurve: DrawCurve): Boolean {
        val succeed = mCurves.remove(drawCurve)
        if (succeed && mTotalPointsSize > 0) {
            mTotalPointsSize -= drawCurve.drawPoints.size
        }
        return succeed
    }

    fun getAllCurves(): List<DrawCurve> = mCurves

    fun getTotalPointsSize(): Int = mTotalPointsSize

    fun clearAllCurves() {
        mCurves.clear()
        mCurCurve = null
        mTotalPointsSize = 0
    }

    override fun onDestroy() {
        clearAllCurves()
    }
}