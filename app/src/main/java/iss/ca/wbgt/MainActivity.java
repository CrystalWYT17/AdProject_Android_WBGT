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

import androidx.lifecycle.ViewModelProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Location lastKnownLocation;
    private Task<Location> locationResult;
    private FusedLocationProviderClient fusedLocationClient;

    //notification Fragment
    private Fragment prevFragment = new MainFragment();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private boolean isClickedNotification = false;
    private Drawable originalDrawable;
    private Drawable tintedDrawable;
    private ImageView notificationBell;

    //To Store Notification
    File mTargetFile;
    private ArrayList<NotificationModel> notifications = new ArrayList<NotificationModel>();
    private ArrayList<NotificationModel> notificationsTest = new ArrayList<NotificationModel>();

    private int LOCATION_PERMISSION_REQCODE = 1111;

    private final String apiKey = BuildConfig.MAPS_API_KEY;
    private PlacesClient placesClient;

    private List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

    private String[] locationPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private String[] notificationPermission = {Manifest.permission.POST_NOTIFICATIONS};

    private LocationManager locationManager;
    private Location location;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private List<Station> stationData = new ArrayList<>();
    private UserCurrentData userCurrentData;
    private Map<String,List<String>> dayForecast = new HashMap<>();
    private Map<Integer, List<Double>> xHoursForecast = new HashMap<>();
    private ApiService apiService = new ApiService();
    private UserCurrentData currentData = new UserCurrentData();
    private String nearestStation;

    private StationDataViewModel viewModel;

    protected BroadcastReceiver apiReceiver = new ApiReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("CurrentWbgtCompleted")){
                currentData.setStationName(intent.getStringExtra("currentStationName"));
                currentData.setWbgtValue(intent.getStringExtra("currentWbgt"));
                viewModel.setUserCurrentData(currentData);

                writeToSharedPreference("currentData");
//                currentData = apiService.getCurrentData();
//                currentData.setStationName(intent.getStringExtra("currentStationName"));
//                currentData.setWbgtValue(intent.getStringExtra("currentWbgt"));
//                dayForecast = intent.getSerializableExtra()
            }
            else {

                dayForecast = (Map<String, List<String>>) intent.getSerializableExtra("dayForecast");
                writeToSharedPreference("dayForecast");

            }
//            writeToSharedPreference();

            Fragment mainFragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mainFragment);
            transaction.commit();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        register();
        storeStationData();

        // create view model object
        viewModel = new ViewModelProvider(this).get(StationDataViewModel.class);

//        Intent intent = getIntent();
//        String stationName = intent.getStringExtra("stationName");
//        String wbgt = intent.getStringExtra("wbgt");
//
//        // store current data in object
//        userCurrentData = new UserCurrentData(stationName,wbgt);
//
//        stationData = (List<Station>) intent.getSerializableExtra("stationList");
//        dayForecast = (Map<String, List<String>>) intent.getSerializableExtra("dayForecast");
//        xHoursForecast = (Map<Integer, List<Double>>) intent.getSerializableExtra("xHoursForecast");

        //notification icon

        originalDrawable = AppCompatResources.getDrawable(MainActivity.this, R.drawable.notification);
        tintedDrawable = originalDrawable.getConstantState().newDrawable().mutate();
        DrawableCompat.setTint(tintedDrawable, Color.GREEN);

        notificationBell = findViewById(R.id.notification_badge);
        //switch to notification fragment
        notificationBell.setOnClickListener(v -> {
            isClickedNotification = !isClickedNotification;
            //get current fragment
            if (isClickedNotification) {
                prevFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                notificationBell.setImageDrawable(tintedDrawable);
                //switch to notification fragment
                Fragment notificationFragment = new NotificationFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right
                ).replace(R.id.fragment_container, notificationFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            } else {
                notificationBell.setImageDrawable(originalDrawable);
                Fragment mainFragment =  prevFragment;
                makeFragmentTransaction(prevFragment);

            }
        });

        //store notifications to file
        String folder = "NotificationsTest";
        String fileName = "notification_list";
        mTargetFile = new File(getFilesDir(), folder + "/" + fileName);
        System.out.println("NotiTest" + notificationsTest);


        Places.initialize(getApplicationContext(), apiKey);
        placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

//        checkPermission();

        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        Fragment mainFragment = new MainFragment();
