package com.github.roarappstudio.btkontroller.listeners

import android.view.MotionEvent
import android.view.View
import android.util.Log

class CompositeListener : View.OnTouchListener {

    private var registeredListeners : MutableList<View.OnTouchListener> = ArrayList<View.OnTouchListener>()

    fun registerListener(listener : View.OnTouchListener): Unit {
        Log.i("CompositeListener", "registerListener")
        registeredListeners.add(listener)

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.i("CompositeListener", "onTouch")

        for( listener:View.OnTouchListener in registeredListeners ) {
            listener.onTouch(v,event)
        }
        return true
    }
}