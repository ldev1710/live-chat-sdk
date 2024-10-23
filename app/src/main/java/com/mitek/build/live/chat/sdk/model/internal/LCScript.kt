package com.mitek.build.live.chat.sdk.model.internal

data class LCScript (
    val id: String,
    val name: String,
    val nextAction: String,
    val answers: ArrayList<LCAnswer>,
    val buttonAction: ArrayList<LCButtonAction>,
)

data class LCButtonAction(
    val textSend: String,
    val nextId: String,
)

data class LCAnswer(
    val type: String,
    val value: String,
)
