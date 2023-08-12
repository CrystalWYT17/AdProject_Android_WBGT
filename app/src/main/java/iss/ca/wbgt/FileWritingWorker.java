package iss.ca.wbgt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;

public class FileWritingWorker extends Worker {

    public FileWritingWorker(@NonNull Context context, @NonNull WorkerParameters parameters){
        super(context, parameters);
    }
    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        Gson gson = new Gson();
        String serializedObj = inputData.getString("notification");
        NotificationModel notification = gson.fromJson(serializedObj, NotificationModel.class);
        writeNotificationToFile(notification);

        return null;
    }

    private void writeNotificationToFile(NotificationModel newNotification){
        FileOutputStream fos = null;
        try{
            String folder = "Notifications";
            String fileName = "notification_list";
            File mTargetFile = new File(getApplicationContext().getFilesDir(), folder+"/"+fileName);
            Log.d("targetFile in Worker", mTargetFile.getAbsolutePath());
            fos = new FileOutputStream(mTargetFile, true);
            String notification = newNotification.getTitle()+"|"+newNotification.getMessage()+"|"+newNotification.getTime();
            fos.write((notification+"\n").getBytes());
            Log.d("FileWrite", "OK");
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
}
