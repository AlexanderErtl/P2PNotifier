package com.example.androidclient;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Crypto {
    private final String identity_filename = "identity";
    private final String key_alias = "notifier";

    private SecretKey generateKey() {
        try {
            final KeyGenParameterSpec spec =
                    new KeyGenParameterSpec.Builder(key_alias, KeyProperties.PURPOSE_DECRYPT|KeyProperties.PURPOSE_ENCRYPT).
                            setIsStrongBoxBacked(true).
                            setKeySize(256).
                            setBlockModes(KeyProperties.BLOCK_MODE_GCM).
                            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build();

            final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(spec);

            return keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Failed building keyGenParameterSpec.");
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Could not initialize keyGenerator.");
        }
        return null;
    }

    private SecretKey getKey(Boolean generate) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (generate || !ks.containsAlias(key_alias)) {
                return generateKey();
            }
            KeyStore.SecretKeyEntry e = (KeyStore.SecretKeyEntry)ks.getEntry(key_alias, null);
            return e.getSecretKey();
        } catch (Exception e) {
            System.out.println("Could not retrieve key from keystore");
            return null;
        }
    }

    public void encryptAndWriteToDisk(Context context, Boolean generateNewKey, String data) {
        SecretKey key = getKey(generateNewKey);
        try {
            File file = new File(context.getFilesDir(), identity_filename);
            if (!file.exists()) {
                System.out.println("Creating file '" + identity_filename + "'.");
                if (!file.createNewFile()) {
                    System.out.println("Could not create file.");
                    return;
                }
            }
            final Cipher cipher = Cipher.getInstance("AES_256/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            FileOutputStream fileOutputStream = context.openFileOutput(identity_filename, Context.MODE_PRIVATE);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
            System.out.println("Writing file");
            cipherOutputStream.write(data.getBytes());
            cipherOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] decrypt(Context context) {
        int size = 1000;
        byte[] bytes = new byte[size];
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(key_alias, null);
            SecretKey key = entry.getSecretKey();

            final Cipher cipher = Cipher.getInstance("AES_256/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            FileInputStream fileInputStream = context.openFileInput(identity_filename);
            CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
            System.out.println("Writing file");

            if (cipherInputStream.read(bytes) == -1) {
                System.out.println("Could not read from file");
            }
            cipherInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
