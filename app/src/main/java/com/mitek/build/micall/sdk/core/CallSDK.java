package com.mitek.build.micall.sdk.core;

import com.mitek.build.micall.sdk.util.MiCallLog;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.List;

public class CallSDK extends Call {
    public CallSDK(Account acc, int call_id) {
        super(acc, call_id);
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        MiCallLog.logI(prm.toString());
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        try {
            CallMediaInfoVector cmiv = getInfo().getMedia();
            for (int i = 0 ; i<cmiv.size();++i){
                CallMediaInfo cmi = cmiv.get(i);
                if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                        (cmi.getStatus() ==
                                pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                                cmi.getStatus() ==
                                        pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)
                ){
                    Media m = getMedia(i);
                    AudioMedia am = AudioMedia.typecastFromMedia(m);
                    try{
                        MiCallSDK.getEp().audDevManager().getCaptureDevMedia().startTransmit(am);
                    } catch (Exception e){
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
