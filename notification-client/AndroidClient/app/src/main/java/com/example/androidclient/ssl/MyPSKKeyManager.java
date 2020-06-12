package com.example.androidclient.ssl;

import android.util.Base64;

import com.example.androidclient.Crypto;
import com.example.androidclient.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLEngine;

@SuppressWarnings("deprecation")
public class MyPSKKeyManager implements org.conscrypt.PSKKeyManager {
    @Override
    public String chooseServerKeyIdentityHint(Socket socket) {
        System.out.println("chooseServerKeyIdentityHint: socket");
        return null;
    }

    @Override
    public String chooseServerKeyIdentityHint(SSLEngine engine) {
        System.out.println("chooseServerKeyIdentityHint: engine");
        return null;
    }

    @Override
    public String chooseClientKeyIdentity(String identityHint, Socket socket) {
        System.out.println("chooseClientKeyIdentity: socket");
        return "Client_identity";
    }

    @Override
    public String chooseClientKeyIdentity(String identityHint, SSLEngine engine) {
        System.out.println("chooseClientKeyIdentity: engine");
        return "Client_identity";
    }

    @Override
    public SecretKey getKey(String identityHint, String identity, Socket socket) {
        System.out.println("getKey: socket");
        byte[] keyBytes = null;
        try {
            byte[] bytes = Crypto.decrypt();
            JSONObject jsonObject = new JSONObject(bytes.toString());
            String key = jsonObject.getString(Utils.JSON_SECRET_KEY);
            keyBytes = Base64.decode(key, Base64.DEFAULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //byte[] keyBytes = new byte[] {0x1a, 0x2b, 0x3c, 0x4d};
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        System.out.println(key.getEncoded());
        System.out.println(key);
        return key;
    }

    @Override
    public SecretKey getKey(String identityHint, String identity, SSLEngine engine) {
        System.out.println("getKey: engine");
        return null;
    }
}
