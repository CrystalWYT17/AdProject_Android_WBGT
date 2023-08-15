package iss.ca.wbgt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

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
        String stationId = getNearestStation();
        System.out.println("Message Received");
        remoteMessage.getData();
        Log.d("STATION", remoteMessage.getData().toString());
        String stationIdFromNotification = remoteMessage.getData().get("station_id");
        Log.d("StationIdFromNotification", stationIdFromNotification);
        if(stationIdFromNotification != null && stationIdFromNotification.equals(stationId)){
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            System.out.println(title + body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            String time = formatter.format(LocalDateTime.now());
            //save notification to file
            NotificationModel newNotification = new NotificationModel(title, body, time);
            writeNotificationToFile(newNotification);
            //save Notification with worker
//            Gson gson = new Gson();
//            String serializeObject = gson.toJson(newNotification);
//            Data input = new Data.Builder().putString("notification", serializeObject).build();
//            //Enqueue worker
//            OneTimeWorkRequest fileWriteRequest = new OneTimeWorkRequest.Builder(FileWritingWorker.class)
//                    .setInputData(input)
//                    .build();
//            WorkManager.getInstance(getApplicationContext()).enqueue(fileWriteRequest);
            //get stationId and need to implement to get the nearest station


            //create notification
            createNotification(title, body);
            }
    }

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
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
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

    public String getNearestStation(){
        LocationService locationService = new LocationService();
        String stationId = locationService.getCurrentLocation(getApplicationContext());
        Log.i("stationId in noti",stationId);
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("wbgt_main_fragment", Context.MODE_PRIVATE);
//        String stationId = pref.getString("currentStationId", "S121");
        return stationId;
    }

}
