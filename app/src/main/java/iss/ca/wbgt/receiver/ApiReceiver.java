package iss.ca.wbgt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import iss.ca.wbgt.service.LocationService;

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