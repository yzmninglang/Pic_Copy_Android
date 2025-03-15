package com.ninglang.pic_copy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
//import kotlinx.coroutines.*

class GalleryForegroundService : Service() {

    private lateinit var galleryObserver: GalleryContentObserver

    override fun onCreate() {
        super.onCreate()
        println("here")
        createNotificationChannel()
        startForeground(1, buildNotification())
        galleryObserver = GalleryContentObserver(this) { latestImageUri ->
//            println("latestImageUriForeGround:"+latestImageUri)
            copyToClipboard(latestImageUri)
        }
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            galleryObserver
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(galleryObserver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "gallery_service_channel",
                "Gallery Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "gallery_service_channel")
            .setContentTitle("图库监听服务")
            .setContentText("正在后台监听图库变化...")
            .setSmallIcon(R.mipmap.ic_launcher_foreground) // 替换为您的应用图标
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun copyToClipboard(imageUri: Uri) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newUri(contentResolver, "Image URI", imageUri)
        clipboard.setPrimaryClip(clip)
//        Toast.makeText(this, "图片已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}