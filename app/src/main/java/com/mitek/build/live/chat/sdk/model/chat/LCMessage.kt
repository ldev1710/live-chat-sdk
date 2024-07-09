package com.mitek.build.live.chat.sdk.model.chat

import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment

class LCMessage {
    var id: Int = 0
    var content: String? = null
    var attachments: List<LCAttachment>? = null
    var timeCreated: String? = null
}
