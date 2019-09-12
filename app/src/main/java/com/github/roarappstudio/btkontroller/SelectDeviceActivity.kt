package com.github.roarappstudio.btkontroller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.github.roarappstudio.btkontroller.listeners.CompositeListener
import com.github.roarappstudio.btkontroller.listeners.GestureDetectListener
import com.github.roarappstudio.btkontroller.senders.RelativeMouseSender
import com.github.roarappstudio.btkontroller.senders.SensorSender
import com.github.roarappstudio.btkontroller.listeners.ViewListener
import org.jetbrains.anko.*
import com.github.roarappstudio.btkontroller.extraLibraries.CustomGestureDetector
import com.github.roarappstudio.btkontroller.senders.KeyboardSender


class SelectDeviceActivity : Activity(), KeyEvent.Callback
{
    //
    //  vars
    //
    private             var autoPairMenuItem        : MenuItem? = null
    private             var screenOnMenuItem        : MenuItem? = null
    private             var bluetoothStatus         : MenuItem? = null
    private lateinit    var linearLayout            : _LinearLayout
    private             var sender                  : SensorSender? = null
    private             var modifier_checked_state  : Int = 0
//  private             var rMouseSender            : RelativeMouseSender? = null
    private             var rKeyboardSender         : KeyboardSender? = null

