package com.fangjet.ez.launcher.battery;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Based on and credit given to <INSERT_APP_NAME> </INSERT_APP_NAME>
 * <p>
 * Created by ctucker on 5/29/17.
 */
public class BatteryInfoService extends Service {

    private static final String TAG = "BatteryInfoService";

    private static Set<Messenger> clientMessengers;
    private static Messenger messenger;
    private final IntentFilter batteryChanged = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private final Handler mHandler = new Handler();
    private final Runnable runRenotify = new Runnable() {
        public void run() {
            registerReceiver(mBatteryInfoReceiver, batteryChanged);
        }
    };
    private BatteryInfo info;
    private final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (!Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) return;

            update();
        }
    };

    private static void sendClientMessage(Messenger clientMessenger, int what) {
        sendClientMessage(clientMessenger, what, null);
    }

    private static void sendClientMessage(Messenger clientMessenger, int what, Bundle data) {
        Log.e(TAG, "------------sendClientMessage-----------");
        Message outgoing = Message.obtain();
        outgoing.what = what;
        outgoing.replyTo = messenger;
        outgoing.setData(data);
        try {
            clientMessenger.send(outgoing);
        } catch (android.os.RemoteException ignored) {
        }
    }

    @Override
    public void onCreate() {
        info = new BatteryInfo();

        messenger = new Messenger(new MessageHandler());
        clientMessengers = new HashSet<>();

        Intent bc_intent = registerReceiver(mBatteryInfoReceiver, batteryChanged);
        info.load(bc_intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBatteryInfoReceiver);
        mHandler.removeCallbacks(runRenotify);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        update();
        Log.e(TAG, "------------onStartCommand-----------");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void update() {
        Log.e(TAG, "------------update-----------");

        for (Messenger messenger : clientMessengers) {
            // TODO: Can I send the same message to multiple clients instead of sending duplicates?
            sendClientMessage(messenger, RemoteConnection.CLIENT_BATTERY_INFO_UPDATED, info.toBundle());
        }
    }


    public static class RemoteConnection implements ServiceConnection {
        public static final int SERVICE_REGISTER_CLIENT = 1;
        // Messages the service sends to clients
        public static final int CLIENT_SERVICE_CONNECTED = 0;
        public static final int CLIENT_BATTERY_INFO_UPDATED = 1;
        // Messages clients send to the service
        static final int SERVICE_CLIENT_CONNECTED = 0;
        static final int SERVICE_UNREGISTER_CLIENT = 2;
        Messenger serviceMessenger;
        private final Messenger clientMessenger;

        public RemoteConnection(Messenger m) {
            clientMessenger = m;
        }

        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            serviceMessenger = new Messenger(iBinder);

            Message outgoing = Message.obtain();
            outgoing.what = SERVICE_CLIENT_CONNECTED;
            outgoing.replyTo = clientMessenger;
            try {
                serviceMessenger.send(outgoing);
            } catch (android.os.RemoteException ignored) {
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }
    }

    // TODO do I need this?
    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message incoming) {
            switch (incoming.what) {
                case RemoteConnection.SERVICE_CLIENT_CONNECTED:
                    sendClientMessage(incoming.replyTo, RemoteConnection.CLIENT_SERVICE_CONNECTED);
                    break;
                case RemoteConnection.SERVICE_REGISTER_CLIENT:
                    clientMessengers.add(incoming.replyTo);
                    sendClientMessage(incoming.replyTo, RemoteConnection.CLIENT_BATTERY_INFO_UPDATED, info.toBundle());
                    break;
                case RemoteConnection.SERVICE_UNREGISTER_CLIENT:
                    clientMessengers.remove(incoming.replyTo);
                    break;
                default:
                    super.handleMessage(incoming);
            }
        }
    }
}

