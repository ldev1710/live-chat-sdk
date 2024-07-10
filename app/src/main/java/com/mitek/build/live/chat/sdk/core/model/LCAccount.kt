package com.mitek.build.live.chat.sdk.core.model

data class LCSupportType (
    val id:String,
    val name:String
) {
    override fun toString(): String {
        return "LCSupportType(id='$id', name='$name')"
    }
}

data class LCAccount(
    val id: Int,
    val groupId: Int,
    val groupName: String,
    val socketDomain: String,
    val hostName: String,
    val supportTypes: ArrayList<LCSupportType>
) {
    override fun toString(): String {
        return "LCAccount(id=$id, groupId=$groupId, groupName='$groupName', socketDomain='$socketDomain', hostName='$hostName', supportTypes=$supportTypes)"
    }
}
