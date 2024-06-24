package com.test.excute.core;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;

public class CallSDK extends Call {
    public CallSDK(Account acc, int call_id) {
        super(acc, call_id);
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        super.onCallState(prm);
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        super.onCallMediaState(prm);
    }
}
