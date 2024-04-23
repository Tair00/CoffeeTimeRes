    package com.example.coffeetimeres.notificasion;


    import android.content.Intent;
    import com.google.firebase.messaging.FirebaseMessagingService;
    import com.google.firebase.messaging.RemoteMessage;

    import java.util.Map;

    public class PushService extends FirebaseMessagingService {

        public static final String INTENT_FILTER = "PUSH_EVENT";
        public static final String KEY_ACTION = "action";
        public static final String KEY_MESSAGE = "message";
        public static final String ACTIONS_SHOW_MESSAGE = "show_message";

        @Override
        public void onNewToken(String newToken) {
            super.onNewToken(newToken);
        }

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            Intent intent = new Intent(INTENT_FILTER);
            if (remoteMessage.getData() != null) {
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    intent.putExtra(key, value);
                }
                sendBroadcast(intent);
            }
        }

    }