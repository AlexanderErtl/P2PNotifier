package com.example.androidclient;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

public class Crypto {
    private static final String identity_filename = "identity";
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

    /*private static SecretKey getKey(Boolean generate) {
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
    }*/

    private static MasterKey getKey(Boolean generate) {
        Context context = App.getAppContext();
        MasterKey masterKey = null;
        try {
            if (generate) {
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                if (ks.containsAlias(key_alias)) {
                    ks.deleteEntry(key_alias);
                }
            }
            /*Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.WEEK_OF_YEAR, 1);
            final KeyGenParameterSpec spec =
                    new KeyGenParameterSpec.Builder(key_alias, KeyProperties.PURPOSE_DECRYPT|KeyProperties.PURPOSE_ENCRYPT).
                            setKeyValidityStart(start.getTime()).
                            setKeyValidityEnd(end.getTime()).
                            setIsStrongBoxBacked(true).
                            setKeySize(256).
                            setBlockModes(KeyProperties.BLOCK_MODE_GCM).
                            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build();*/

            masterKey = new MasterKey.Builder(context, key_alias)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setRequestStrongBoxBacked(true)
                    .build();
        } catch (GeneralSecurityException|IOException e) {
            e.printStackTrace();
        }
        return masterKey;
    }

    public static void encryptAndWriteToDisk(Boolean generateNewKey, String data) {
        Context context = App.getAppContext();
        try {
            File file = new File(context.getFilesDir(), identity_filename);
            if (file.exists()) {
                System.out.println("File exists");
                if (file.delete()) {
                    System.out.println("Deleted file successfully");
                }
            }
            /*KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println(alias);
            }*/

            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    new File(context.getFilesDir(), identity_filename),
                    getKey(generateNewKey),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();
            System.out.println("Created encrypted file");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(encryptedFile.openFileOutput()));
            writer.write(data);
            writer.close();
            System.out.println("Finished writing file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decrypt() {
        Context context = App.getAppContext();
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                context,
                new File(context.getFilesDir(), identity_filename),
                getKey(false),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();
            System.out.println("Got handle on encrypted file");

            StringBuilder stringBuffer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(encryptedFile.openFileInput()))) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuffer.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                System.out.println("Error reading file");
                e.printStackTrace();
            }
            String contents = stringBuffer.toString();
            System.out.println("contents: " + contents);
            return contents;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
