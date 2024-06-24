package com.mitek.build.micall.sdk.core;

import static com.mitek.build.micall.sdk.core.MiCallSDK.observingRegState;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;

public class AccountSDK extends Account {
    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        super.onRegState(prm);
        observingRegState(prm);
    }

}
