package com.mitek.build.live.chat.sdk.core.model

data class ResponseUploadFile(
    val error: Boolean,
    val message: String?,
) {
    override fun toString(): String {
        return "ResponseUploadFile(error=$error, message=$message)"
    }
}