package com.rn5.wireguardlibrary.define;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.rn5.wireguardlibrary.WireGuardConnection.file;
import static com.rn5.wireguardlibrary.util.Constants.fromStringToObject;
import static com.rn5.wireguardlibrary.util.Constants.getEncryptedFileContents;
import static com.rn5.wireguardlibrary.util.Constants.getFileContents;
import static com.rn5.wireguardlibrary.util.Constants.writeStringToEncryptedFile;

@Getter
@Setter
@ToString
public class Vpn {
    private static final String TAG = Vpn.class.getSimpleName();
    private static final String fileName = "vpn.json";
    private static final String fileNameEncrypted = "encrypted_vpn.txt";
    private Host host;
    private Client client;
    public Vpn() {}

    @Getter
    @Setter
    @ToString
    public static class Host {
        private String endpoint;
        private String publickey;
        private String presharedkey;
        private String allowedips;
        private String persistentkeepalive;
        public Host() {}
    }

    @Getter
    @Setter
    @ToString
    public static class Client {
        private String address;
        private String dns;
        private String listenport;
        private String privatekey;
        private String mtu;
        public Client() {}
    }

    public Vpn build(Activity activity) throws FileNotFoundException, GeneralSecurityException {
        Log.d(TAG,"build()");
        File jsonFile = new File(file, fileName);
        try {
            if (jsonFile.exists()) {
                String val = getFileContents(jsonFile);
                writeStringToEncryptedFile(activity, new File(file, fileNameEncrypted), val);
                if (jsonFile.delete()) {
                    Toast.makeText(activity.getApplicationContext(), "vpn.json encrypted.", Toast.LENGTH_SHORT).show();
                }
                return fromStringToObject(val, Vpn.class);
            } else {
                String val = getEncryptedFileContents(activity, new File(file, fileNameEncrypted));
                Log.d(TAG, "Vpn.build() " + val);
                return fromStringToObject(val, Vpn.class);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            Log.e(TAG, "build() failed. Error: " + e.getMessage());
            throw new FileNotFoundException();
        }
    }
}

