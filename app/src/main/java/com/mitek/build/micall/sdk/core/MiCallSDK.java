package com.mitek.build.micall.sdk.core;

import android.util.Log;

import com.mitek.build.micall.sdk.listener.publisher.MiCallStateListener;
import com.mitek.build.micall.sdk.model.RegistrationStateEnum;
import com.mitek.build.micall.sdk.model.account.MiCallAccount;
import com.mitek.build.micall.sdk.util.MiCallLog;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.util.ArrayList;
import java.util.List;

public class MiCallSDK {
    private static String apiKey;
    private static boolean isAvailable = false;
    private static Endpoint ep = new Endpoint();
    private static AccountSDK accountSDK = new AccountSDK();
    private static CallSDK callSDK;
    private static MiCallAccount currAccount;
    private static AccountConfig acf = new AccountConfig();
    static private List<MiCallStateListener> observe;
    static void init(String apiKey){
        MiCallSDK.apiKey = apiKey;
        observe = new ArrayList<>();
        try {
            ep.libCreate();
            EpConfig epConfig = new EpConfig();
            epConfig.getLogConfig().setLevel(4);
            epConfig.getLogConfig().setConsoleLevel(4);
            epConfig.getUaConfig().setUserAgent("MiCall SDK");
            ep.libInit(epConfig);
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setPort(5969);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,transportConfig);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,transportConfig);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,transportConfig);
            ep.libStart();
        } catch (Exception e) {
            MiCallLog.logE(e.getMessage());
        }
    }

    public static Endpoint getEp(){
        return ep;
    }

    static void addMiCallListener(MiCallStateListener listener){
        if(observe==null) observe = new ArrayList<>();
        observe.add(listener);
    }

    static void makeCall(String phone){
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

    static void hangup(){
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        try {
            callSDK.answer(param);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            } else {
                message += "Failed";
                stateEnum = RegistrationStateEnum.REGISTER_FAILED;
            }
        }
        message += "(" + prm.getReason() + ")";
        for(MiCallStateListener ob : MiCallSDK.observe){
            ob.onRegistrationStateChanged(stateEnum,message);
        }
    }
    static void register(MiCallAccount acc){
        currAccount = acc;
        String accountId = "sip:"+acc.getUsername()+"@"+acc.getDomain();
        String registrar = "sip:"+ acc.getDomain();
        String proxy = "sip:"+ acc.getProxy()+":"+ acc.getPort();
        String username = acc.getUsername();
        String password = acc.getPassword();
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
        acf.getCallConfig().setTimerUse(15);
        acf.getCallConfig().setTimerSessExpiresSec(90);
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
