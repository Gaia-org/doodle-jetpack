package com.dorck.doodle.ink.engine.render

import androidx.annotation.WorkerThread
import com.dorck.doodle.ink.engine.data.BrushData
import com.dorck.doodle.ink.engine.data.BrushType

class BrushRendererManager {
    private var mCurBrushRenderer: AbsBrushRenderer? = null

    private var mBrushRenderers = hashMapOf<BrushType, AbsBrushRenderer>()

    init {
        mBrushRenderers[BrushType.INK] = InkRenderer()
    }

    fun switchCurrentRenderer(brushData: BrushData) {
        val existRenderer = obtainBrushRenderer(brushData.type)
        if (!existRenderer.isInitialized) {
            existRenderer.initialize()
        }
        mCurBrushRenderer = existRenderer
        mCurBrushRenderer?.applyBrushConfig(brushData)
    }

    @WorkerThread
    fun getCurrentRenderer(): AbsBrushRenderer? = mCurBrushRenderer?.also {
        if (!it.isInitialized) {
            it.initialize()
        }
    }

    fun obtainBrushRenderer(brushType: BrushType): AbsBrushRenderer = mBrushRenderers[brushType]
        ?: throw IllegalArgumentException("Not support this type of brush: $brushType")
}