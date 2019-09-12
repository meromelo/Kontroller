package com.github.roarappstudio.btkontroller.senders

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import com.github.roarappstudio.btkontroller.reports.ScrollableTrackpadMouseReport
import java.util.*
import kotlin.concurrent.schedule

@Suppress("MemberVisibilityCanBePrivate")
open class RelativeMouseSender(
    val hidDevice: BluetoothHidDevice,
    val host: BluetoothDevice

) {
    //
    //  params
    //
    val mouseReport = ScrollableTrackpadMouseReport()
    var previousvscroll :Int=0
    var previoushscroll :Int =0


    protected open fun sendMouse() {
        Log.i("RelativeMouseSender", "sendMouse")
        if( !hidDevice.sendReport(host, ScrollableTrackpadMouseReport.ID, mouseReport.bytes) ) {
            Log.e(TAG, "Report wasn't sent")
        }
    }

    fun sendTestMouseMove() {
        Log.i("RelativeMouseSender", "sendTestMouseMove")
        mouseReport.dxLsb = 20
        mouseReport.dyLsb = 20
        mouseReport.dxMsb = 20
        mouseReport.dyMsb = 20
        sendMouse()
    }

    fun sendTestClick() {
        Log.i("RelativeMouseSender", "sendTestClick")
        mouseReport.leftButton = true
        sendMouse()
        mouseReport.leftButton = false
        sendMouse()
//        Timer().schedule(20L) {
//
//        }
    }

    fun sendDoubleTapClick() {
        Log.i("RelativeMouseSender", "sendDoubleTapClick")
        mouseReport.leftButton = true
        sendMouse()
        Timer().schedule(100L) {
            mouseReport.leftButton = false
            sendMouse()
            Timer().schedule(100L) {
                mouseReport.leftButton = true
                sendMouse()
                Timer().schedule(100L) {
                    mouseReport.leftButton = false
                    sendMouse()
                }
            }
        }
    }



    fun sendLeftClickOn() {
        Log.i("RelativeMouseSender", "sendLeftClickOn")
        mouseReport.leftButton = true
        sendMouse()
    }

    fun sendLeftClickOff() {
        Log.i("RelativeMouseSender", "sendLeftClickOff")
        mouseReport.leftButton = false
        sendMouse()

    }

    fun sendRightClick() {
        Log.i("RelativeMouseSender", "sendRightClick")
        mouseReport.rightButton = true
        sendMouse()
        Timer().schedule(50L) {
            mouseReport.rightButton= false
            sendMouse()
        }
    }

    fun sendScroll(vscroll:Int,hscroll:Int){
        Log.i("RelativeMouseSender", "sendScroll")

        var hscrollmutable  = 0
        var vscrollmutable  = 0

        hscrollmutable      = hscroll
        vscrollmutable      = vscroll

//        var dhscroll= hscrollmutable-previoushscroll
//        var dvscroll= vscrollmutable-previousvscroll
//
//        dhscroll = Math.abs(dhscroll)
//        dvscroll = Math.abs(dvscroll)
//        if(dvscroll>=dhscroll) {
//            hscrollmutable=0
//        } else {
//            vscrollmutable=0
//        }
//
        var vs:Int = (vscrollmutable)
        var hs:Int = (hscrollmutable)
        Log.i("RelativeMouseSender", "vscroll " + vscroll.toString())
        Log.i("RelativeMouseSender", "vs "      + vs.toString())
        Log.i("RelativeMouseSender", "hscroll " + hscroll.toString())
        Log.i("RelativeMouseSender", "hs "      + hs.toString())

        mouseReport.vScroll = vs.toByte()
        mouseReport.hScroll = hs.toByte()

        sendMouse()

//      previousvscroll=-1*vscroll
//      previoushscroll=hscroll
    }

    companion object {
        const val TAG = "TrackPadSender"
    }

}