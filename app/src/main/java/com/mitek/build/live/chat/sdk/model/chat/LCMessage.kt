package com.mitek.build.live.chat.sdk.model.chat

data class LCMessage (
    var id: Int = 0,
    var content: String? = null,
    var timeCreated: String? = null
) {
    override fun toString(): String {
        return "LCMessage(id=$id, content=$content, timeCreated=$timeCreated)"
    }
}
