package com.github.roarappstudio.btkontroller

import android.app.Application
import android.util.Log

@Suppress("unused") // It's in fucking manifest -_-
class Kontroller : Application() {
    override fun onCreate() {
        Log.i("Kontroller:", "Kontroller::onCreate..." )
        super.onCreate()
    }
}