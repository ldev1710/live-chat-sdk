package com.example.mifonelibproj.util;

import android.content.Context;
import android.os.Build;

import com.example.mifonelibproj.R;

import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LoggingServiceListener;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

import java.util.ArrayList;


public class MifoneContext {
    private static MifoneContext sInstance = null;

    private Context mContext;

    private final LoggingServiceListener mJavaLoggingService =
            (logService, domain, lev, message) -> {
                switch (lev) {
                    case Debug:
                        android.util.Log.d(domain, message);
                        break;
                    case Message:
                        android.util.Log.i(domain, message);
                        break;
                    case Warning:
                        android.util.Log.w(domain, message);
                        break;
                    case Error:
                        android.util.Log.e(domain, message);
                        break;
                    case Fatal:
                    default:
                        android.util.Log.wtf(domain, message);
                        break;
                }
            };
    private MifoneManager mMifoneManager;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static MifoneContext instance() {
        if (sInstance == null) {
            throw new RuntimeException("[Context] Mifone Context not available!");
        }
        return sInstance;
    }

    public MifoneContext(Context context) {
        mContext = context;

        MifonePreferences.instance().setContext(context);
        Factory.instance().setLogCollectionPath(context.getFilesDir().getAbsolutePath());
        boolean isDebugEnabled = MifonePreferences.instance().isDebugEnabled();
        MifoneUtils.configureLoggingService(isDebugEnabled, context.getString(R.string.app_name));

        dumpDeviceInformation();

        sInstance = this;
        mMifoneManager = new MifoneManager(context);
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public LoggingServiceListener getJavaLoggingService() {
        return mJavaLoggingService;
    }

    public MifoneManager getMifoneManager() {
        return mMifoneManager;
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        Log.i(sb.substring(0, sb.length() - 2));
    }

}
