package com.mitek.build.micall.sdk.core;

import static com.mitek.build.micall.sdk.core.MiCallSDK.observingCallState;
import static com.mitek.build.micall.sdk.core.MiCallSDK.observingRegState;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStartedParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pjsip_status_code;
import com.mitek.build.micall.sdk.model.CallStateEnum;
import com.mitek.build.micall.sdk.util.MiCallLog;

public class AccountSDK extends Account {
    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        MiCallLog.logI("onIncomingCall: "+prm.getCallId());
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
        try {
            CallSDK callSDK = new CallSDK(this, prm.getCallId());
            callSDK.answer(param);
            observingCallState(CallStateEnum.INCOMING,callSDK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        super.onRegState(prm);
        observingRegState(prm);
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        super.onRegStarted(prm);
        observingRegState(prm);
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        super.onInstantMessage(prm);
        MiCallLog.logI("onInstantMessage: "+prm.getMsgBody());
    }
}
