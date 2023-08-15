package iss.ca.wbgt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ApiReceiver extends BroadcastReceiver {

    private LocationService locationService = new LocationService();

    public ApiReceiver(){
        // empty constructor
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // default method
    }
}