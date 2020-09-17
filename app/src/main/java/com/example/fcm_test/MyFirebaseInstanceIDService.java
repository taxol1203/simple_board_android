package com.example.fcm_test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import static android.content.ContentValues.TAG;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
    public MyFirebaseInstanceIDService() {
    }

    //메서드를 재정의하면 수신된 RemoteMessage 객체를 기준으로 작업을 수행하고 메시지 데이터를 가져올 수 있습니다.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("FCMTest", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCMTest", "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //10초 이상 넘어가는 일
                //scheduleJob();
            } else {
                //10초 이내에 처리
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("FCMTest", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // m
    }
}
