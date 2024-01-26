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

    fun insertCurve(drawCurve: DrawCurve) {
        if (!mCurves.contains(drawCurve)) {
            drawCurve.id = mCurves.size + 1
            mCurves.add(drawCurve)
        }
        mCurCurve = drawCurve
        // 曲线拟合、插值处理
        val curvePointInterpolator = CurvePointInterpolator(drawCurve.drawPoints).apply {
            processCurveFitting()
            drawCurve.drawPoints = getProcessingPoints()
        }
        Log.i(TAG, "insertCurve, curves size: " + mCurves.size + ", inserted curve points: ${curvePointInterpolator.getProcessingPoints().size}")
    }

    fun removeCurve(drawCurve: DrawCurve): Boolean {
        return mCurves.remove(drawCurve)
    }

    fun getAllCurves(): List<DrawCurve> = mCurves

    override fun onDestroy() {
        mCurves.clear()
    }
}