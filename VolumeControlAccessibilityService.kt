package com.example.volumecontrolapp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class VolumeControlAccessibilityService : AccessibilityService() {

    private val TARGET_APPS = setOf(
        "com.zhiliaoapp.musically", // TikTok'un paket adı
        "com.instagram.android" // Instagram'ın paket adı
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Erişilebilirlik olaylarını işlemek için (gerekirse)
    }

    override fun onInterrupt() {
        // Servis kesintiye uğradığında
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        // Ses düğmesi olaylarını yakala
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val foregroundPackage = getForegroundPackageName()
            if (foregroundPackage in TARGET_APPS) {
                // Hedef uygulamalardan biri ön plandaysa
                if (event.action == KeyEvent.ACTION_DOWN) {
                    // Ses açma: yukarı kaydırma, Ses kapatma: aşağı kaydırma
                    val isVolumeUp = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    performSwipe(isVolumeUp)
                    return true // Olayı tüket (ses kontrolü devre dışı bırakılır)
                }
            }
        }
        return super.onKeyEvent(event) // Normal ses kontrolü için olayı tüketme
    }

    private fun getForegroundPackageName(): String? {
        val window = rootInActiveWindow
        return window?.packageName?.toString()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun performSwipe(isUp: Boolean) {
        val path = Path().apply {
            // Ekranın ortasından kaydırma
            moveTo(500f, 1000f) // Başlangıç noktası (ekranın ortası)
            lineTo(500f, if (isUp) 200f else 1800f) // Yukarı veya aşağı kaydırma
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200)) // 200ms kaydırma süresi
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d("VolumeControl", "Kaydırma tamamlandı")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d("VolumeControl", "Kaydırma iptal edildi")
            }
        }, null)
    }
}