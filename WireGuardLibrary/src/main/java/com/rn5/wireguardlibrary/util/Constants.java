package com.rn5.wireguardlibrary.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rn5.wireguardlibrary.define.Vpn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();
    public static <T> T fromStringToObject(String a, Class<T> t) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(a, t);
    }

    public static String getEncryptedFileContents(Activity activity, File file) throws IOException, GeneralSecurityException {
        Log.d(TAG, "getEncryptedFileContents()");
        Context context = activity.getApplicationContext();
        MasterKey mainKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                file,
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        InputStream inputStream = encryptedFile.openFileInput();
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int nextByte = objectInputStream.read();
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte);
            nextByte = objectInputStream.read();
        }
        byte[] text = byteArrayOutputStream.toByteArray();
        return new String(text);
    }

    public static String getFileContents(File file) throws IOException {
        String inputLine;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        br.close();
        return sb.toString();
    }

    public static void writeStringToFile(File file, String content) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    public static void writeStringToEncryptedFile(Activity activity, File file, String content) throws IOException, GeneralSecurityException {
        Context context = activity.getApplicationContext();
        MasterKey mainKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                file,
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        byte[] contents = content.getBytes(UTF_8);
        OutputStream outputStream = encryptedFile.openFileOutput();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.write(contents);
        objectOutputStream.close();
        outputStream.flush();
        outputStream.close();
    }
}
