package com.example.dbmeter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()
    var spikes = ArrayList<RectF>()
    private var amplitudes = ArrayList<Float>()
    private var radius = 0f
    private var w = 2f
    private var d = 1f

    private var sw = 0f
    private var sh = 0f
    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(255, 87, 34)
        sw = resources.displayMetrics.widthPixels.toFloat()

        maxSpikes = (sw / (w+d)).toInt()
    }

    fun addAmplitude(amp: Float) {
        var norm = Math.min(amp.toInt()/7, 400).toFloat()
        var getDb = amp


        if(amp <= 0) {
            getDb = 0.0f
        } else {
            amplitudes.add(amp)
            getDb = amp
        }

        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for(i in amps.indices) {
            var left = sw - i*(w+d)
            var top = 0f
            var right = left + w + 1f
            var bottom = amps[i] * 4

            spikes.add(RectF(left, top, right, bottom))
        }

        // println(spikes)
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        // canvas?.drawRoundRect(RectF(20f, 30f, 20+30f, 30f+60f), 6f, 6f, paint)
       spikes.forEach() {
           canvas?.drawRoundRect(it, radius, radius, paint)
           // canvas?.drawRoundRect(RectF(70f, 60f, 40+60f, 40f+80f), 6f, 6f, paint)

       }


    }
}
