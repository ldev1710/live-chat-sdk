package com.mitek.build.micall.sdk.core;

import static com.mitek.build.micall.sdk.core.MiCallSDK.observingCallState;

import com.mitek.build.micall.sdk.model.CallStateEnum;
import com.mitek.build.micall.sdk.util.MiCallLog;

import com.mitek.build.micall.sdk.core.module.model.Account;
import com.mitek.build.micall.sdk.core.module.model.AudDevManager;
import com.mitek.build.micall.sdk.core.module.model.AudioMedia;
import com.mitek.build.micall.sdk.core.module.model.Call;
import com.mitek.build.micall.sdk.core.module.model.CallMediaInfo;
import com.mitek.build.micall.sdk.core.module.model.CallMediaInfoVector;
import com.mitek.build.micall.sdk.core.module.model.Media;
import com.mitek.build.micall.sdk.core.module.model.OnCallMediaStateParam;
import com.mitek.build.micall.sdk.core.module.model.OnCallStateParam;
import com.mitek.build.micall.sdk.core.module.model.pjmedia_type;
import com.mitek.build.micall.sdk.core.module.model.pjsip_inv_state;
import com.mitek.build.micall.sdk.core.module.model.pjsua_call_media_status;

public class CallSDK extends Call {
    public CallSDK(Account acc, int call_id) {
        super(acc, call_id);
    }
    @Override
    public void onCallState(OnCallStateParam prm) {
        try {
//            MiCallLog.logI("onCallState raw: "+getInfo().getState());
            switch (getInfo().getState()){
                case pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED:
                    observingCallState(CallStateEnum.END,this);
                    break;
                case pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED:
                    observingCallState(CallStateEnum.ACCEPTED,this);
                    break;
                case pjsip_inv_state.PJSIP_INV_STATE_CONNECTING:
                    observingCallState(CallStateEnum.CONNECTING,this);
                    break;
                case pjsip_inv_state.PJSIP_INV_STATE_EARLY:
                    observingCallState(CallStateEnum.EARLY,this);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean isHolding = false;

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        try {
            CallMediaInfoVector cmiv = getInfo().getMedia();
            for (int i = 0 ; i<cmiv.size();++i){
                CallMediaInfo cmi = cmiv.get(i);
//                MiCallLog.logI(cmi.getStatus()+"");
                if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO){
                    Media m = getMedia(i);
                    AudioMedia am = AudioMedia.typecastFromMedia(m);
                    try{
                        AudDevManager manager = MiCallSDK.getEp().audDevManager();
                        am.startTransmit(manager.getPlaybackDevMedia());
                        manager.getCaptureDevMedia().startTransmit(am);
                        observingCallState(CallStateEnum.STREAMING,this);
                    } catch (Exception ignored){
                    }
                }
                if(cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD
                        || cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD){
                    isHolding = true;
                    observingCallState(CallStateEnum.HOLD,this);
                }
                if(cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE){
                    if(isHolding){
                        isHolding = false;
                        observingCallState(CallStateEnum.UN_HOLD,this);
                    }
                }
            }
        } catch (Exception e) {
            MiCallLog.logE(e.getMessage());
        }
    }
}
