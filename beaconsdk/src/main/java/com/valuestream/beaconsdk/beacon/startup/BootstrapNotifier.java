package com.valuestream.beaconsdk.beacon.startup;

import android.content.Context;

import com.valuestream.beaconsdk.beacon.MonitorNotifier;

public interface BootstrapNotifier extends MonitorNotifier {
    public Context getApplicationContext();
}