//        mainFragment.setStationName(stationName);
//        mainFragment.setWbgtValue(wbgt);
//        mainFragment.setDayForecast(dayForecast);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }

    public void storeStationData(){
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

    }

    @Override
    protected void onStart() {
        register();
        checkPermission();
        checkNotificationPermission();
        super.onStart();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (item.getItemId() == R.id.nav_station) {
            Fragment fragment = new StationFragment();
            transaction.replace(R.id.fragment_container, fragment);
            //    transaction.addToBackStack(null);
            transaction.commit();
        } else if (item.getItemId() == R.id.home) {
            Fragment fragment = new MainFragment();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack("mainFragment");
            transaction.commit();
        } else if (item.getItemId() == R.id.nav_about) {
            Fragment fragment = new AboutFragment();
            transaction.replace(R.id.fragment_container, fragment);
            // transaction.addToBackStack(null);
            transaction.commit();
        } else if (item.getItemId() == R.id.nav_faq) {
            Fragment fragment = new FaqFragment();
            transaction.replace(R.id.fragment_container, fragment);
            //transaction.addToBackStack(null);
            transaction.commit();
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentFragment instanceof MainFragment) {
            super.onBackPressed();
        } else if (currentFragment instanceof NotificationFragment) {
            //isClickedNotification = !isClickedNotification;
            Fragment mainFragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.enter_left_to_right, R.anim.exit_left_to_right,
                    R.anim.enter_right_to_left, R.anim.exit_right_to_left
            ).replace(R.id.fragment_container, mainFragment).commit();
        } else {
            Fragment mainFragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mainFragment);
            transaction.commit();
        }
        if (isClickedNotification) {
            notificationBell.setImageDrawable(originalDrawable);
        }

    }

    private NotificationModel convertStringToNotification(String notiString) {
        NotificationModel notification = new NotificationModel();
        String[] stringArr = notiString.split("\\|");
        notification.setTitle(stringArr[0]);
        notification.setMessage(stringArr[1]);
        notification.setTime(stringArr[2]);

        return notification;
    }

    private void checkNotificationPermission(){
        if(checkSelfPermission(notificationPermission[0])==PackageManager.PERMISSION_GRANTED){
            Log.d("notification permit", "granted");
        }else {
            requestPermission();
        }
    }

    private void requestPermission(){
        String[] permission = {Manifest.permission.POST_NOTIFICATIONS};
        ActivityCompat.requestPermissions(this, permission, 1);
    }

    private void makeFragmentTransaction(Fragment fragment){
        //Fragment mainFragment = new MainFragment();
        if(fragment instanceof MainFragment){
            fragment = new MainFragment();
        } else if (fragment instanceof StationFragment) {
            fragment = new StationFragment();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_left_to_right, R.anim.exit_left_to_right,
                R.anim.enter_right_to_left, R.anim.exit_right_to_left
        ).replace(R.id.fragment_container, fragment).commit();
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
        nearestStation = stationDistanceList.get(0).getKey();
        Log.i("NEAREST STATION",nearestStation);

        checkCurrentData(nearestStation);

    }

    public void checkCurrentData(String nearestStation){

//        viewModel.getStateData().observe(this, state -> {
//            Log.i("stateData",state.getStationId());
//        });

//        Intent intent = new Intent(MainActivity.this,ApiService.class);
//        intent.putExtra("stationId",nearestStation);
//        intent.putExtra("stationData",(Serializable) stationData);
//        startService(intent);

        SharedPreferences shr = getSharedPreferences("wbgt_main_fragment", MODE_PRIVATE);
        String currentStationName = shr.getString("currentStationName","");
        String currentStationId = shr.getString("currentStationId","");
        if(!currentStationId.equals(nearestStation)){
            Intent intent = new Intent(MainActivity.this,ApiService.class);
            intent.putExtra("stationId",nearestStation);
            intent.putExtra("stationData",(Serializable) stationData);
            startService(intent);
//            apiService.getCurrentWBGTData(nearestStation);
//            Thread bkThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    apiService.getCurrentWBGTData(nearestStation);
////            apiService.getXDayForecast(nearestStation);
//                }
//            });
//            bkThread.run();
//
//
////            currentData = apiService.getCurrentData();
////            dayForecast = apiService.getDayForecast();
////            writeToSharedPreference();
        }
    }

    public void writeToSharedPreference(String state){
        SharedPreferences shr = getSharedPreferences("wbgt_main_fragment",MODE_PRIVATE);
        SharedPreferences.Editor editor = shr.edit();
        if(state.equals("currentData")){
            editor.putString("currentStationName",currentData.getStationName());
            editor.putString("currentStationId",nearestStation);
            editor.putString("currentWbgt",currentData.getWbgtValue());
        }
        else{
            String dayForecastString = convertMapToString(dayForecast);
            editor.putString("dayForecast",dayForecastString);
        }

        editor.commit();
//        Log.i("dayforecast",dayForecastString);
    }

    public String convertMapToString(Map<String,List<String>> dayForecast){
        String finalConcatString = "";
        for(Map.Entry<String,List<String>> kv: dayForecast.entrySet()){
            finalConcatString +=kv.getKey() + "," + kv.getValue().get(0) +"|" + kv.getValue().get(1) +"/";
        }
        return finalConcatString;
    }

    public void register(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("CurrentWbgtCompleted");
        filter.addAction("DayForecastCompleted");
//        filter.addAction("HourlyForecastCompleted");
        registerReceiver(apiReceiver,filter);
    }
}