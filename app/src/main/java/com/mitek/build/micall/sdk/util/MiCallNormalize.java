package com.mitek.build.micall.sdk.util;

public class MiCallNormalize {
    public static String normalizeRemoteUri(String raw){
        return raw.substring(raw.indexOf("sip:")+4,raw.indexOf("@"));
    }
}
