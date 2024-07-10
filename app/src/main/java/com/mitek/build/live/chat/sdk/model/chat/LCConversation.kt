package com.mitek.build.live.chat.sdk.model.chat

class LCConversation (
    var requestId:Int,
    var contactId:Int,
    var conversationId: String?,
    var name: String,
    var email: String?,
    var avatar: String?,
    var os: String?,
    var browser: String?,
    var browserLang: String?,
) {
    override fun toString(): String {
        return "LCConversation(requestId=$requestId, contactId=$contactId, conversationId=$conversationId, name='$name', email=$email, avatar=$avatar, os=$os, browser=$browser, browserLang=$browserLang)"
    }
}