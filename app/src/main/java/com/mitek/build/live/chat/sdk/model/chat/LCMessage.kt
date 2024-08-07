package com.mitek.build.live.chat.sdk.model.chat

import com.google.gson.annotations.SerializedName
import java.util.UUID

class LCMessage {
    var id: Int = 0
    @SerializedName("mapping_id")
    var mappingId: String?
    var content: LCContent? = null
    var from: LCSender? = null
    @SerializedName("created_at")
    var timeCreated: String? = null

    constructor(id: Int,mappingId: String?, content: LCContent?, from: LCSender?, timeCreated: String?) {
        this.id = id
        this.mappingId = mappingId
        this.content = content
        this.from = from
        this.timeCreated = timeCreated
    }


    override fun toString(): String {
        return "LCMessage(id=$id, mappingId='$mappingId', content='$content', from=$from, timeCreated='$timeCreated')"
    }
}
