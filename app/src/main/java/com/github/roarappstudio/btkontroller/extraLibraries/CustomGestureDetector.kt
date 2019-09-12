package com.github.roarappstudio.btkontroller.extraLibraries

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.util.Log
import com.github.roarappstudio.btkontroller.listeners.GestureDetectListener

class CustomGestureDetector(context: Context, internal var mListener: GestureDetectListener) : GestureDetector(context, mListener) {

    //
    //  onTouchEvent
    //
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        Log.i(TAG, "onTouchEvent")
        val consume = if (mListener != null) mListener!!.onTouchEvent(ev) else false
        return consume || super.onTouchEvent(ev)
    }

    companion object {
        const val TAG = "CustomGestureDetector"
    }
}
