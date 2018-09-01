package com.valuestream.beaconsdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.valuestream.beaconsdk.beacon.BeaconConsumer;

import com.valuestream.beaconsdk.beacon.Beacon;
import com.valuestream.beaconsdk.beacon.BeaconConsumer;
import com.valuestream.beaconsdk.beacon.BeaconManager;
import com.valuestream.beaconsdk.beacon.BeaconParser;
import com.valuestream.beaconsdk.beacon.Identifier;
import com.valuestream.beaconsdk.beacon.MonitorNotifier;
import com.valuestream.beaconsdk.beacon.RangeNotifier;
import com.valuestream.beaconsdk.beacon.Region;
import com.valuestream.beaconsdk.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;

import static com.valuestream.beaconsdk.beacon.service.BeaconService.TAG;


/**
 * Created by Value Stream Technologies on 01-09-2018.
 */
public class SampleBroadcastReceiver implements BeaconConsumer {
    Activity mActivity;
    Context mContext;
    private BeaconManager beaconManager;

    public SampleBroadcastReceiver(Activity mActivity, Context context) {
        this.mActivity = mActivity;
        mContext = context;
        checkForLocationPermission();
    }

    public void startBeaconService() {
        Log.w("success", "Comes startBeaconService");
        beaconManager = BeaconManager.getInstanceForApplication(mContext);
        beaconManager.getBeaconParsers().clear();
        beaconManager.setEnableScheduledScanJobs(true);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                //.setBeaconLayout(("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19")));
                .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.setDebug(true);

        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        // beaconManager.bind(this);
        Log.w("success", "Comes onBeaconServiceConnect");
        Identifier myBeaconNamespaceId = null;//Identifier.parse("0xaaaaaaaaaaaaaaaaaaaa");
        Identifier myBeaconInstanceId = null;//Identifier.parse("0xffffffffffff");
        final Region region = new Region("my-beacon-region", myBeaconNamespaceId, myBeaconInstanceId, null);

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "Region = " + region.toString());
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon oneBeacon : beacons) {
                    //Log.d(TAG, "distance: " + oneBeacon.getDistance());
                    Log.d(TAG, "Id1: " + oneBeacon.getId1());
                    Log.d(TAG, "Id1: " + oneBeacon.getId2());
                    //Log.d(TAG,"Id2: "+oneBeacon.getId2());
                    final double distanceDouble = oneBeacon.getDistance();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Beacon is communicating with Phone. Distance is: " + distanceDouble);
                        }
                    });


                }
            }
        });

        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(mActivity);
        beaconManager.setBackgroundScanPeriod(3000l);
        beaconManager.setBackgroundBetweenScanPeriod(15000l);

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkForLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mActivity, "Please give Location permission....", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
