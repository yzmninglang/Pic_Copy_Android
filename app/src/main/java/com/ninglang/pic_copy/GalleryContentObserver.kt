package com.ninglang.pic_copy

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.os.Looper

class GalleryContentObserver(
    private val context: Context,
    private val onImageAdded: (Uri) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {
    private var times = 0 //使用times_flag来表征图片是否下载完成，如果没有下载完成，则先不要复制，以免收到clipshare的影响，测试结果为4
    private var lastProcessedId: Long? = null
//    添加防抖机制,暂时不在使用
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
//    override fun onChange(selfChange: Boolean, uri: Uri?) {
//        super.onChange(selfChange, uri)
//        uri?.let {
//            if (it.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
//                println("start")
//                runnable?.let { handler.removeCallbacks(it) }
//                runnable = Runnable {
//                    println("Runable")
//                    val latestImageUri = getLatestImageUri(context)
//                    latestImageUri?.let { onImageAdded(it) }
//                    runnable = null
//                }
//                handler.postDelayed(runnable!!, 500) // 延迟500毫秒
//            }
//        }
//    }


//    原来的机制，主要是通过这个time_flag
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri?.let {
            println(it.toString())
//            println(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
            if (it.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                val latestImageUri = getLatestImageUri(context)
                println("latestImageUri:"+latestImageUri)
                println("lastImgeUrl:"+latestImageUri)
                println("lastProcessedId:"+lastProcessedId)
                latestImageUri?.lastPathSegment?.toLongOrNull()?.let { id ->
                    if (id != lastProcessedId) {
                        times += 1
                        println("times"+times)
                        if (times>=4)
                        {
                            lastProcessedId = id
                            onImageAdded(latestImageUri)
                            times =0
                            println("-------------------detect")
                        }
                    }
                }
            }
        }
    }

    private fun getLatestImageUri(context: Context): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                println("cursor:"+it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)))
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            }
        }
        return null
    }
}