package com.rn5.wireguardlibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.rn5.wireguardlibrary.define.Vpn;
import com.rn5.wireguardlibrary.listener.ConnectionStateChangeListener;
import com.wireguard.android.backend.BackendException;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.BadConfigException;
import com.wireguard.config.Config;
import com.wireguard.config.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WireGuardConnection extends Activity {
    private final static String TAG = WireGuardConnection.class.getSimpleName();
    private final Activity activity;
    private final ConnectionStateChangeListener listener;
    private Peer peer;
    private Tunnel tunnel;
    private Config config;
    private final GoBackend backend;
    private boolean up;
    public static File file;

    public WireGuardConnection(Activity activity, ConnectionStateChangeListener listener) {
        this.activity = activity;
        this.listener = listener;
        backend = new GoBackend(activity.getApplicationContext());
        setFilePath(this.activity);
    }

    public WireGuardConnection build() {
        try {
            Vpn vpn = new Vpn().build(activity);
            List<String> itPeer = new ArrayList<>();
            itPeer.add("endpoint=" + vpn.getHost().getEndpoint());
            itPeer.add("publickey=" + vpn.getHost().getPublickey());
            itPeer.add("presharedkey=" + vpn.getHost().getPresharedkey());
            itPeer.add("allowedips=" + vpn.getHost().getAllowedips());
            itPeer.add("persistentkeepalive=" + vpn.getHost().getPersistentkeepalive());

            List<String> itInt = new ArrayList<>();
            itInt.add("address=" + vpn.getClient().getAddress());
            itInt.add("dns=" + vpn.getClient().getDns());
            itInt.add("listenport=" + vpn.getClient().getListenport());
            itInt.add("privatekey=" + vpn.getClient().getPrivatekey());
            itInt.add("mtu=" + vpn.getClient().getMtu());

            try {
                peer = Peer.parse(itPeer);
            } catch (BadConfigException e) {
                Log.e(TAG, "setVpn() invalid peer " + e.getLocation() + " " + e.getReason());
            }
            tunnel = new Tunnel() {
                @Override
                @NonNull
                public String getName() {
                    return "home";
                }

                @Override
                public void onStateChange(@NonNull State newState) {
                    Log.d(TAG, "Tunnel() onStateChange[" + newState.toString() + "]");
                    up = newState == State.UP;
                    listener.onStateChange(newState == State.UP? ConnectionStateChangeListener.State.UP:
                            ConnectionStateChangeListener.State.DOWN);
                }
            };
            try {
                config = new Config.Builder().parseInterface(itInt)
                        .addPeer(peer).build();
            } catch (BadConfigException e) {
                Log.e(TAG, "setVpn() invalid interface " + e.getLocation() + " " + e.getReason() + " " + e.getText());
            }
            return this;
        } catch (FileNotFoundException e) {
            Toast.makeText(activity.getApplicationContext(), "No vpn.json file exists.  Vpn will not be used.", Toast.LENGTH_SHORT).show();
            return null;
        } catch (GeneralSecurityException e) {
            Toast.makeText(activity.getApplicationContext(), "Security Exception. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void up() {
        tunnel(Tunnel.State.UP);
    }

    public void down() {
        tunnel(Tunnel.State.DOWN);
    }

    private void tunnel(Tunnel.State state) {
        Intent intentPrep = VpnService.prepare(activity.getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putString("state", state.toString());
        if (intentPrep != null) {
            intentPrep.putExtras(bundle);
            activity.startActivityForResult(intentPrep, 0);
        } else {
            Intent intentVpn = new Intent();
            intentVpn.putExtras(bundle);
            onActivityResult(0, RESULT_OK, intentVpn);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Tunnel.State state = Tunnel.State.valueOf(bundle.getString("state"));
            executeVpn(state);
        }
    }

    private static void setFilePath(Activity activity) {
        file = activity.getExternalFilesDir("Settings");
        boolean result = file != null && (file.exists() || file.mkdir());
        result = result && (file.canWrite() || file.setWritable(true, true));
        if (!result)
            Toast.makeText(activity.getApplicationContext(), "Create Settings Directory Failed.", Toast.LENGTH_SHORT).show();
    }

    private void executeVpn(final Tunnel.State state) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
                try {
                    backend.setState(tunnel, state, config);
                } catch (BackendException e) {
                    e.printStackTrace();
                    Log.e(TAG, "createTunnel() BackendException " + e.getLocalizedMessage() + " Reason " + e.getReason().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "createTunnel() failed" + e.getMessage());
                }
        });
    }
}
