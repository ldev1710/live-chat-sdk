package com.mitek.build.micall.sdk.core;

import static com.mitek.build.micall.sdk.core.MiCallSDK.observingRegState;

import android.util.Log;

import com.mitek.build.micall.sdk.util.MiCallLog;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.concurrent.TimeUnit;

public class AccountSDK extends Account {
    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        MiCallLog.logI("onIncomingCall: "+prm.getCallId());
        Call call = new Call(this,prm.getCallId());
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
        try {
            call.answer(param);
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
    public void onInstantMessage(OnInstantMessageParam prm) {
        super.onInstantMessage(prm);
        Log.d("TAG", "onInstantMessage: "+prm.getMsgBody());
    }
}
