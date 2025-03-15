package com.ninglang.pic_copy

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkPermissions()) {
            requestPermissions()
            requestBackgroundAccess()
            println("get")
        }

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            if (!isListening) {
//                isListening = !isListening
                startButton.text = getString(R.string.button_started)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                Toast.makeText(this, "开始监听", Toast.LENGTH_SHORT).show()
//                println(isListening)

                startGalleryService()
            } else {
                startButton.text = getString(R.string.button_start)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800080"))
                stopGalleryService()
                Toast.makeText(this, "停止监听", Toast.LENGTH_SHORT).show()
            }
//            反转一下flag
            isListening = !isListening

        }
    }

    private fun startGalleryService() {

        println("-------------GalleryForegroundService kaiqi")
        val intent = Intent(this, GalleryForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopGalleryService() {
        val intent = Intent(this, GalleryForegroundService::class.java)
        stopService(intent)
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            100
        )
    }

    private fun requestBackgroundAccess() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}