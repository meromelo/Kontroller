package com.github.roarappstudio.btkontroller.senders



import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import android.view.KeyEvent
import com.github.roarappstudio.btkontroller.reports.KeyboardReport

@Suppress("MemberVisibilityCanBePrivate")
open class KeyboardSender(
    val hidDevice   : BluetoothHidDevice,
    val host        : BluetoothDevice

) {
    //
    //  params
    //
    val keyboardReport = KeyboardReport()

    //
    //  sendKeys
    //
    protected open fun sendKeys() {
        Log.i(TAG, "sendKeys")
        if( !hidDevice.sendReport(host, KeyboardReport.ID, keyboardReport.bytes) ) {
            Log.e(TAG, "Report wasn't sent")
        }
    }

    //
    //  customSender
    //
    protected open fun customSender(modifier_checked_state: Int) {
        Log.i(TAG, "customSender: modifier_checked_state=${modifier_checked_state}")
        sendKeys()
        if( modifier_checked_state == 0 ) {
            sendNullKeys()
        } else {
            keyboardReport.key1 = 0.toByte()
            sendKeys()
        }
    }

    //
    //  sendModifiers
    //
    protected open fun setModifiers(event:KeyEvent) {
        Log.i(TAG, "setModifiers: event=${event}")
        if( event.isShiftPressed ) keyboardReport.leftShift    = true
        if( event.isAltPressed   ) keyboardReport.leftAlt      = true
        if( event.isCtrlPressed  ) keyboardReport.leftControl  = true
        if( event.isMetaPressed  ) keyboardReport.leftGui      = true
    }

    //
    //  sendNullKeys
    //
    fun sendNullKeys() {
        Log.i(TAG, "sendNullKeys")
        keyboardReport.bytes.fill(0)
        if( !hidDevice.sendReport(host, KeyboardReport.ID, keyboardReport.bytes) ) {
            Log.e(TAG, "Report wasn't sent")
        }
    }

    //
    //  keyEventHandler
    //
    fun keyEventHandler(keyEventCode: Int, event : KeyEvent, modifier_checked_state: Int,keyCode:Int): Boolean {
        Log.i(TAG, "keyEventHandler: keyEventCode=${keyEventCode}, event=${event}, modifier_check_state=${modifier_checked_state}, keyCode=${keyCode}")

        val byteKey = KeyboardReport.KeyEventMap[keyEventCode]

        if( byteKey != null ) {
            setModifiers(event)
            if( event.keyCode == KeyEvent.KEYCODE_AT || event.keyCode == KeyEvent.KEYCODE_POUND || event.keyCode == KeyEvent.KEYCODE_STAR ) {
                keyboardReport.leftShift = true
            }
            keyboardReport.key1 = byteKey.toByte()
            customSender(modifier_checked_state)
            return true
        } else {
            return false
        }
    }


    //
    //  sendKeyboard
    //
    fun sendKeyboard(keyCode : Int, event : KeyEvent, modifier_checked_state :Int): Boolean {
        Log.i(TAG, "sendKeyboard: keyCode=${keyCode}, event=${event}, modifier_checked_state=${modifier_checked_state}")
        return keyEventHandler(event.keyCode, event, modifier_checked_state, keyCode)
    }

    companion object {
        const val TAG = "KeyboardSender"
    }

}