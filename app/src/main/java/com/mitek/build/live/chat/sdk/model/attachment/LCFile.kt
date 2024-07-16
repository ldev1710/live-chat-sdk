package com.mitek.build.live.chat.sdk.model.attachment

open class LCFile(private val fileName: String, private val extension: String){
    override fun toString(): String {
        return "LCFile(fileName='$fileName', extension='$extension')"
    }
}
