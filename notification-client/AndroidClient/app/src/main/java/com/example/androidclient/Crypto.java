package com.example.androidclient;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {
    private static final String identity_filename = "identity";
    private static final String iv_filename = "iv";
    private static final String key_alias = "notifier";

    private static SecretKey generateKey() {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.WEEK_OF_YEAR, 1);
            final KeyGenParameterSpec spec =
                    new KeyGenParameterSpec.Builder(key_alias, KeyProperties.PURPOSE_DECRYPT|KeyProperties.PURPOSE_ENCRYPT).
                            setKeyValidityStart(start.getTime()).
                            setKeyValidityEnd(end.getTime()).
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

    private static SecretKey getKey(Boolean generate) {
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

    public static void encryptAndWriteToDisk(Boolean generateNewKey, String data) {
        SecretKey key = getKey(generateNewKey);
        Context context = App.getAppContext();
        try {
            File file = new File(context.getFilesDir(), identity_filename);
            if (file.exists()) {
                file.delete();
            }
            System.out.println("Creating file '" + identity_filename + "'.");
            if (!file.createNewFile()) {
                System.out.println("Could not create file.");
                return;
            }
            File ivFile = new File(context.getFilesDir(), iv_filename);
            if (ivFile.exists()) {
                ivFile.delete();
            }
            System.out.println("Creating file '" + iv_filename + "'.");
            if (!ivFile.createNewFile()) {
                System.out.println("Could not create file.");
                return;
            }
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] IV = Base64.encode(cipher.getIV(), Base64.DEFAULT);
            String ivString = new String(IV, StandardCharsets.UTF_8);
            System.out.println("Encrypting, IV: " + ivString);
            FileOutputStream fileOutputStreamIV = context.openFileOutput(iv_filename, Context.MODE_PRIVATE);
            fileOutputStreamIV.write(IV);
            fileOutputStreamIV.close();

            FileOutputStream fileOutputStream = context.openFileOutput(identity_filename, Context.MODE_PRIVATE);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
            System.out.println("Writing file");
            cipherOutputStream.write(data.getBytes());
            cipherOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] decrypt() {
        int size = 1000;
        byte[] bytes = new byte[size];
        Context context = App.getAppContext();
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(key_alias, null);
            SecretKey key = entry.getSecretKey();

            FileInputStream fileInputStreamIV = context.openFileInput(iv_filename);
            byte[] ivBytes = new byte[size];
            if (fileInputStreamIV.read(ivBytes) == -1) {
                System.out.println("Could not read from iv file");
                fileInputStreamIV.close();
                return bytes;
            }
            IvParameterSpec parameterSpec = new IvParameterSpec(ivBytes);
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            FileInputStream fileInputStream = context.openFileInput(identity_filename);
            CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
            System.out.println("Reading key");

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
