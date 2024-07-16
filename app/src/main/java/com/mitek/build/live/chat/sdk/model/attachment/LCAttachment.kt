package com.mitek.build.live.chat.sdk.model.attachment

data class LCAttachment(val fileName: String, val extension: String,val url: String) : LCFile(fileName, extension)
