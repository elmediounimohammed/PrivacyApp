package com.example.privacyapp.utils

import android.content.Context
import android.net.Uri
import java.io.File

object FileUtils {
    fun createImageFile(context: Context): File {
        return File(
            context.getExternalFilesDir(null),
            "reference_face.jpg"
        ).apply {
            if (exists()) {
                delete()
            }
        }
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return Uri.fromFile(file)
    }

    fun clearReferenceImage(context: Context) {
        File(
            context.getExternalFilesDir(null),
            "reference_face.jpg"
        ).delete()
    }
}