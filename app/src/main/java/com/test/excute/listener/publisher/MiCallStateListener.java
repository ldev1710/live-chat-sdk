package com.test.excute.listener.publisher;

import com.test.excute.listener.observe.MiCallObserve;
import com.test.excute.model.CallStateEnum;
import com.test.excute.model.RegistrationStateEnum;

public class MiCallStateListener implements MiCallObserve {
    @Override
    public void onRegistrationStateChanged(RegistrationStateEnum state, String message) {

    }

    @Override
    public void onCallStateChanged(CallStateEnum state, String message) {

    }
}
