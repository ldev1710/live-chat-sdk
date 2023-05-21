package com.example.mifonelibproj.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;

public class DecodeAssistant {
    private static String TAG = "DecodeAssistant";
    private String dataHashed;

    public DecodeAssistant(String dataHashed) {
        this.dataHashed = dataHashed;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject decodeDataAssistant(){
        try {

            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedBytes = decoder.decode(dataHashed);
            String mDecoder1 = new String(decodedBytes);

            byte[] decodedBytes1 = decoder.decode(mDecoder1);
            String mDecode2 = new String(decodedBytes1);

            byte[] decodedBytes2 = decoder.decode(mDecode2);
            String mDecoder3 = new String(decodedBytes2);

            String[] items = mDecoder3.split("b6aed9ab7cdf85432c321757b4d48153");
            //stringbase64+regex+stringbase64+regex+stringbase64+regex+stringbase64+regex+stringbase64+regex+stringbase64
            for (int i = 0; i < items.length; i++) {
                byte[] decodedBytesDomain = decoder.decode(items[0]);
                byte[] decodedBytesPort = decoder.decode(items[1]);
                byte[] decodedBytesProxy = decoder.decode(items[2]);
                byte[] decodedBytesExtension = decoder.decode(items[3]);
                byte[] decodedBytesPassword = decoder.decode(items[4]);
                byte[] decodedBytesTransport = decoder.decode(items[5]);
                String domain = new String(decodedBytesDomain);
                String port = new String(decodedBytesPort);
                String proxy = new String(decodedBytesProxy);
                String extension = new String(decodedBytesExtension);
                String password = new String(decodedBytesPassword);
                String transport = new String(decodedBytesTransport);
                JSONObject json = new JSONObject();
                try {
                    JSONObject jsonObject = new JSONObject();
                    json.put("data", jsonObject);
                    jsonObject.put("domain", domain);
                    jsonObject.put("port", port);
                    jsonObject.put("proxy", proxy);
                    jsonObject.put("extension", extension);
                    jsonObject.put("password", password);
                    jsonObject.put("transport", transport);
                    Log.d("ASSISTANT", json.toString());
                    return json;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Log.e(TAG, "decodeDataAssistant: "+message);
            return null;
        }
        return null;
    }
}
