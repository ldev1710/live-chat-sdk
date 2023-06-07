package com.example.mifonelibproj.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import org.linphone.core.tools.Log;

public class BluetoothReceiver extends BroadcastReceiver {
    public BluetoothReceiver() {
        super();
        Log.i("[Bluetooth] Bluetooth receiver created");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("[Bluetooth] Bluetooth broadcast received");
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.w("[Bluetooth] Adapter has been turned off");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.w("[Bluetooth] Adapter is being turned off");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i("[Bluetooth] Adapter has been turned on");
//                    MifoneManager.getAudioManager().bluetoothAdapterStateChanged();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i("[Bluetooth] Adapter is being turned on");
                    break;
                case BluetoothAdapter.ERROR:
                    Log.e("[Bluetooth] Adapter is in error state !");
                    break;
                default:
                    Log.w("[Bluetooth] Unknown adapter state: ", state);
                    break;
            }
        } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
            int state =
                    intent.getIntExtra(
                            BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
            if (state == BluetoothHeadset.STATE_CONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset connected");
//                MifoneManager.getAudioManager().bluetoothHeadetConnectionChanged(true);
            } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset disconnected");
//                MifoneManager.getAudioManager().bluetoothHeadetConnectionChanged(false);
            } else {
                Log.w("[Bluetooth] Bluetooth headset unknown state changed: " + state);
            }
        } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
            int state =
                    intent.getIntExtra(
                            BluetoothHeadset.EXTRA_STATE,
                            BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
            if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset audio connected");
//                MifoneManager.getAudioManager().bluetoothHeadetAudioConnectionChanged(true);
            } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset audio disconnected");
//                MifoneManager.getAudioManager().bluetoothHeadetAudioConnectionChanged(false);
            } else if (state == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                Log.i("[Bluetooth] Bluetooth headset audio connecting");
            } else {
                Log.w("[Bluetooth] Bluetooth headset unknown audio state changed: " + state);
            }
        } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
            int state =
                    intent.getIntExtra(
                            AudioManager.EXTRA_SCO_AUDIO_STATE,
                            AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset SCO connected");
//                MifoneManager.getAudioManager().bluetoothHeadetScoConnectionChanged(true);
            } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                Log.i("[Bluetooth] Bluetooth headset SCO disconnected");
//                MifoneManager.getAudioManager().bluetoothHeadetScoConnectionChanged(false);
            } else if (state == AudioManager.SCO_AUDIO_STATE_CONNECTING) {
                Log.i("[Bluetooth] Bluetooth headset SCO connecting");
            } else if (state == AudioManager.SCO_AUDIO_STATE_ERROR) {
                Log.i("[Bluetooth] Bluetooth headset SCO connection error");
            } else {
                Log.w("[Bluetooth] Bluetooth headset unknown SCO state changed: " + state);
            }
        } else if (action.equals(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)) {
            String command =
                    intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
            int type =
                    intent.getIntExtra(
                            BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, -1);

            String commandType;
            switch (type) {
                case BluetoothHeadset.AT_CMD_TYPE_ACTION:
                    commandType = "AT Action";
                    break;
                case BluetoothHeadset.AT_CMD_TYPE_READ:
                    commandType = "AT Read";
                    break;
                case BluetoothHeadset.AT_CMD_TYPE_TEST:
                    commandType = "AT Test";
                    break;
                case BluetoothHeadset.AT_CMD_TYPE_SET:
                    commandType = "AT Set";
                    break;
                case BluetoothHeadset.AT_CMD_TYPE_BASIC:
                    commandType = "AT Basic";
                    break;
                default:
                    commandType = "AT Unknown";
                    break;
            }
            Log.i("[Bluetooth] Vendor action " + commandType + " : " + command);
        } else {
            Log.w("[Bluetooth] Bluetooth unknown action: " + action);
        }
    }
}