package com.github.roarappstudio.btkontroller.listeners

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.github.roarappstudio.btkontroller.senders.RelativeMouseSender
import java.util.*
import kotlin.concurrent.schedule

class GestureDetectListener(val rMouseSender : RelativeMouseSender) : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    //
    //  params
    //
//  val TAP                             = 0
//  val DOUBLE_TAP                      = 1
    val DOUBLE_TAP_TIMEOUT              = ViewConfiguration.getDoubleTapTimeout().toLong()
//  var mViewScaledTouchSlop: Float     = 0.toFloat()

    private var mCurrentDownEvent : MotionEvent?    = null
    private var mPtrCount                           = 0
    private var possibleTwoFingerTapFlag            = 0
    private var mPossibleTwoFingerTapStartTime      = System.currentTimeMillis()
//  private var mPrimStartTouchEventX               = 0f
//  private var mPrimStartTouchEventY               = 0f
    private var mSecStartTouchEventX                = 0f
    private var mSecStartTouchEventY                = 0f
    private var mPrimSecStartTouchDistance          = 0f
    private var notAConfirmedDoubleTapFlag          = 0
    private var disableSingleTapFlag                = 0
//  private var previousScrollX : Float             = 0f
//  private var previousScrollY : Float             = 0f
    private var testerp1                            = 0
    private var testerp2                            = 0
    private var stopScrollFlag                      = 0

    internal var downTimestamp                      = System.currentTimeMillis()

    //
    //  onTouchEvent
    //
    fun onTouchEvent(ev: MotionEvent?): Boolean {
        Log.i(TAG, "onTouchEvent, ev=${ev}")
        if( ev != null ) {
            val action = ev.action and MotionEvent.ACTION_MASK
            if( ev.pointerCount == 1 ) {
                if( stopScrollFlag == 1 ) {
                    rMouseSender.mouseReport.hScroll = 0
                    rMouseSender.mouseReport.vScroll = 0
                    stopScrollFlag = 0
                }
            }

            when( action ) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    mPtrCount++
                    if( ev.pointerCount > 1 ) {
                        testerp1 = ev.getPointerId(0)//remove at end of testing
                        testerp2 = ev.getPointerId(1)

                        mSecStartTouchEventX        = ev.getX(1)
                        mSecStartTouchEventY        = ev.getY(1)
                        mPrimSecStartTouchDistance  = distance(ev, 0, 1)
                        if( ev.pointerCount == 2 ) {
                            possibleTwoFingerTapFlag = 1
                        }
                        mCurrentDownEvent   = MotionEvent.obtain(ev)
                        downTimestamp       = System.currentTimeMillis()

                        return true
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    mPtrCount--
                }

                MotionEvent.ACTION_DOWN -> {
                    mPtrCount++
                    mPossibleTwoFingerTapStartTime = System.currentTimeMillis()
                }

                MotionEvent.ACTION_UP -> {
                    mPtrCount--
                    if( possibleTwoFingerTapFlag == 1 ) {
                        possibleTwoFingerTapFlag = 0
                        if( mPtrCount == 0 && ((System.currentTimeMillis() - mPossibleTwoFingerTapStartTime) <= ViewConfiguration.getTapTimeout()) ) {
                            disableSingleTapFlag =1
                            Log.i(TAG, "two finger single tap is implemented")
                            rMouseSender.sendRightClick()
                        }
                    }
                }
            }
        }
        return false
    }

    //
    //  onDoubleTap
    //
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.i(TAG,"onDoubleTap::this is on double tap ${e}")
        return false
    }

    //
    //  onDoubleTapEvent
    //
    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.i(TAG,"onDoubleTapEvent::this is on double tap event ${e}")
        if( mPtrCount == 1 ) {
            if( e != null ) {
                if( e.action == MotionEvent.ACTION_DOWN ) {
                    Timer().schedule(150L) {
                        if( mPtrCount == 1 ) {
                            notAConfirmedDoubleTapFlag=1;
                            rMouseSender.sendLeftClickOn()
                            Log.i(TAG,"this is on double tap and hold and also $DOUBLE_TAP_TIMEOUT and $e ")
                        }
                    }
                }
            }
        }

        if( mPtrCount == 0 ) {
            if( e != null ) {
                if( e.action == MotionEvent.ACTION_UP ) {
                    if( notAConfirmedDoubleTapFlag == 0 ) {
                        rMouseSender.sendDoubleTapClick()
                        Log.i(TAG, "this is on double tap confirmed $e")
                    } else {
                        notAConfirmedDoubleTapFlag=0
                        rMouseSender.sendLeftClickOff()
                    }
                }
            }
        }

        return false
    }

    //
    //  onSingleTapConfirmed
    //
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.i(TAG,"onSingleTapConfirmed::this is on single tap confirmed ${e}")
        if( disableSingleTapFlag == 1 ) {
            disableSingleTapFlag = 0
        } else {
            rMouseSender.sendTestClick()
        }
        return false
    }

    //
    //  onSingleTapUp
    //
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i(TAG,"onSingleTapUp::this is on single tap up ${e}")
        return true
    }

    //
    //  onDown
    //
    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "onDown: ${e}")
       return false

    }

    //
    //  onFiling
    //
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.d(TAG, "onFiling: e1=${e1}, e2=${e2}, velocity=[${velocityX},${velocityY}]")
        Log.i("\tthis is a fling e1 ","$e1")
        Log.i("\tthis is a fling e2 ","$e2")
        Log.i("\tthis is a fling vx ","$velocityX")
        Log.i("\tthis is a fling vy ","$velocityY")

        return false
    }

    //
    //  onScroll
    //
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.i(TAG, "onScroll, e1=${e1}, e2=${e2}, distance=[${distanceX},${distanceY}]")
        if( mPtrCount == 2 ) {
            var dy: Int = 0
            var dx :Int = 0

            if( distanceY > 0 ) {
                dy = -1
            } else if( distanceY < 0 ) {
                dy = 1
            } else if( distanceY == 0f ) {
                dy = 0
            }

            if( distanceX > 2 ) {
                dx= 1
            } else if( distanceX < -2 ) {
                dx = -1
            } else {
                dx = 0
            }

            if( dx > 127 ) {
                dx = 127
            } else if( dx < -127 ) {
                dx = -127
            }

            Log.i(TAG, "onScroll::rMouseSender.sendScroll(" + dy.toString() + ", " + dx.toString())
            rMouseSender.sendScroll(dy, dx)

            stopScrollFlag = 1
        }
        return false
    }

    //
    //  onLongPress
    //
    override fun onLongPress(e: MotionEvent?) {
        Log.i(TAG, "onLongPress, e=${e}")
    }

    //
    //  onShowPress
    //
    override fun onShowPress(e: MotionEvent?) {
        Log.i(TAG, "onShowPress, e=${e}")
    }

    //
    //  dstance
    //
    fun distance(event: MotionEvent, first: Int, second: Int): Float {
        if( event.pointerCount >= 2 ) {
            val x = event.getX(first) - event.getX(second)
            val y = event.getY(first) - event.getY(second)
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        } else {
            return 0f
        }
    }

    companion object {
        const val TAG = "GestureDetectListener"
    }
}