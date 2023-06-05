package com.example.mifonelibproj.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.example.mifonelibproj.R;
import com.example.mifonelibproj.api.Common;
import com.example.mifonelibproj.api.IResponseAPIs;
import com.example.mifonelibproj.model.other.ProfileUser;
import com.example.mifonelibproj.model.other.UpdateTokenFirebase;
import com.example.mifonelibproj.model.response.Logout;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.Transports;
import org.linphone.core.Tunnel;
import org.linphone.core.TunnelConfig;
import org.linphone.core.VideoActivationPolicy;
import org.linphone.core.VideoDefinition;
import org.linphone.core.tools.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MifonePreferences {
    private static final String LINPHONE_DEFAULT_RC = "/.linphonerc";
    private static final String LINPHONE_FACTORY_RC = "/linphonerc";
    private static final String LINPHONE_LPCONFIG_XSD = "/lpconfig.xsd";
    private static final String DEFAULT_ASSISTANT_RC = "/default_assistant_create.rc";
    private static final String LINPHONE_ASSISTANT_RC = "/linphone_assistant_create.rc";
    private static MifonePreferences sInstance;
    private Context mContext;
    private static String mBasePath;
    private MifonePreferences() {}

    public static synchronized MifonePreferences instance() {
        if (sInstance == null) {
            sInstance = new MifonePreferences();
        }
        return sInstance;
    }

    public void setContext(Context c) {
        mContext = c;
        mBasePath = mContext.getFilesDir().getAbsolutePath();
        android.util.Log.d("TAG", "base path: "+mBasePath);
        try {
            copyAssetsFromPackage();
        } catch (IOException ioe) {

        }
    }


    /* Assets stuff */
    private void copyAssetsFromPackage() throws IOException {
        copyIfNotExist(R.raw.linphonerc_default, getMifoneDefaultConfig());
        copyFromPackage(R.raw.linphonerc_factory, new File(getMifoneFactoryConfig()).getName());
        copyIfNotExist(R.raw.lpconfig, mBasePath + LINPHONE_LPCONFIG_XSD);
        copyFromPackage(
                R.raw.default_assistant_create,
                new File(mBasePath + DEFAULT_ASSISTANT_RC).getName());
        copyFromPackage(
                R.raw.linphone_assistant_create,
                new File(mBasePath + LINPHONE_ASSISTANT_RC).getName());
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = mContext.openFileOutput(target, 0);
        InputStream lInputStream = mContext.getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public static String getMifoneDefaultConfig() {
        return mBasePath + LINPHONE_DEFAULT_RC;
    }

    public static String getMifoneFactoryConfig() {
        return mBasePath + LINPHONE_FACTORY_RC;
    }

    public String getDefaultDynamicConfigFile() {
        return mBasePath + DEFAULT_ASSISTANT_RC;
    }

    public String getMifoneDynamicConfigFile() {
        return mBasePath + LINPHONE_ASSISTANT_RC;
    }

    private Core getLc() {
        if (!MifoneContext.isReady()) return null;

        return MifoneManager.mCore;
    }

    public Config getConfig() {
        Core core = getLc();
        if (core != null) {
            return core.getConfig();
        }

        if (!MifoneContext.isReady()) {
            File linphonerc = new File(mBasePath + "/.linphonerc");
            if (linphonerc.exists()) {
                return Factory.instance().createConfig(linphonerc.getAbsolutePath());
            } else if (mContext != null) {
                InputStream inputStream =
                        mContext.getResources().openRawResource(R.raw.linphonerc_default);
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                StringBuilder text = new StringBuilder();
                String line;
                try {
                    while ((line = buffreader.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                } catch (IOException ioe) {
                    Log.e(ioe);
                }
                return Factory.instance().createConfigFromString(text.toString());
            }
        } else {
            return Factory.instance().createConfig(getMifoneDefaultConfig());
        }
        return null;
    }

    public void firstLaunchSuccessful() {
        getConfig().setBool("app", "first_launch", false);
    }

    public boolean shouldAutomaticallyAcceptVideoRequests() {
        if (getLc() == null) return false;
        VideoActivationPolicy vap = getLc().getVideoActivationPolicy();
        return vap.getAutomaticallyAccept();
    }
    public boolean isPushNotificationEnabled() {
        if (getConfig() == null) return true;
        return getConfig().getBool("app", "push_notification", true);
    }

    public void setPushNotificationEnabled(boolean enable) {
        if (getConfig() == null) return;
        getConfig().setBool("app", "push_notification", enable);

        Core core = getLc();
        if (core == null) {
            return;
        }

        if (enable) {
            // Add push infos to exisiting proxy configs
            String regId = getPushNotificationRegistrationID();
            String appId = "1:699475125868:android:d4d102a9ede27b2fdb9a49";
            if (regId != null && core.getProxyConfigList().length > 0) {
                for (ProxyConfig lpc : core.getProxyConfigList()) {
                    if (lpc == null) continue;
                    if (lpc.isPushNotificationAllowed()) {
                        lpc.edit();
                        lpc.setContactUriParameters(null);
                        lpc.done();
                        if (lpc.getIdentityAddress() != null)
                            Log.d(
                                    "[Push Notification] infos removed from proxy config "
                                            + lpc.getIdentityAddress().asStringUriOnly());
                    } else {
                        String contactInfos =
                                "pn-provider="
                                        + ";pn-type="
                                        + "android"
                                        + ";pn-param="
                                        + appId
                                        + ";pn-timeout=0"
                                        + ";pn-prid="
                                        + regId
                                        + ";pn-silent=1";
                        String prevContactParams = lpc.getContactParameters();
                        if (prevContactParams == null
                                || prevContactParams.compareTo(contactInfos) != 0) {
                            lpc.edit();
                            lpc.setContactUriParameters(contactInfos);
                            lpc.done();
                            if (lpc.getIdentityAddress() != null)
                                Log.d(
                                        "[Push Notification] info added to proxy config "
                                                + lpc.getIdentityAddress().asStringUriOnly());
                        }
                    }
                }
                Log.i(
                        "[Push Notification] Refreshing registers to ensure token is up to date: "
                                + regId);
                core.refreshRegisters();
            }
        } else {
            if (core.getProxyConfigList().length > 0) {
                for (ProxyConfig lpc : core.getProxyConfigList()) {
                    lpc.edit();
                    lpc.setContactUriParameters(null);
                    lpc.done();
                    if (lpc.getIdentityAddress() != null)
                        Log.d(
                                "[Push Notification] infos removed from proxy config "
                                        + lpc.getIdentityAddress().asStringUriOnly());
                }
                core.refreshRegisters();
            }
        }
    }

    public String getPushNotificationRegistrationID() {
        if (getConfig() == null) return null;
        return getConfig().getString("app", "push_notification_regid", null);
    }

    public boolean isDebugEnabled() {
        if (getConfig() == null) return false;
        return getConfig().getBool("app", "debug", false);
    }

    public boolean useJavaLogger() {
        if (getConfig() == null) return false;
        return getConfig().getBool("app", "java_logger", false);
    }

    public String getXmlrpcUrl() {
        if (getConfig() == null) return null;
        return getConfig().getString("assistant", "xmlrpc_url", null);
    }

    public void setServiceNotificationVisibility(boolean enable) {
        if (getConfig() == null) return;
        getConfig().setBool("app", "show_service_notification", enable);
    }
    public boolean isDeviceRingtoneEnabled() {
        int readExternalStorage =
                mContext.getPackageManager()
                        .checkPermission(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                mContext.getPackageName());
        if (getConfig() == null) return readExternalStorage == PackageManager.PERMISSION_GRANTED;
        return getConfig().getBool("app", "device_ringtone", true)
                && readExternalStorage == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isAutoAnswerEnabled() {
        if (getConfig() == null) return false;
        return getConfig().getBool("app", "auto_answer", false);
    }

    public String getDeviceName(Context context) {
        return "test";
    }
}
