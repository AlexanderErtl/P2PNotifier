package com.example.androidclient.ssl;

import android.util.Base64;

import com.example.androidclient.Crypto;
import com.example.androidclient.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
        String identity = "Client_identity";
        try {
            String data = Crypto.decrypt();
            JSONObject jsonObject = new JSONObject(data);
            String identityString = jsonObject.getString(Utils.JSON_IDENTITY_KEY);
            identity = new String(Base64.decode(identityString, Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (JSONException e) {
            System.out.println("Failed to restore identity!");
            e.printStackTrace();
        }
        return identity;
    }

    @Override
    public String chooseClientKeyIdentity(String identityHint, SSLEngine engine) {
        System.out.println("chooseClientKeyIdentity: engine");
        return "Client_identity";
    }

    @Override
    public SecretKey getKey(String identityHint, String identity, Socket socket) {
        System.out.println("getKey: socket");
        SecretKey key = null;
        try {
            String data = Crypto.decrypt();
            JSONObject jsonObject = new JSONObject(data);
            String keyString = jsonObject.getString(Utils.JSON_SECRET_KEY);
            byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
            //byte[] keyBytes = new byte[] {0x1a, 0x2b, 0x3c, 0x4d};
            key = new SecretKeySpec(keyBytes, "AES");
        } catch (JSONException e) {
            System.out.println("Failed to restore secret key!");
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public SecretKey getKey(String identityHint, String identity, SSLEngine engine) {
        System.out.println("getKey: engine");
        return null;
    }
}
