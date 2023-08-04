package iss.ca.wbgt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationReceiver extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token){
        //Log.d(TAG,"Refreshed Token: "+ token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getNotification() != null){
            showNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    public void showNotification(String title, String body){
        Intent intent = new Intent(this, MainActivity.class);
        String channel_id = "wbgt_notification";
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder
                = new NotificationCompat
                .Builder(getApplicationContext(),
                channel_id)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000, 1000,
                        1000, 1000 })
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "wbgt",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1, builder.build());
        }
    }
}
