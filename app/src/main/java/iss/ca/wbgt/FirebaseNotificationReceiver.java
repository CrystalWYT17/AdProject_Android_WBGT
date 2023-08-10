package iss.ca.wbgt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class FirebaseNotificationReceiver extends FirebaseMessagingService {

    public static final int NOTIFY_ID = 9999;
    private static final String CHANNEL_ID = "7777";
    private static final String CHANNEL_NAME = "WBGT Notification";

    //to send broadcast notification
    private static final String NEW_NOTIFICATION_ACTION="new_notification_action";

    @Override
    public void onNewToken(@NonNull String token){
        Log.i("NewToken","Refreshed Token: "+ token);
//        uncomment when we test push notification
        String userId = getUserId();
        saveTokenToFirebaseStore(userId, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        System.out.println("Message Received");
        if(remoteMessage.getNotification() != null){
            //create notification
            if(remoteMessage.getData() != null){
                Log.d("STATION", remoteMessage.getData().toString());
                String station = remoteMessage.getData().get("station_id");
                //Log.d("STATION", station);
            }else {
                Log.d("STATION", "null for station");
            }
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            System.out.println(title + body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            String time = formatter.format(LocalDateTime.now());
            NotificationModel newNotification = new NotificationModel(title, body, time);
            //save notification to file
            writeNotificationToFile(newNotification);
            //get stationId and need to implement to get the nearest station


            //create notification
            createNotification(title, body);
        }
    }

//    @Override
//    public void onCreate() {
//
//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//            @Override
//            public void onComplete(@NonNull Task<String> task) {
//                if(!task.isSuccessful()){
//                    Log.e("GetToken","Error: "+task.getException());
//                    return;
//                }
//                String token = task.getResult();
//                Log.i("Token",token);
//            }
//        });
//
//    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, importance
            );
            channel.setDescription("WBGT Alert");
            NotificationManager notificationManager = getSystemService((NotificationManager.class));
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    public void createNotification(String title, String message) {
        createNotificationChannel();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000, 1000,
                        1000, 1000 })
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManagerCompat mgr = NotificationManagerCompat.from(this);
        mgr.notify(NOTIFY_ID,notification);

    }
    //save token in fire store
    private void saveTokenToFirebaseStore(String userId, String token){
        String collectionName= "users";
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = fStore.collection(collectionName).document(userId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("token", token);
        documentReference.set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("FCM Token", "Token successfully saved in Firestore");
                        }else{
                            Log.e("FCM Token", "Error saving token to Firestore", task.getException());
                        }
                    }
                });

    }

    private String getUserId(){
        String uuId;
        SharedPreferences pref = getSharedPreferences("UserIDs", MODE_PRIVATE);
        String userId = pref.getString("id", null);
        if(userId == null){
            uuId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("id", uuId);
            editor.apply();
        }else {
            uuId = userId;
        }
        return uuId;
    }

    private void writeNotificationToFile(NotificationModel newNotification){
        FileOutputStream fos = null;
        try{
            String folder = "Notifications";
            String fileName = "notification_list";
            File mTargetFile = new File(getApplication().getFilesDir(), folder+"/"+fileName);
            Log.d("targetFile", mTargetFile.getAbsolutePath());
            fos = new FileOutputStream(mTargetFile, true);
            String notification = newNotification.getTitle()+"|"+newNotification.getMessage()+"|"+newNotification.getTime();
            fos.write((notification+"\n").getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fos !=null){
                try {
                    fos.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

//    public void showNotification(String title, String body){
//        Intent intent = new Intent(this, MainActivity.class);
//        String channel_id = "wbgt_notification";
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        NotificationCompat.Builder builder
//                = new NotificationCompat
//                .Builder(getApplicationContext(),
//                channel_id)
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setAutoCancel(true)
//                .setVibrate(new long[] { 1000, 1000, 1000,
//                        1000, 1000 })
//                .setOnlyAlertOnce(true)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "wbgt",
//                    NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(notificationChannel);
//            notificationManager.notify(1, builder.build());
//        }
//    }
}
