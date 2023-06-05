package com.example.mifonelibproj.util;

import static com.example.mifonelibproj.core.MifoneCoreHandle.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import com.example.mifonelibproj.config.BuildConfig;

import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListenerStub;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.FriendList;
import org.linphone.core.ProxyConfig;
import org.linphone.core.Reason;
import org.linphone.core.tools.Log;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MifoneManager implements SensorEventListener {
    private final String mBasePath;
    private final String mRingSoundFile;
    private final String mCallLogDatabaseFile;
    private final String mFriendsDatabaseFile;
    private final String mUserCertsPath;
    private WakeLock mProximityWakelock;
    private final SensorManager mSensorManager;
    private final Sensor mProximity;
    private Context mContext;
    private final MifonePreferences mPrefs;
    public static Core mCore;
    private CoreListenerStub mCoreListener;
    private AccountCreator mAccountCreator;
    private IntentFilter mCallIntentFilter;
    private boolean mExited;
    private boolean mCallGsmON;
    private boolean mProximitySensingEnabled;
    private Runnable mIterateRunnable;
    private final PowerManager mPowerManager;
    public MifoneManager(Context c) {
        android.util.Log.d("TAG", "MifoneManager: Contructor is called");
        mExited = false;
        mContext = c;
        mBasePath = c.getFilesDir().getAbsolutePath();
        mCallLogDatabaseFile = mBasePath + "/linphone-log-history.db";
        mFriendsDatabaseFile = mBasePath + "/linphone-friends.db";
        mRingSoundFile = mBasePath + "/share/sounds/linphone/rings/notes_of_the_optimistic.mkv";
        mUserCertsPath = mBasePath + "/user-certs";

        mPrefs = MifonePreferences.instance();
        mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mCoreListener =
                new CoreListenerStub() {
                    @SuppressLint("Wakelock")
                    @Override
                    public void onCallStateChanged(
                            final Core core,
                            final Call call,
                            final Call.State state,
                            final String message) {
                        android.util.Log.d("TAG", "onCallStateChanged: 09");
                        Log.i("[Manager] Call state is [", state, "]");
                        if (state == Call.State.IncomingReceived
                                && !call.equals(core.getCurrentCall())) {
                            if (call.getReplacedCall() != null) {
                                return;
                            }
                        }

                        if ((state == Call.State.IncomingReceived || state == Call.State.IncomingEarlyMedia)
                                && getCallGsmON()) {
                            if (mCore != null) {
                                call.decline(Reason.Busy);
                            }
                        } else if (state == Call.State.IncomingReceived
                                && (MifonePreferences.instance().isAutoAnswerEnabled())
                                && !getCallGsmON()) {
                        } else if (state == Call.State.End || state == Call.State.Error) {
                            if (mCore.getCallsNb() == 0) {
                                enableProximitySensing(false);
                            }
                        } else if (state == Call.State.UpdatedByRemote) {
                            boolean remoteVideo = call.getRemoteParams().videoEnabled();
                            boolean localVideo = call.getCurrentParams().videoEnabled();
                            boolean autoAcceptCameraPolicy =
                                    MifonePreferences.instance()
                                            .shouldAutomaticallyAcceptVideoRequests();
                            if (remoteVideo
                                    && !localVideo
                                    && !autoAcceptCameraPolicy
                                    && mCore.getConference() == null) {
                                call.deferUpdate();
                            }
                        }
                    }

                    @Override
                    public void onFriendListCreated(Core core, FriendList list) {
                    }

                    @Override
                    public void onFriendListRemoved(Core core, FriendList list) {
                    }
                };
    }

    public static synchronized MifoneManager getInstance() {
        MifoneManager manager = MifoneContext.instance().getMifoneManager();
        if (manager == null) {
            throw new RuntimeException(
                    "[Manager] Mifone Manager should be created before accessed");
        }
        if (manager.mExited) {
            throw new RuntimeException(
                    "[Manager] Mifone Manager was already destroyed. "
                            + "Better use getCore and check returned value");
        }
        return manager;
    }

    public void startLibLinphone(boolean isPush, CoreListenerStub listener) {
        try {
            android.util.Log.d(TAG, "onCreate: trigger");
            mCore =
                    Factory.instance()
                            .createCore(
                                    mPrefs.getMifoneDefaultConfig(),
                                    mPrefs.getMifoneFactoryConfig(),
                                    mContext);
            android.util.Log.d(TAG, "onCreate: created");
            mCoreListener = listener;
            mCore.addListener(mCoreListener);
            if(mCore == null) android.util.Log.d(TAG, "onCreate: nulllllll");
            else android.util.Log.d(TAG, "onCreate: not nulllllll");
            if (isPush) {
                Log.w(
                        "[Manager] We are here because of a received push notification, enter background mode before starting the Core");
                mCore.enterBackground();
            }
            mCore.start();

            mIterateRunnable =
                    () -> {
                        if (mCore != null) {
                            mCore.iterate();
                        }
                    };
            TimerTask lTask =
                    new TimerTask() {
                        @Override
                        public void run() {
                            MifoneUtils.dispatchOnUIThread(mIterateRunnable);
                        }
                    };

            configureCore();
        } catch (Exception e) {
            Log.e(e, "[Manager] Cannot start Mifone");
        }
        mCallIntentFilter = new IntentFilter("android.intent.action.ACTION_NEW_OUTGOING_CALL");
        mCallIntentFilter.setPriority(99999999);
    }

    private synchronized void configureCore() {
        Log.i("[Manager] Configuring Core");

        mCore.setZrtpSecretsFile(mBasePath + "/zrtp_secrets");

        String deviceName = mPrefs.getDeviceName(mContext);
        String appName = "MiFone";
        String androidVersion = BuildConfig.VERSION_NAME;
        String userAgent = appName + "/" + androidVersion + " (" + deviceName + ") ";

        mCore.setUserAgent(userAgent, "");
        mCore.setCallLogsDatabasePath(mCallLogDatabaseFile);
        mCore.setFriendsDatabasePath(mFriendsDatabaseFile);
        mCore.setUserCertificatesPath(mUserCertsPath);
        enableDeviceRingtone(mPrefs.isDeviceRingtoneEnabled());

        int availableCores = Runtime.getRuntime().availableProcessors();
        Log.w("[Manager] MediaStreamer : " + availableCores + " cores detected and configured");

        mCore.migrateLogsFromRcToDb();
        String uri = "https:mifone.vn";
        for (ProxyConfig lpc : mCore.getProxyConfigList()) {
            if (lpc.getIdentityAddress().getDomain().equals("mifone.vn/mitek")) {
                if (lpc.getConferenceFactoryUri() == null) {
                    lpc.edit();
                    Log.i(
                            "[Manager] Setting conference factory on proxy config "
                                    + lpc.getIdentityAddress().asString()
                                    + " to default value: "
                                    + uri);
                    lpc.setConferenceFactoryUri(uri);
                    lpc.done();
                }
            }
        }

        mProximityWakelock =
                mPowerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        mContext.getPackageName() + ";manager_proximity_sensor");


        mAccountCreator = mCore.createAccountCreator(MifonePreferences.instance().getXmlrpcUrl());
        mCallGsmON = false;

        Log.i("[Manager] Core configured");
    }

    /* Account linking */

    public AccountCreator getAccountCreator() {
        if (mAccountCreator == null) {
            mAccountCreator = mCore.createAccountCreator(null);
        }
        return mAccountCreator;
    }

    public void enableProximitySensing(boolean enable) {
        if (enable) {
            if (!mProximitySensingEnabled) {
                mSensorManager.registerListener(
                        this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
                mProximitySensingEnabled = true;
            }
        } else {
            if (mProximitySensingEnabled) {
                mSensorManager.unregisterListener(this);
                mProximitySensingEnabled = false;
                // Don't forgeting to release wakelock if held
                if (mProximityWakelock.isHeld()) {
                    mProximityWakelock.release();
                }
            }
        }
    }

    private Boolean isProximitySensorNearby(final SensorEvent event) {
        float threshold = 4.001f; // <= 4 cm is near

        final float distanceInCm = event.values[0];
        final float maxDistance = event.sensor.getMaximumRange();
        Log.d(
                "[Manager] Proximity sensor report ["
                        + distanceInCm
                        + "] , for max range ["
                        + maxDistance
                        + "]");

        if (maxDistance <= threshold) {
            // Case binary 0/1 and short sensors
            threshold = maxDistance;
        }
        return distanceInCm < threshold;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.timestamp == 0) return;
        if (isProximitySensorNearby(event)) {
            if (!mProximityWakelock.isHeld()) {
                mProximityWakelock.acquire(10 * 60 * 1000L /*10 minutes*/);
            }
        } else {
            if (mProximityWakelock.isHeld()) {
                mProximityWakelock.release();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void enableDeviceRingtone(boolean use) {
        if (use) {
            mCore.setRing(null);
        } else {
            mCore.setRing(mRingSoundFile);
        }
    }

    public boolean getCallGsmON() {
        return mCallGsmON;
    }

    public void setCallGsmON(boolean on) {
        mCallGsmON = on;
        if (on && mCore != null) {
            mCore.pauseAllCalls();
        }
    }


}
