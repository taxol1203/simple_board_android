package com.example.fcm_test.Firebase;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.fcm_test.MainActivity;
import com.example.fcm_test.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static android.content.ContentValues.TAG;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseInstanceIDService extends FirebaseMessagingService {


    public MyFirebaseInstanceIDService() {
    }

    //메서드를 재정의하면 수신된 RemoteMessage 객체를 기준으로 작업을 수행하고 메시지 데이터를 가져올 수 있습니다.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        //Log.e("fcmMessage", "out +" + remoteMessage.getNotification().getTitle() + " " +remoteMessage.getNotification().getBody() );
        if (remoteMessage.getData().size() > 0)
        {
            showNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
        }

        if (remoteMessage.getNotification() != null)
        {
            //애플케이션이 커져잇는지 확인.
            if(isAppRunning(getApplicationContext())){

                //메인엑티비티에 메세지 전송함
                Handler handler = ((MainActivity)MainActivity.context).mHandler ;
                Message message = handler.obtainMessage() ;
                message.what = 0;
                handler.sendMessage(message);

                Log.e("CheckFCM", "Notification_isRunning!");
            }else{
                Log.e("CheckFCM", "Notification_is_notRunning!");
            }
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private RemoteViews getCustomDesign(String title, String message)
    {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.noti_title, title);
        remoteViews.setTextViewText(R.id.noti_message, message);
        remoteViews.setImageViewResource(R.id.noti_icon, R.mipmap.ic_launcher);
        return remoteViews;
    }

    public void showNotification(String title, String message){
        Intent intent = new Intent(this, MainActivity.class);
        //"채널 id로 정하고 싶은 문자열 아무거나 입력"
        String channel_id = "MsgFromPhp";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(new long[] {1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            builder = builder.setContent(getCustomDesign(title, message));
        }
        else
        {
            builder = builder.setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.mipmap.ic_launcher);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "web_app", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(uri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0, builder.build());
    }
    private boolean isAppRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if(procInfos.get(i).processName.equals(context.getPackageName())){
                return true;
            }
        }

        return false;
    }


}
