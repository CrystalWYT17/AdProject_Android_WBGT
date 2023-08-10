package iss.ca.wbgt;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.auth.User;
import com.google.gson.JsonSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class SplashScreenActivity extends AppCompatActivity {

    private String[] locationPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private LocationManager locationManager;
    private Location location;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private List<Station> stationData = new ArrayList<>();

    private int LOCATION_PERMISSION_REQCODE = 1111;
    private Map<String,List<String>> dayForecast = new HashMap<>();
    private Map<Integer, List<Double>> xHoursForecast = new HashMap<>();
    private ApiService apiService = new ApiService();
    private UserCurrentData currentData = new UserCurrentData();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // add station data
        stationData.add(new Station("S117", "Ban Yan", 103.679, 1.256));
        stationData.add(new Station("S116", "West Coast", 103.754, 1.281));
        stationData.add(new Station("S50", "Clementi", 103.7768, 1.3337));
        stationData.add(new Station("S60", "Sentosa", 103.8279, 1.25));
        stationData.add(new Station("S107","East Coast Parkway",103.9625,1.3135));
        stationData.add(new Station("S43","Kim Chuan Street",103.8878,1.3399));
        stationData.add(new Station("S111","Scotts Road",103.8365,1.31055));
        stationData.add(new Station("S115","Tuas South Avenue 3",103.61843,1.29377));
        stationData.add(new Station("S109","Ang Mo Kio Avenue 5",103.8492,1.3764));
        stationData.add(new Station("S121","Choa Chu Kang Road",103.72244,1.37288));
        stationData.add(new Station("S104","Woodlands Avenue 9",103.78538,1.44387));
        stationData.add(new Station("S24","Upper Changi Road North",103.9826,1.3678));
        stationData.add(new Station("S44","Nanyang Avenue",103.68166,1.34583));

        apiService.setStationData(stationData);

        checkPermission();


    }

    private void checkPermission() {
        if (checkSelfPermission(locationPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(locationPermission[1]) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this, locationPermission, LOCATION_PERMISSION_REQCODE);
        }
    }

    // get user current location
    public void getCurrentLocation() {

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled && isNetworkEnabled) {
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null){
                    calculateDistance(location);
//                    Log.i("currentlocation","lat:"+location.getLatitude()+" long: "+location.getLongitude());
                }
                else{
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null){
                        calculateDistance(location);
                        Log.i("CURRENT","lat: "+location.getLatitude()+" long: "+location.getLongitude());
                    }
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Make sure you have GPS or Network service available.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQCODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    // calculate distance between user and stations
    public void calculateDistance(Location location){
        float[] results = new float[1];
        HashMap<String,Float> dstWithStation = new HashMap<String, Float>();
        for(Station s: stationData){
            Location.distanceBetween(location.getLatitude(),location.getLongitude(),s.getLatitude(),s.getLongitude(),results);
            dstWithStation.put(s.getId(),results[0]);
        }

        Comparator<Map.Entry<String,Float>> valueComparator = new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> t1, Map.Entry<String, Float> t2) {
                return t1.getValue().compareTo(t2.getValue());
            }
        };

        Set<Map.Entry<String, Float>> stationDistanceSet = dstWithStation.entrySet();
        List<Map.Entry<String, Float>> stationDistanceList = new ArrayList<>(stationDistanceSet);

        // get nearest station
        Collections.sort(stationDistanceList,valueComparator);
        String nearestStation = stationDistanceList.get(0).getKey();
        Log.i("NEAREST STATION",nearestStation);


        apiService.getCurrentWBGTData(nearestStation);
        apiService.getXDayForecast(nearestStation);
        apiService.getXHourForecast(nearestStation);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                writeToSharedPreference();
                currentData = apiService.getCurrentData();
                dayForecast = apiService.getDayForecast();
                xHoursForecast = apiService.getxHoursForecast();
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.putExtra("stationName",currentData.getStationName());
                intent.putExtra("stationId", currentData.getStationId());
                intent.putExtra("wbgt",currentData.getWbgtValue());
                intent.putExtra("dayForecast",(Serializable) dayForecast);
                intent.putExtra("stationList",(Serializable) stationData);
                intent.putExtra("xHoursForecast",(Serializable) xHoursForecast);
                startActivity(intent);
            }
        },10000);


    }

    public void writeToSharedPreference(){
        SharedPreferences shr = getSharedPreferences("wbgt_main_fragment",MODE_PRIVATE);
        SharedPreferences.Editor editor = shr.edit();
        editor.putString("currentStationName",currentData.getStationName());
        editor.putString("currentWbgt",currentData.getWbgtValue());
    }

    public String convertMapToString(Map<String,List<String>> dayForecast){
        for(Map.Entry<String,List<String>> day: dayForecast.entrySet()){

        }
        return null;
    }


}