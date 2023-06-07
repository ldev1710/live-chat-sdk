package com.example.mifonelibproj.core;

import static com.example.mifonelibproj.util.MifoneManager.mCore;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mifonelibproj.api.Common;
import com.example.mifonelibproj.api.IResponseAPIs;
import com.example.mifonelibproj.call.CallManager;
import com.example.mifonelibproj.listener.MifoneCoreListener;
import com.example.mifonelibproj.model.other.ConfigMifoneCore;
import com.example.mifonelibproj.model.other.Privileges;
import com.example.mifonelibproj.model.other.ProfileUser;
import com.example.mifonelibproj.model.other.State;
import com.example.mifonelibproj.model.other.UpdateTokenFirebase;
import com.example.mifonelibproj.model.other.User;
import com.example.mifonelibproj.model.response.APIsResponse;
import com.example.mifonelibproj.receiver.MyAlarmReceiver;
import com.example.mifonelibproj.util.DecodeAssistant;
import com.example.mifonelibproj.util.MifoneContext;
import com.example.mifonelibproj.util.MifoneManager;
import com.example.mifonelibproj.util.MifonePreferences;
import com.example.mifonelibproj.util.Permission;
import com.example.mifonelibproj.util.SharePrefUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.AccountCreator;
import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.CallLog;
import org.linphone.core.CallStats;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Event;
import org.linphone.core.GlobalState;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MifoneCoreHandle{
    private static MifoneCoreHandle mInstance;
    private static CoreListenerStub mListener;
    private static MifoneCoreListener mifoneCoreListener;
    private static IResponseAPIs iResponseAPIs;
    private static Context mContext;
    private static User mUser;
    private static ConfigMifoneCore mConfigMifoneCore;
    private static final String defaultDomain = "mifone.vn/mitek";
    public static final String TAG = "DEBUGLISTENER";
    private MifoneCoreHandle(ConfigMifoneCore configMifoneCore,User user) {
//        mUser = new User("hieu@mitek2020.vn","Hieu@mitek2020.vn","sf");
        mUser = user;
        mConfigMifoneCore = configMifoneCore;

    }

    public static void initMifoneCore(Context context, ConfigMifoneCore configMifoneCore,User user) {
        mInstance = new MifoneCoreHandle(configMifoneCore,user);
        iResponseAPIs = Common.getAPIs();
        mContext = context;
        new MifoneContext(mContext);
        mListener = new CoreListenerStub() {

            @Override
            public void onCallStatsUpdated(Core lc, org.linphone.core.Call call, CallStats stats) {
                super.onCallStatsUpdated(lc, call, stats);
                Log.d(TAG, "onCallStatsUpdated: call: "+call.getState().toInt()+", state: "+stats.getIceState().toInt());
            }

            @Override
            public void onTransferStateChanged(Core lc, org.linphone.core.Call transfered, org.linphone.core.Call.State newCallState) {
                super.onTransferStateChanged(lc, transfered, newCallState);
                Log.d(TAG, "onTransferStateChanged: transfer state: "+transfered.getTransferState().toInt()+", call state: "+newCallState.toInt());
            }

            @Override
            public void onCallStateChanged(Core core, org.linphone.core.Call call, org.linphone.core.Call.State state, String message) {
                Log.d(TAG, "onCallStateChanged: "+message);
                State stateMifone = new State(state);
                MifoneCoreHandle.mifoneCoreListener.onCallStateChanged(stateMifone,message);
            }

            @Override
            public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
                super.onRegistrationStateChanged(lc, cfg, cstate, message);
                Log.d(TAG, "onRegistrationStateChanged: "+message+", "+cstate);
                com.example.mifonelibproj.model.other.RegistrationState registrationStateMifone = new com.example.mifonelibproj.model.other.RegistrationState(cstate.toInt());
                MifoneCoreHandle.mifoneCoreListener.onRegistrationStateChanged(registrationStateMifone,message);
            }
        };
        MifoneManager.getInstance().startLibLinphone(true,mListener);
        mCore.enableMic(true);
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long timeInMillis = System.currentTimeMillis() + mConfigMifoneCore.getExpire() * 1000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        MifonePreferences.instance().setContext(context);
    }

    public static String getNumbPhoneCallIn(){
        return mCore.getCurrentCall().getRemoteAddress().getUsername();
    }

    public static void acceptCall(){
        org.linphone.core.Call call = mCore.getCurrentCall();
        org.linphone.core.CallParams callParams =  mCore.createCallParams(call);
        MifoneManager.mCore.acceptCallWithParams(call,callParams);
    }
    public static void registerListener(MifoneCoreListener mifoneCoreListener){
        MifoneCoreHandle.mifoneCoreListener = mifoneCoreListener;
    }

    public static void cancelCall(){
        CallManager callManager = new CallManager(mContext);
        callManager.cancelCall();
    }
    public static void holdCall(){
        mCore.getCurrentCall().pause();
    }

    public static void resumeCall(){
        org.linphone.core.Call[] calls = mCore.getCalls();
        for(org.linphone.core.Call call : calls){
            if(call.getState() == org.linphone.core.Call.State.Paused) {
                call.resume();
                return;
            }
        }
    }
    public static void transfer(String phoneNumber){
        mCore.getCurrentCall().transfer(phoneNumber);
    }
    public static void configMifoneCore(){
        if (mUser==null){
            mifoneCoreListener.onResultConfigAccount(false,"Email and password of user not be configured");
            return;
        }
        if(mInstance==null){
            mifoneCoreListener.onResultConfigAccount(false,"MifoneCore not be initialized");
            return;
        }
        iResponseAPIs.isLoginData(mUser.getUsername(), mUser.getPassword(), mUser.getType())
                .enqueue(new Callback<APIsResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(Call<APIsResponse> call, Response<APIsResponse> response) {
                        APIsResponse result = response.body();
                        if (response.isSuccessful()) {
                            assert result != null;
                            Log.d(TAG, "res code: "+result);
                            if (result.getCode() == 200) {
                                String secret = result.getSecret();
                                signIn(result, secret);
                                Common.groupId = result.getGroupId();
                                String user_log_id = result.getUser_log_id();
                                List<Privileges> arrayPrivileges = result.getPrivileges();
                                mifoneCoreListener.onResultConfigAccount(true,"Config Successful!");
                            } else {
                                mifoneCoreListener.onResultConfigAccount(false,"Username and Password are wrong! Please check again, "+result.getMessage());
                            }
                        } else {
                            mifoneCoreListener.onResultConfigAccount(false,"Email or password not precision");
                        }
                    }

                    @Override
                    public void onFailure(Call<APIsResponse> call, Throwable t) {
                        Log.e("Error ", t.getMessage());
                        mifoneCoreListener.onResultConfigAccount(false,"Login fail. Please try again");
                    }
                });
    }

    public static void callOut(String phoneNumber){
        Permission permission = new Permission(mContext);
        if(!permission.checkPermissions(new String[]{Manifest.permission.RECORD_AUDIO})) {
            mifoneCoreListener.onError("RECORD_AUDIO permission not granted");
            return;
        }
        CallManager callManager = new CallManager(mContext);
        callManager.newOutgoingCall(phoneNumber);
    }

    public static MifoneCoreHandle getInstance(){
        if(mInstance==null){
            Log.e("[Mifone Core]", "Mifone core not initialized");
            return null;
        }
        return mInstance;
    }

    private static void reloadDefaultAccountCreatorConfig() {
        org.linphone.core.tools.Log.i("[Assistant] Reloading configuration with default");
        reloadAccountCreatorConfig(MifonePreferences.instance().getDefaultDynamicConfigFile());
    }
    private static AccountCreator getAccountCreator() {
        return MifoneManager.getInstance().getAccountCreator();
    }
    private static void reloadAccountCreatorConfig(String path) {
        Core core = mCore;
        if (core != null) {
            core.loadConfigFromXml(path);
            AccountCreator accountCreator = getAccountCreator();
            accountCreator.reset();
            accountCreator.setLanguage(Locale.getDefault().getLanguage());
        }
    }

    public static void sendDtms(int numb){
        mCore.getCurrentCall().sendDtmf(String.valueOf(numb).charAt(0));
    }

    public static void declineCall(){
        org.linphone.core.Call[] calls = mCore.getCalls();
        for(org.linphone.core.Call call : calls){
            if(call.getState().toInt() == State.IncomingEarlyMedia || call.getState().toInt() == State.IncomingReceived){
                call.terminate();
                return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void signIn(APIsResponse result, String secret) {
        Core core = mCore;
        if (core != null) {
            Log.d("TAG", "signIn: reloaded!@");
            reloadDefaultAccountCreatorConfig();
        }

        if (result.getData() == "" || result.getData() == null) {
            //"Information does not exist, \nPlease check again"
        } else {
            DecodeAssistant decodeAssistant = new DecodeAssistant(result.getData());
            JSONObject json = decodeAssistant.decodeDataAssistant();
            if(json == null) {
                //"Unable to access information, \n Please check again!"
                return;
            }
            configMifoneProd(json, secret);
        }
    }


    private static void createProxyConfigAndLeaveAssistant(boolean isGenericAccount) {
        boolean useMiphoneDefaultValues = defaultDomain.equals(getAccountCreator().getDomain());
        if (isGenericAccount) {
            if (useMiphoneDefaultValues) {
                org.linphone.core.tools.Log.i(
                        "[Assistant] Default domain found for generic connection, reloading configuration");
                mCore.loadConfigFromXml(MifonePreferences.instance().getMifoneDynamicConfigFile());
            } else {
                org.linphone.core.tools.Log.i("[Assistant] Third party domain found, keeping default values");
            }
        }
        String server = Common.curentUser.getProxy() + ":" + Common.curentUser.getPort();
        ProxyConfig proxyConfig = getAccountCreator().createProxyConfig();
        Log.d(TAG, "createProxyConfigAndLeaveAssistant: "+proxyConfig.setServerAddr(server));

        if (isGenericAccount) {
            if (useMiphoneDefaultValues) {
                // Restore default values
                org.linphone.core.tools.Log.i("[Assistant] Restoring default assistant configuration");
                mCore.loadConfigFromXml(MifonePreferences.instance().getDefaultDynamicConfigFile());
            } else {

                if (proxyConfig != null) {
                    proxyConfig.setPushNotificationAllowed(false);
                }
                org.linphone.core.tools.Log.w(
                        "[Assistant] Unknown domain used, push probably won't work, enable service mode");
                MifonePreferences.instance().setServiceNotificationVisibility(true);
//                MifoneContext.instance().getNotificationManager().startForeground();
                String mProxy = proxyConfig.getServerAddr();
                proxyConfig.setRoute(mProxy);
            }
        }
        mCore.addProxyConfig(proxyConfig);
        mCore.setDefaultProxyConfig(proxyConfig);
//        MifoneManager.mCore.start();
        Log.d(TAG, "createProxyConfigAndLeaveAssistant: started");
        MifonePreferences.instance().firstLaunchSuccessful();
        MifonePreferences.instance()
                .setPushNotificationEnabled(
                        MifonePreferences.instance().isPushNotificationEnabled());
    }

    private static void configureAccountMifone(ProfileUser data, String secret) {
        Common.curentUser = data;
        AccountCreator accountCreator = getAccountCreator();
        accountCreator.setUsername(data.getExtension());
        accountCreator.setPassword(data.getPassword());
        accountCreator.setDomain(data.getDomain());
        if (data.getTransport().equals("TLS")) {
            accountCreator.setTransport(TransportType.Tls);
        } else if (data.getTransport().equals("UDP")) {
            accountCreator.setTransport(TransportType.Udp);
        } else {
            accountCreator.setTransport(TransportType.Tcp);
        }
        createProxyConfigAndLeaveAssistant(true);
        String token = MifonePreferences.instance().getPushNotificationRegistrationID();
        String provider = "android";
        iResponseAPIs.isUpdateTokenFirebase(token, secret, data.getExtension(), provider)
                .enqueue(new Callback<UpdateTokenFirebase>() {
                    @Override
                    public void onResponse(Call<UpdateTokenFirebase> call, Response<UpdateTokenFirebase> response) {
                        org.linphone.core.tools.Log.d("TAG  ", response.body());
                    }

                    @Override
                    public void onFailure(Call<UpdateTokenFirebase> call, Throwable t) {
                        org.linphone.core.tools.Log.d("TAG  ", t.getMessage());
                    }
                });
    }

    private static void configMifoneProd(JSONObject json, String secret) {
        try {
            JSONObject jsonObject = json.getJSONObject("data");
            ProfileUser dataRes =
                    new ProfileUser(
                            jsonObject.getString("extension"),
                            jsonObject.getString("password"),
                            jsonObject.getString("domain"),
                            jsonObject.getString("proxy"),
                            jsonObject.getString("port"),
                            jsonObject.getString("transport"));
            SharePrefUtils.getInstance().put("ProfileUser", dataRes);
            configureAccountMifone(dataRes, secret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}