package com.ninglang.pic_copy

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.os.Looper
import java.io.File
import android.content.ContentResolver
import androidx.core.net.toUri

class GalleryContentObserver(
    private val context: Context,
    private val onImageAdded: (Uri) -> Unit,
) : ContentObserver(Handler(Looper.getMainLooper())) {
    private var times = 0 //使用times_flag来表征图片是否下载完成，如果没有下载完成，则先不要复制，以免收到clipshare的影响，测试结果为4
    private var lastProcessedId: Long? = null
    private val contentResolver: ContentResolver = context.contentResolver
//    添加防抖机制,暂时不在使用
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri?.let {
            if (it.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                println("start")
                times +=1
//                if (times>=2){
                    println(times)
                    runnable?.let { handler.removeCallbacks(it) }
                    runnable = Runnable {
//                        这里必须是2，是因为为了能适配全能扫描王
                        if (times>=2)
                        {
                            println("Runable")
                            val latestImageUri = getLatestImageUri(context)
                            latestImageUri?.let { onImageAdded(it) }
                            times =0
                        }
                        runnable = null
                    }
                    handler.postDelayed(runnable!!, 500) // 延迟500毫秒
//                }
            }
        }
    }
    private  fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            // 打开输入流
            contentResolver.openInputStream(uri)?.use { inputStream ->
                // 计算输入流的总字节数
                inputStream.available().toLong()
            } ?: 0L // 如果输入流为空，返回 0
        } catch (e: Exception) {
            e.printStackTrace()
            0L // 捕获异常并返回 0
        }
    }


//    原来的机制，主要是通过这个time_flag
//    override fun onChange(selfChange: Boolean, uri: Uri?) {
//        super.onChange(selfChange, uri)
//        uri?.let {
//            println(it.toString())
////            println(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
//            if (it.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
//                val latestImageUri = getLatestImageUri(context)
//                println("latestImageUri:"+latestImageUri)
//                println("lastImgeUrl:"+latestImageUri)
//                println("lastProcessedId:"+lastProcessedId)
//                latestImageUri?.lastPathSegment?.toLongOrNull()?.let { id ->
//                    if (id != lastProcessedId) {
//                        times += 1
//                        println("times"+times)
//                        if (times>=4)
//                        {
//                            lastProcessedId = id
//                            onImageAdded(latestImageUri)
//                            times =0
//                            println("-------------------detect")
//                        }
//                    }
//                }
//            }
//        }
//    }
    private fun isFileReady(uri: Uri): Boolean {
        return try {
            println("path:"+uri.path)
            val file = File(uri.toString()!!)
            println("file:"+file.length()+file.exists()+"totalspace"+file.totalSpace+"user:"+file.usableSpace)
            file.exists() && file.length() > 0 // 文件存在且大小大于0
        } catch (e: Exception) {
            false
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
                val latestUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
//                val latestUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
//                isFileReady(latestUri)
//                if (isFileReady(latestUri))
//                {
                    return latestUri
//                }
            }
        }
        return null
    }
}