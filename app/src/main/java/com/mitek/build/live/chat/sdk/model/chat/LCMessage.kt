package com.mitek.build.live.chat.sdk.model.chat

data class LCMessage (
    var id: Int = 0,
    var content: String,
    var from: LCSender,
    var timeCreated: String
) {
    override fun toString(): String {
        return "LCMessage(id=$id, content='$content', from=$from, timeCreated='$timeCreated')"
    }
}
