package com.mitek.build.live.chat.sdk.util

import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.chat.LCContent
import org.json.JSONArray
import org.json.JSONObject

object LCParseUtils {
    fun parseLCContentFrom(rawContent: JSONObject,isPush: Boolean = false) : LCContent{
        val contentType = rawContent.getString("content-type")
        var lcContent = LCContent(
            contentType,
            ""
        )
        if(contentType == "file" || contentType == "image"){
            val lcAttachments = ArrayList<LCAttachment>()
            val rawContentMessages: JSONArray = if (isPush) {
                JSONArray(rawContent.getString("content-message"))
            } else {
                rawContent.getJSONArray("content-message")
            }
            val len = rawContentMessages.length()
            for (i in 0..<len) {
                val rawE = rawContentMessages.getJSONObject(i)
                val fileName = rawE.getString("file-name")
                val extension = fileName.split('.').last()
                lcAttachments.add(
                    LCAttachment(
                        fileName,
                        extension,
                        rawE.getString("url"),
                    )
                )
            }
            lcContent.contentMessage = lcAttachments
        } else {
            lcContent.contentMessage = rawContent.getString("content-message")
        }
        return lcContent
    }
}