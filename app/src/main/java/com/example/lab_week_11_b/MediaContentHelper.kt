package com.example.lab_week_11_b

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore

class MediaContentHelper {
    fun getImageContentUri(): Uri =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    fun getVideoContentUri(): Uri =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

    fun generateImageContentValues(fileInfo: FileInfo): ContentValues =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileInfo.name)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, fileInfo.relativePath)
            }
            put(MediaStore.Images.Media.MIME_TYPE, fileInfo.mimeType)
        }

    fun generateVideoContentValues(fileInfo: FileInfo): ContentValues =
        ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileInfo.name)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, fileInfo.relativePath)
            }
            put(MediaStore.Video.Media.MIME_TYPE, fileInfo.mimeType)
        }
}