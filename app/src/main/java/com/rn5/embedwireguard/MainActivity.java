package com.rn5.embedwireguard;

import android.os.Bundle;

import com.rn5.wireguardlibrary.WireGuardConnection;
import com.rn5.wireguardlibrary.listener.ConnectionStateChangeListener;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ConnectionStateChangeListener {
    private WireGuardConnection connection;
    public MainActivity() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = new WireGuardConnection(this, this).build();
        connection.up();
    }

    @Override
    public void onStateChange(State state) {
    }

    @Override
    public void onDestroy() {
        connection.down();
        super.onDestroy();
    }
}
