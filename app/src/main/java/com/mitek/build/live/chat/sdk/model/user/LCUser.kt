package com.mitek.build.live.chat.sdk.model.user

data class LCUser (
    val fullName:String,
    val email: String?,
    val phone: String?,
    val deviceName: String?
) {
    override fun toString(): String {
        return "LCUser(fullName='$fullName', email=$email, phone=$phone, deviceName=$deviceName)"
    }
}