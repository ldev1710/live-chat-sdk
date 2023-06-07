package com.example.mifonelibproj.util;

import android.os.Handler;
import android.os.Looper;

import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;

public final class MifoneUtils {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private MifoneUtils() {}

    public static void configureLoggingService(boolean isDebugEnabled, String appName) {
        if (!MifonePreferences.instance().useJavaLogger()) {
            Factory.instance().enableLogCollection(LogCollectionState.Enabled);
            Factory.instance().setDebugMode(isDebugEnabled, appName);
        } else {
            Factory.instance().setDebugMode(isDebugEnabled, appName);
            Factory.instance()
                    .enableLogCollection(LogCollectionState.EnabledWithoutPreviousLogHandler);
            if (isDebugEnabled) {
                if (MifoneContext.isReady()) {
                    Factory.instance()
                            .getLoggingService()
                            .addListener(MifoneContext.instance().getJavaLoggingService());
                }
            } else {
                if (MifoneContext.isReady()) {
                    Factory.instance()
                            .getLoggingService()
                            .removeListener(MifoneContext.instance().getJavaLoggingService());
                }
            }
        }
    }
    public static void dispatchOnUIThread(Runnable r) {
        sHandler.post(r);
    }
}