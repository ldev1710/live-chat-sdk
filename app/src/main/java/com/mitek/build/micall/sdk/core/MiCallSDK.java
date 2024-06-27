package com.mitek.build.micall.sdk.core;

import android.content.Context;
import android.media.AudioManager;

import com.mitek.build.micall.sdk.listener.publisher.MiCallStateListener;
import com.mitek.build.micall.sdk.model.Call;
import com.mitek.build.micall.sdk.model.CallStateEnum;
import com.mitek.build.micall.sdk.model.RegistrationStateEnum;
import com.mitek.build.micall.sdk.core.account.MiCallAccount;
import com.mitek.build.micall.sdk.util.MiCallLog;
import com.mitek.build.micall.sdk.util.MiCallNormalize;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.ArrayList;
import java.util.List;


class MiCallSDK {
    private static String apiKey;
    private static Endpoint ep = new Endpoint();
    private static AccountSDK accountSDK = new AccountSDK();
    private static CallSDK callSDK;
    private static MiCallAccount currAccount;
    private static AccountConfig acf = new AccountConfig();
    private static List<MiCallStateListener> observe;
    static private boolean isInitialized = false;
    static private boolean isRegistered = false;

    static void init(String apiKey){
        MiCallSDK.apiKey = apiKey;
        observe = new ArrayList<>();
        try {
            ep.libCreate();
            EpConfig epConfig = new EpConfig();
            epConfig.getLogConfig().setLevel(4);
            epConfig.getLogConfig().setConsoleLevel(4);
            epConfig.getUaConfig().setUserAgent("MiCall-SDK");
            ep.libInit(epConfig);
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setPort(5969);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,transportConfig);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,transportConfig);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,transportConfig);
            ep.libStart();
            currAccount = new MiCallAccount(
                    "pbx57.mipbx.vn",
                    "sipproxy01-2020.mipbx.vn",
                    "5969",
                    "1995",
                    "f4935275fe3f745c7daff955f0097075"
            );
            isInitialized = true;
        } catch (Exception e) {
            MiCallLog.logE(e.getMessage());
        }
    }

    private static boolean interValidate(){
        if(!isInitialized || !isRegistered){
            MiCallLog.logE("The library is not ready");
            return false;
        }
        return true;
    }

    public static void unRegister(){
        try {
            if(interValidate()) accountSDK.setRegistration(false);
        } catch (Exception e) {
            MiCallLog.logE(e.getMessage());
        }
    }

    public static void toggleMute(boolean mute){
        if(!interValidate()) return;
        try {
            CallInfo info = callSDK.getInfo();
            for (int i = 0; i < info.getMedia().size(); i++) {
                Media media = callSDK.getMedia(i);
                CallMediaInfo mediaInfo = info.getMedia().get(i);

                if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                        && media != null
                        && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);
                    try {
                        AudDevManager mgr = ep.audDevManager();
                        if (mute) {
                            mgr.getCaptureDevMedia().stopTransmit(audioMedia);
                        } else {
                            mgr.getCaptureDevMedia().startTransmit(audioMedia);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            observingCallState(mute ? CallStateEnum.MUTE : CallStateEnum.UN_MUTE,callSDK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void hold(){
        if(!interValidate()) return;
        CallOpParam prm = new CallOpParam(true);
        try {
            callSDK.setHold(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unHold(){
        if(!interValidate()) return;
        CallOpParam prm = new CallOpParam(true);
        prm.getOpt().setFlag(1);
        try {
            callSDK.reinvite(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Endpoint getEp(){
        return ep;
    }

    public static void addMiCallListener(MiCallStateListener listener){
        if(observe==null) observe = new ArrayList<>();
        observe.add(listener);
    }

    public static Call getCurrentCall(){
        if(!interValidate()) return null;
        try {
            return new Call(
                    callSDK.getId(),
                    MiCallNormalize.normalizeRemoteUri(callSDK.getInfo().getRemoteUri())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void makeCall(String phone){
        if(!interValidate()) return;
        callSDK = new CallSDK(accountSDK,-1);
        CallOpParam param = new CallOpParam(true);
        String buddyUri = "sip:"+phone+"@"+currAccount.getDomain();
        try {
            callSDK.makeCall(buddyUri,param);
        } catch (Exception e) {
            callSDK.delete();
            MiCallLog.logE(e.getMessage());
        }
    }

    static void toggleSpeaker(Context context,boolean isEnable){
        if(!interValidate()) return;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(isEnable);
    }

    static void decline(){
        if(!interValidate()) return;
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
        try {
            callSDK.answer(param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void hangup(){
        if(!interValidate()) return;
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED);
        try {
            callSDK.hangup(param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void answer(){
        if(!interValidate()) return;
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            callSDK.answer(param);
        } catch (Exception e) {
            MiCallLog.logE(e.getMessage());
        }
    }

    public static void observingCallState(CallStateEnum callStateEnum, CallSDK newCall){
        callSDK = newCall;
        try {
            Call call = new Call(
                    newCall.getId(),
                    MiCallNormalize.normalizeRemoteUri(newCall.getInfo().getRemoteUri())
            );
            MiCallLog.logI("onCallStateChanged: "+callStateEnum.toString());
            for(MiCallStateListener ob : MiCallSDK.observe){
                ob.onCallStateChanged(callStateEnum,call);
            }
        } catch (Exception ignored) {
        }
    }

    public static void observingRegState(OnRegStateParam prm){
        String message;
        RegistrationStateEnum stateEnum;
        if (prm.getExpiration() == 0L) {
            message = "Un-registration: ";
            if (prm.getCode() == 200) {
                message += "Successful";
                stateEnum = RegistrationStateEnum.UNREGISTERED;
            } else {
                message += "Failed";
                stateEnum = RegistrationStateEnum.UNREGISTERED_FAILED;
            }
        } else {
            message = "Registration: ";
            if (prm.getCode() == 200) {
                message += "Successful";
                stateEnum = RegistrationStateEnum.REGISTERED;
                isRegistered = true;
            } else {
                message += "Failed";
                stateEnum = RegistrationStateEnum.REGISTER_FAILED;
            }
        }
        message += "(" + prm.getReason() + ")";
        MiCallLog.logI(message);
        for(MiCallStateListener ob : MiCallSDK.observe){
            ob.onRegistrationStateChanged(stateEnum,message);
        }
    }
    public static void register(){
        if(!isInitialized){
            MiCallLog.logE("The library is not ready");
            return;
        }
        String accountId = "sip:"+currAccount.getUsername()+"@"+currAccount.getDomain();
        String registrar = "sip:"+ currAccount.getDomain();
        String proxy = "sip:"+ currAccount.getProxy()+":"+ currAccount.getPort();
        String username = currAccount.getUsername();
        String password = currAccount.getPassword();
        acf.setIdUri(accountId);
        acf.getRegConfig().setRegistrarUri(registrar);
        acf.getSipConfig().getProxies().clear();
        acf.getSipConfig().getProxies().add(proxy);
        acf.getSipConfig().getAuthCreds().clear();
        acf.getSipConfig().getAuthCreds().add(new AuthCredInfo(
                "Digest", "*", username, 0,
                password
        ));
        acf.getNatConfig().setIceEnabled(true);
        acf.getCallConfig().setTimerMinSESec(90);
        acf.getCallConfig().setTimerSessExpiresSec(120);
        acf.getVideoConfig().setAutoTransmitOutgoing(true);
        acf.getVideoConfig().setAutoShowIncoming(true);
        try {
            accountSDK.create(acf);
            accountSDK.modify(acf);
        } catch (Exception e) {
            accountSDK.delete();
            MiCallLog.logE(e.getMessage());
        }
    }
}
