package com.test.excute.listener.observe;

import com.test.excute.model.CallStateEnum;
import com.test.excute.model.RegistrationStateEnum;

public interface MiCallObserve {
    void onRegistrationStateChanged(RegistrationStateEnum state, String message);
    void onCallStateChanged(CallStateEnum state, String message);
}
