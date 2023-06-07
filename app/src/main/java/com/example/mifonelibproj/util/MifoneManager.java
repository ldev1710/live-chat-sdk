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
    private MyCallStateListener myCallStateListener;
    private Context mContext;
    //    private AndroidAudioManager mAudioManager;
//    private CallManager mCallManager;
    private final PowerManager mPowerManager;
    private final ConnectivityManager mConnectivityManager;
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;
    private WakeLock mProximityWakelock;
    private final SensorManager mSensorManager;
    private final Sensor mProximity;
    //    private final MediaScanner mMediaScanner;
    private Timer mTimer, mAutoAnswerTimer;
    private BroadcastReceiver mCallReceiver;
    private final MifonePreferences mPrefs;
    public static Core mCore;
    private CoreListenerStub mCoreListener;
    private AccountCreator mAccountCreator;
    private AccountCreatorListenerStub mAccountCreatorListener;
    private IntentFilter mCallIntentFilter;
    private boolean mExited;
    private boolean mCallGsmON;
    private boolean mProximitySensingEnabled;
    private boolean mHasLastCallSasBeenRejected;
    private Runnable mIterateRunnable;

    @RequiresApi(api = Build.VERSION_CODES.S)
    class MyCallStateListener extends TelephonyCallback implements TelephonyCallback.CallStateListener{
        @Override
        public void onCallStateChanged(int state) {
            android.util.Log.d("TAG", "onCallStateChanged: Changed"+state);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i("[Manager] Phone state is off hook");
                    setCallGsmON(true);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i("[Manager] Phone state is ringing");
                    setCallGsmON(true);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i("[Manager] Phone state is idle");
                    setCallGsmON(false);
                    break;
            }
        }
    }
    public void registerListenPhoneState(Context c){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
//            if(ContextCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
//                myCallStateListener = new MyCallStateListener();
//                mTelephonyManager.registerTelephonyCallback(c.getMainExecutor(), myCallStateListener);
//                android.util.Log.d("TAG", "MifoneManager: registed");
//            }
        } else {
            mPhoneStateListener =
                    new PhoneStateListener() {//For Android 10 or lower
                        @Override
                        public void onCallStateChanged(int state, String phoneNumber) {
                            switch (state) {
                                case TelephonyManager.CALL_STATE_OFFHOOK:
                                    Log.i("[Manager] Phone state is off hook");
                                    setCallGsmON(true);
                                    break;
                                case TelephonyManager.CALL_STATE_RINGING:
                                    Log.i("[Manager] Phone state is ringing");
                                    setCallGsmON(true);
                                    break;
                                case TelephonyManager.CALL_STATE_IDLE:
                                    Log.i("[Manager] Phone state is idle");
                                    setCallGsmON(false);
                                    break;
                            }
                        }
                    };
            Log.i("[Manager] Registering phone state listener");
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

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
        mConnectivityManager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mTelephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        mHasLastCallSasBeenRejected = false;
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
//                            MifoneUtils.dispatchOnUIThreadAfter(
//                                    () -> {
//                                        if (mCore != null) {
//                                            if (mCore.getCallsNb() > 0) {
//                                                mCallManager.acceptCall(call);
//                                                mAudioManager.routeAudioToEarPiece();
//                                            }
//                                        }
//                                    },
//                                    mPrefs.getAutoAnswerTime());
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
//                        if (MifoneContext.isReady()) {
//                            list.addListener(ContactsManager.getInstance());
//                        }
                    }

                    @Override
                    public void onFriendListRemoved(Core core, FriendList list) {
//                        list.removeListener(ContactsManager.getInstance());
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

    public synchronized void destroy() {
        destroyManager();
        // Wait for Manager to destroy everything before setting mExited to true
        // Otherwise some objects might crash during their own destroy if they try to call
        // MifoneManager.getCore(), for example to unregister a listener
        mExited = true;
    }

    public void restartCore() {
        Log.w("[Manager] Restarting Core");
        mCore.stop();
        mCore.start();
    }

    private void destroyCore() {
        Log.w("[Manager] Destroying Core");
        if (MifonePreferences.instance() != null) {
            // We set network reachable at false before destroying the Core
            // to not send a register with expires at 0
            if (MifonePreferences.instance().isPushNotificationEnabled()) {
                Log.w(
                        "[Manager] Setting network reachability to False to prevent unregister and allow incoming push notifications");
                mCore.setNetworkReachable(false);
            }
        }
        mCore.stop();
        mCore.removeListener(mCoreListener);
    }

    private synchronized void destroyManager() {
        Log.w("[Manager] Destroying Manager");
//        changeStatusToOffline();

        if (mTelephonyManager != null) {
            Log.i("[Manager] Unregistering phone state listener");
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

//        if (mCallManager != null) mCallManager.destroy();
//        if (mMediaScanner != null) mMediaScanner.destroy();
//        if (mAudioManager != null) mAudioManager.destroy();

        if (mTimer != null) mTimer.cancel();
        if (mAutoAnswerTimer != null) mAutoAnswerTimer.cancel();

        if (mCore != null) {
            // destroyCore();
            mCore = null;
        }
    }

    public void startLibLinphone(boolean isPush, CoreListenerStub listener) {
        try {
            android.util.Log.d(TAG, "onCreate: trigger");
            mCore =
                    Factory.instance()
                            .createCore(
                                    mPrefs.getMifoneDefaultConfig(),
                                    mPrefs.getMifoneFactoryConfig(),
//                                    null,
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
            // use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst
            // after cpu wake up
            mTimer = new Timer("Mifone scheduler");
            mTimer.schedule(lTask, 0, 20);

            configureCore();
        } catch (Exception e) {
            Log.e(e, "[Manager] Cannot start Mifone");
        }
        mCallIntentFilter = new IntentFilter("android.intent.action.ACTION_NEW_OUTGOING_CALL");
        mCallIntentFilter.setPriority(99999999);
//        mCallReceiver = new OutGoingCallReceive();
        try {
            mContext.registerReceiver(mCallReceiver, mCallIntentFilter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private synchronized void configureCore() {
        Log.i("[Manager] Configuring Core");
//        mAudioManager = new AndroidAudioManager(mContext);

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
        mAccountCreator.addListener(mAccountCreatorListener);
        mCallGsmON = false;

        Log.i("[Manager] Core configured");
    }

    /* Account linking */

    public AccountCreator getAccountCreator() {
        if (mAccountCreator == null) {
//            Log.w("[Manager] Account creator shouldn't be null !");
            mAccountCreator = mCore.createAccountCreator(null);
//            mAccountCreator = mCore.createAccountCreator(MifonePreferences.instance().getXmlrpcUrl());
            mAccountCreator.addListener(mAccountCreatorListener);
        }
        return mAccountCreator;
    }

    public void isAccountWithAlias() {
        if (mCore.getDefaultProxyConfig() != null) {
            long now = new Timestamp(new Date().getTime()).getTime();
            AccountCreator accountCreator = getAccountCreator();
            if (MifonePreferences.instance().getLinkPopupTime() == null
                    || Long.parseLong(MifonePreferences.instance().getLinkPopupTime()) < now) {
                accountCreator.reset();
                accountCreator.setUsername(
                        MifonePreferences.instance()
                                .getAccountUsername(
                                        MifonePreferences.instance().getDefaultAccountIndex()));
                accountCreator.isAccountExist();
            }
        } else {
            MifonePreferences.instance().setLinkPopupTime(null);
        }
    }

    private boolean isPresenceModelActivitySet() {
        if (mCore != null) {
            return mCore.getPresenceModel() != null
                    && mCore.getPresenceModel().getActivity() != null;
        }
        return false;
    }

//    public void changeStatusToOnline() {
//        if (mCore == null) return;
//        PresenceModel model = mCore.createPresenceModel();
//        model.setBasicStatus(PresenceBasicStatus.Open);
//        mCore.setPresenceModel(model);
//    }
//
//    public void changeStatusToOnThePhone() {
//        if (mCore == null) return;
//
//        if (isPresenceModelActivitySet()
//                && mCore.getPresenceModel().getActivity().getType()
//                        != PresenceActivity.Type.OnThePhone) {
//            mCore.getPresenceModel().getActivity().setType(PresenceActivity.Type.OnThePhone);
//        } else if (!isPresenceModelActivitySet()) {
//            PresenceModel model =
//                    mCore.createPresenceModelWithActivity(PresenceActivity.Type.OnThePhone, null);
//            mCore.setPresenceModel(model);
//        }
//    }
//
//    private void changeStatusToOffline() {
//        if (mCore != null) {
//            PresenceModel model = mCore.getPresenceModel();
//            model.setBasicStatus(PresenceBasicStatus.Closed);
//            mCore.setPresenceModel(model);
//        }
//    }
//
//    /* Tunnel stuff */
//
//    public void initTunnelFromConf() {
//        if (!mCore.tunnelAvailable()) return;
//
//        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
//        Tunnel tunnel = mCore.getTunnel();
//        tunnel.cleanServers();
//        TunnelConfig config = mPrefs.getTunnelConfig();
//        if (config.getHost() != null) {
//            tunnel.addServer(config);
//            manageTunnelServer(info);
//        }
//    }

    private boolean isTunnelNeeded(NetworkInfo info) {
        if (info == null) {
            Log.i("[Manager] No connectivity: tunnel should be disabled");
            return false;
        }

        String pref = mPrefs.getTunnelMode();

        if ("always".equals(pref)) {
            return true;
        }

        if (info.getType() != ConnectivityManager.TYPE_WIFI
                && "3G_only".equals(pref)) {
            Log.i("[Manager] Need tunnel: 'no wifi' connection");
            return true;
        }

        return false;
    }

//    private void manageTunnelServer(NetworkInfo info) {
//        if (mCore == null) return;
//        if (!mCore.tunnelAvailable()) return;
//        Tunnel tunnel = mCore.getTunnel();
//
//        Log.i("[Manager] Managing tunnel");
//        if (isTunnelNeeded(info)) {
//            Log.i("[Manager] Tunnel need to be activated");
//            tunnel.setMode(Tunnel.Mode.Enable);
//        } else {
//            Log.i("[Manager] Tunnel should not be used");
//            String pref = mPrefs.getTunnelMode();
//            tunnel.setMode(Tunnel.Mode.Disable);
//            if (getString(vn.mitek.mifone.R.string.tunnel_mode_entry_value_auto).equals(pref)) {
//                tunnel.setMode(Tunnel.Mode.Auto);
//            }
//        }
//    }

    /* Proximity sensor stuff */

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

    private String getString(int key) {
        return mContext.getString(key);
    }

    public void lastCallSasRejected(boolean rejected) {
        mHasLastCallSasBeenRejected = rejected;
    }
}