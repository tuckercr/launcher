package com.fangjet.ez.launcher.battery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver is called when the system reboots.  It starts up the BatteryInfoService
 * <p>
 * Created by ctucker on 5/29/17.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "BootCompletedReceiver#onReceive: " + intent.getAction());
        ComponentName comp = new ComponentName(context.getPackageName(), BatteryInfoService.class.getName());
        context.startService(new Intent().setComponent(comp));
    }
}
