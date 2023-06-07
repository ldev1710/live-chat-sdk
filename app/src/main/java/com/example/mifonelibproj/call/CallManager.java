package com.example.mifonelibproj.call;

import android.content.Context;


import com.example.mifonelibproj.util.MifoneManager;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.tools.Log;

public class CallManager {
    private Context mContext;
    private Call mCall;
    public CallManager(Context context) {
        mContext = context;
    }

    public void cancelCall(){
        for (Call call : MifoneManager.mCore.getCalls()) {
            Call.State cstate = call.getState();
            if (Call.State.OutgoingInit == cstate
                    || Call.State.OutgoingProgress == cstate
                    || Call.State.OutgoingRinging == cstate
                    || Call.State.OutgoingEarlyMedia == cstate) {
                mCall = call;
                break;
            }
        }
        if(mCall==null) mCall = MifoneManager.mCore.getCurrentCall();
        mCall.terminate();
    }

    public void newOutgoingCall(String phoneNumber) {
        if (phoneNumber == null) return;
        Core core = MifoneManager.mCore;
        Address address;
        assert core != null;
        address = core.interpretUrl(phoneNumber); // InterpretUrl does normalizePhoneNumber
        if (address == null) {
            Log.e("[Call Manager] Couldn't convert to String to Address : " + phoneNumber);
            return;
        }

        ProxyConfig lpc = core.getDefaultProxyConfig();
        if (lpc != null && address.weakEqual(lpc.getIdentityAddress())) {
            return;
        }
        inviteAddress(address);
    }

    private void inviteAddress(Address address) {
        Core core = MifoneManager.mCore;

        CallParams params = core.createCallParams(null);

        core.inviteAddressWithParams(address, params);
    }
}