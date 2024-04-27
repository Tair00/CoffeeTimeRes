package com.example.coffeetimeres.notificasion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    private Context context;
    private FirebaseAnalytics mFirebaseAnalytics;
    private BroadcastReceiver pushBroadcastReceiver;

    public FirebaseHelper(Context context) {
        this.context = context;
        FirebaseApp.initializeApp(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void initFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        Log.e(TAG, "Token -> " + token);
                    }
                });

        pushBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                Log.e(TAG, "MESSAGE_RECEIVED ");
                if (extras != null) {
                    String key = extras.keySet().iterator().next();
                    if (key.equals(PushService.KEY_ACTION)) {
                        Log.e(TAG, "KEY  -> " + key);
                        String action = extras.getString(key);
                        if (action != null) {
                            if (action.equals(PushService.ACTIONS_SHOW_MESSAGE)) {
                                String message = extras.getString(PushService.KEY_MESSAGE);
                                if (message != null) {
                                    Log.e(TAG, "MESSAGE_KEY  -> " + message);
                                    NotificationUtils.sendNotification(context, "Заголовок уведомления", message);

                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "NO_");
                            }
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PushService.INTENT_FILTER);
        context.registerReceiver(pushBroadcastReceiver, intentFilter);
    }

    public void unregisterPushReceiver() {
        if (pushBroadcastReceiver != null) {
            context.unregisterReceiver(pushBroadcastReceiver);
        }
    }
}