    //
    //  onCreate
    //
    @SuppressLint("ResourceType")
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate..." );
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate...verticalLayout" );
        verticalLayout {
            //
            // justify your toolbar
            //
            linearLayout    = this
            id              = 0x69
            textView(){
                id              = R.id.mouseView
                background      = getDrawable(R.drawable.view_border)
                text            = "Trackpad"
                gravity         = Gravity.CENTER
            }.lparams(width = matchParent, height = matchParent)
        }
        Log.i(TAG, "onCreate...done" );
    }

    //
    //  getContext
    //
    fun getContext() : Context {
        Log.i(TAG, "getContext..." );
        return this
    }

    //
    //  onStart
    //
    public override fun onStart() {
        Log.i(TAG, "onStart:: bluetoothStatus1=" + bluetoothStatus );
        super.onStart()
        Log.i(TAG, "onStart:: bluetoothStatus2=" + bluetoothStatus );

        bluetoothStatus?.icon               = getDrawable(R.drawable.ic_action_app_not_connected)
        bluetoothStatus?.tooltipText        = "App not connected via bluetooth"
        val sharedPref                      = this.getPreferences(Context.MODE_PRIVATE)
        BluetoothController.autoPairFlag    = sharedPref.getBoolean(getString(R.string.auto_pair_flag),false)
        autoPairMenuItem?.isChecked         = sharedPref.getBoolean(getString(R.string.auto_pair_flag),false)
        screenOnMenuItem?.isChecked         = sharedPref.getBoolean(getString(R.string.screen_on_flag),false)

        if( sharedPref.getBoolean(getString(R.string.screen_on_flag),false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val trackPadView                    = find<View>(R.id.mouseView)


        //
        //  bluetooth
        //
        Log.i(TAG, "onStart::bluetooth init" );
        BluetoothController.init(this)
        BluetoothController.getSender { hidd, device ->
            Log.wtf(TAG, "onStart::called callback.")
            val mainHandler = Handler(getContext().mainLooper)
            mainHandler.post(object : Runnable {
                override fun run() {
                    Log.i(TAG, "onStart::mainLoop run" );
                    rKeyboardSender         = KeyboardSender(hidd,device)
                    val rMouseSender        = RelativeMouseSender(hidd,device)

                    Log.i(TAG, "onStart::mainLoop run::getName=" + Thread.currentThread().getName());

                    val viewTouchListener   = ViewListener(hidd, device, rMouseSender)
                    val mDetector           = CustomGestureDetector(getContext(), GestureDetectListener(rMouseSender))
                    val gTouchListener      = object : View.OnTouchListener {
                        override fun onTouch(v:View?, event:MotionEvent?): Boolean {
                            Log.i(TAG, "onStart::mainLoop run::onTouch v=${v}, event=${event}")
                            return mDetector.onTouchEvent(event)
                        }
                    }

                    val composite : CompositeListener = CompositeListener()
                    composite.registerListener(gTouchListener)
                    composite.registerListener(viewTouchListener)
                    trackPadView.setOnTouchListener(composite)

                    Log.i(TAG, "onStart:: bluetoothStatus3=" + bluetoothStatus );
                    bluetoothStatus?.icon           = getDrawable(R.drawable.ic_action_app_connected)
                    bluetoothStatus?.tooltipText    ="App Connected via bluetooth"
                }
            })

            Log.i(TAG, "onStart::mainLoop, name=" + Thread.currentThread().getName());
        }

        BluetoothController.getDisconnector{
            Log.i(TAG, "onStart::getDisconnector")

            val mainHandler = Handler(getContext().mainLooper)
            mainHandler.post(object : Runnable {
                override fun run() {
                    Log.i(TAG, "onStart:: bluetoothStatus4=" + bluetoothStatus );
                    bluetoothStatus?.icon           = getDrawable(R.drawable.ic_action_app_not_connected)
                    bluetoothStatus?.tooltipText    = "App not connected via bluetooth"
                    Log.i(TAG, "onStart::mainLooper::disconnect run")
                }
            })
        }
        Log.i(TAG, "onStart:: bluetoothStatus5=" + bluetoothStatus );

        Log.i(TAG, "onStart...done" );
    }

    //
    //  initSensor
    //
    private fun initSensor() {
        Log.i(TAG, "initSensor")
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        sensorManager.registerListener(sender, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    //
    //  onPause
    //
    public override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
    }

    //
    //  onStop
    //
    public override fun onStop() {
        Log.i(TAG, "onStop")
        super.onStop()
        BluetoothController.btHid?.unregisterApp()
        BluetoothController.hostDevice  = null
        BluetoothController.btHid       = null
    }

    //
    //  onCreateOptionsMenu
    //
    public override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.i(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.select_device_activity_menu, menu)

        bluetoothStatus     = menu?.findItem(R.id.ble_app_connection_status)
        autoPairMenuItem    = menu?.findItem(R.id.action_autopair)
        screenOnMenuItem    = menu?.findItem(R.id.action_screen_on)
        val sharedPref      = this.getPreferences(Context.MODE_PRIVATE)

        screenOnMenuItem?.isChecked     = sharedPref.getBoolean(getString(R.string.screen_on_flag),false);
        autoPairMenuItem?.isChecked     = sharedPref.getBoolean(getString(R.string.auto_pair_flag),false)

        return super.onCreateOptionsMenu(menu)
    }

    //
    //  onKeyDown
    //
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(TAG, "onKeyDown:: event=${event}, keyCode=${keyCode}")

        if( rKeyboardSender != null && event != null ) {
            var rvalue : Boolean? = false
            if( rvalue == true ) {
                return true
            }
            return super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }


    //
    //  onKeyUp
    //
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(TAG, "onKeyUp:: event=${event}, keyCode=${keyCode}")

        if( rKeyboardSender != null && event != null ) {
            var rvalue: Boolean? = false
            rvalue = rKeyboardSender?.sendKeyboard(keyCode, event,modifier_checked_state)
            if( rvalue == true ) {
                return true
            }
            Log.i(TAG, "onKeyUp:: super onKeyDown event=${event}, keyCode=${keyCode}")
            return super.onKeyDown(keyCode, event)
        }
        Log.i(TAG, "onKeyUp:: super onKeyUp   event=${event}, keyCode=${keyCode}")
        return super.onKeyUp(keyCode, event)
    }


    //
    //  onOptionsItemSelected
    //
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            Log.i(TAG, "onOptionsItemSelected:: setting")
            true
        }

        R.id.action_keyboard -> {
            Log.i(TAG, "onOptionsItemSelected:: keyboard")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
            true
        }

        R.id.check_modifier_state -> {
            Log.i(TAG, "onOptionsItemSelected:: modifier")
            if(modifier_checked_state==1) {
                modifier_checked_state=0
                item.title="(N)"
                rKeyboardSender?.sendNullKeys()

            } else {
                modifier_checked_state=1
                item.title="(P)"
            }
            true
        }

        R.id.action_disconnect -> {
            Log.i(TAG, "onOptionsItemSelected:: disconnect")
            BluetoothController.btHid?.disconnect(BluetoothController.hostDevice)
            bluetoothStatus?.icon           = getDrawable(R.drawable.ic_action_app_not_connected)
            bluetoothStatus?.tooltipText    = "App not connected via bluetooth"
            true
        }

        R.id.action_screen_on -> {
            Log.i(TAG, "onOptionsItemSelected:: screen_on")
            val sharedPref = this?.getPreferences(Context.MODE_PRIVATE)
            if( item.isChecked ) {
                item.isChecked = false

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                with( sharedPref.edit() ) {
                    putBoolean(getString(R.string.screen_on_flag), false)
                    commit()
                }
            } else {
                item.isChecked=true
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.screen_on_flag), true)
                    commit()
                }
            }
            true
        }

        R.id.action_autopair -> {
            Log.i(TAG, "onOptionsItemSelected:: autopair")
            val sharedPref = this?.getPreferences(Context.MODE_PRIVATE)
            if(item.isChecked) {
                item.isChecked = false
                BluetoothController.autoPairFlag=false

                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.auto_pair_flag), BluetoothController.autoPairFlag)
                    commit()
                }
            } else {
                item.isChecked=true
                BluetoothController.autoPairFlag=true
                if(BluetoothController.btHid?.getConnectionState(BluetoothController.mpluggedDevice)==0 && BluetoothController.mpluggedDevice!= null && BluetoothController.autoPairFlag ==true) {
                    BluetoothController.btHid?.connect(BluetoothController.mpluggedDevice)
                    //hostDevice.toString()
                }
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.auto_pair_flag), BluetoothController.autoPairFlag)
                    commit()
                }
            }
            true
        }

        else -> {
            Log.i(TAG, "onOptionsItemSelected:: else")
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val TAG = "SelectDeviceActivity"
    }
}