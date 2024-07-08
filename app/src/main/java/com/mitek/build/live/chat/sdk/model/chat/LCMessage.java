package com.mitek.build.live.chat.sdk.model.chat;

import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment;

import java.util.List;

public class LCMessage {
    int id;
    String content;
    List<LCAttachment> attachments;
    String timeCreated;
}
