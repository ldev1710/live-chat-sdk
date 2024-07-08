package com.mitek.build.live.chat.sdk.model.attachment;

public class LCFile {
    protected String fileName;
    protected String extension;

    public LCFile(String fileName, String extension) {
        this.fileName = fileName;
        this.extension = extension;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }
}
