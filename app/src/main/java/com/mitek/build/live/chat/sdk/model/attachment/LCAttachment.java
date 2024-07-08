package com.mitek.build.live.chat.sdk.model.attachment;

public class LCAttachment extends LCFile{
    private final String url;

    public LCAttachment(String fileName, String extension, String url) {
        super(fileName, extension);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
