package com.mitek.build.live.chat.sdk.model.chat

import com.google.gson.annotations.SerializedName

class LCMessage {
    var id: Int = 0
    var content: LCContent? = null
    var from: LCSender? = null
    @SerializedName("created_at")
    var timeCreated: String? = null

    constructor(id: Int, content: LCContent?, from: LCSender?, timeCreated: String?) {
        this.id = id
        this.content = content
        this.from = from
        this.timeCreated = timeCreated
    }


    override fun toString(): String {
        return "LCMessage(id=$id, content='$content', from=$from, timeCreated='$timeCreated')"
    }
}
