package com.rn5.wireguardlibrary.listener;

import com.wireguard.android.backend.Tunnel;

public interface ConnectionStateChangeListener {
    void onStateChange(State state);

    enum State {
        DOWN,
        UP;
    }
}
