package com.mitek.build.live.chat.sdk.model.chat

class LCSender {
    var id:String? = null
    var name:String? = null

    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }


    override fun toString(): String {
        return "LCSender(id=$id, name='$name')"
    }
}