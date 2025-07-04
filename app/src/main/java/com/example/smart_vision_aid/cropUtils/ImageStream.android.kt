package com.example.smart_vision_aid.cropUtils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

public fun interface ImageStream {
    fun openStream(): InputStream?
}

public suspend fun Uri.toImageSrc(context: Context) = ImageStreamSrc(UriImageStream(this, context))
public suspend fun File.toImageSrc() = ImageStreamSrc(FileImageStream(this))

public data class FileImageStream(val file: File) : ImageStream {
    override fun openStream(): InputStream = file.inputStream()
}

public data class UriImageStream(val uri: Uri, val context: Context) : ImageStream {
    override fun openStream(): InputStream? = context.contentResolver.openInputStream(uri)
}

