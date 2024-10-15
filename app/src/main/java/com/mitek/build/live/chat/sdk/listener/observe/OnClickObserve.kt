package com.mitek.build.live.chat.sdk.listener.observe

import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction

interface OnClickObserve {
    fun onClick(lcButtonAction: LCButtonAction)
}
