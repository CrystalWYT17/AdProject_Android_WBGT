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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

//    private Station banyanStation = new Station("S117", "Ban Yan", 103.679, 1.256);
//    private Station westCoastStation = new Station("S116", "West Coast", 103.754, 1.281);
//    private Station clementiStation = new Station("S50", "Clementi", 103.7768, 1.3337);
//    private Station sentosaStation = new Station("S60", "Sentosa", 103.8279, 1.25);
//    private Station eastCoastStation = new Station("S107","East Coast Parkway",103.9625,1.3135);
//    private Station kimChuanStation = new Station("S43","Kim Chuan Street",103.8878,1.3399);
//    private Station scottsRoadStation = new Station("S111","Scotts Road",103.8365,1.31055);
//    private Station tuasSouthStation = new Station("S115","Tuas South Avenue 3",103.61843,1.29377);
//    private Station angMoKioStation = new Station("S109","Ang Mo Kio Avenue 5",103.8492,1.3764);
//    private Station choaChuKangStation = new Station("S121","Choa Chu Kang Road",103.72244,1.37288);
//    private Station woolandStation = new Station("S104","Woodlands Avenue 9",103.78538,1.44387);
//    private Station changiStation = new Station("S24","Upper Changi Road North",103.9826,1.3678);
//    private Station nanyangStation = new Station("S44","Nanyang Avenue",103.68166,1.34583);
//    private
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add station data
//        stationData.add(new Station("S117", "Ban Yan", 103.679, 1.256));
//        stationData.add(new Station("S116", "West Coast", 103.754, 1.281));
//        stationData.add(new Station("S50", "Clementi", 103.7768, 1.3337));
//        stationData.add(new Station("S60", "Sentosa", 103.8279, 1.25));
//        stationData.add(new Station("S107","East Coast Parkway",103.9625,1.3135));
//        stationData.add(new Station("S43","Kim Chuan Street",103.8878,1.3399));
//        stationData.add(new Station("S111","Scotts Road",103.8365,1.31055));
//        stationData.add(new Station("S115","Tuas South Avenue 3",103.61843,1.29377));
//        stationData.add(new Station("S109","Ang Mo Kio Avenue 5",103.8492,1.3764));
//        stationData.add(new Station("S121","Choa Chu Kang Road",103.72244,1.37288));
//        stationData.add(new Station("S104","Woodlands Avenue 9",103.78538,1.44387));
//        stationData.add(new Station("S24","Upper Changi Road North",103.9826,1.3678));
//        stationData.add(new Station("S44","Nanyang Avenue",103.68166,1.34583));

        Intent intent = getIntent();
        String stationName = intent.getStringExtra("stationName");
        String wbgt = intent.getStringExtra("wbgt");

        // store current data in object
        userCurrentData = new UserCurrentData(stationName,wbgt);

        stationData = (List<Station>) intent.getSerializableExtra("stationList");
        dayForecast = (Map<String, List<String>>) intent.getSerializableExtra("dayForecast");

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
                Fragment mainFragment = new MainFragment();
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
        MainFragment mainFragment = new MainFragment();
        mainFragment.setStationName(stationName);
        mainFragment.setWbgtValue(wbgt);
        mainFragment.setDayForecast(dayForecast);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }

    @Override
    protected void onStart() {
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

//    protected void writeToFile() {
//        ArrayList<String> notificationString = getNotificationString();
//        try {
//            File parent = mTargetFile.getParentFile();
//            if (!parent.exists() && !parent.mkdirs()) {
//                throw new IllegalStateException("Could not create dir: " + parent);
//            }
//            FileOutputStream fos = new FileOutputStream(mTargetFile);
//            for (String notification : notificationString) {
//                fos.write((notification + "\n").getBytes());
//            }
//            fos.close();
//            Toast.makeText(getApplicationContext(), "Write File OK!", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    protected void readFromFile() {
//        try {
//            FileInputStream fis = new FileInputStream(mTargetFile);
//            DataInputStream dis = new DataInputStream(fis);
//            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
//            String strLine;
//            //Assign the lines from buffer reader to strline and check if it is null
//            while ((strLine = br.readLine()) != null) {
//                notificationsTest.add(convertStringToNotification(strLine));
//            }
//            dis.close();
//            //mInputTxt.setText(data);
//            Toast.makeText(this, "Read File OK!", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    protected ArrayList<String> getNotificationString() {
//        //initialize some notification data for testing purpose
//        notifications.add(new NotificationModel("Title1", "Body1", "Time1"));
//        notifications.add(new NotificationModel("Title2", "Body2", "Time2"));
//        notifications.add(new NotificationModel("Title3", "Body3", "Time3"));
//        notifications.add(new NotificationModel("Title4", "Body4", "Time4"));
//        notifications.add(new NotificationModel("Title5", "Body5", "Time5"));
//        notifications.add(new NotificationModel("Title6", "Body6", "Time6"));
//        notifications.add(new NotificationModel("Title7", "Body7", "Time7"));
//        notifications.add(new NotificationModel("Title8", "Body8", "Time8"));
//
//        ArrayList<String> notificationStrings = new ArrayList<String>();
//
//        for (NotificationModel notification : notifications) {
//            String notiString = notification.getTitle() + "|" + notification.getMessage() + "|" + notification.getTime();
//            notificationStrings.add(notiString);
//        }
//        return notificationStrings;
//    }

    private NotificationModel convertStringToNotification(String notiString) {
        NotificationModel notification = new NotificationModel();
        String[] stringArr = notiString.split("\\|");
        notification.setTitle(stringArr[0]);
        notification.setMessage(stringArr[1]);
        notification.setTime(stringArr[2]);

        return notification;
    }

    // location permission
//    private void checkPermission() {
//        if (checkSelfPermission(locationPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(locationPermission[1]) == PackageManager.PERMISSION_GRANTED) {
//            getCurrentLocation();
//
//        } else {
//            ActivityCompat.requestPermissions(this, locationPermission, LOCATION_PERMISSION_REQCODE);
//        }
//    }
//
//    // get user current location
//    public void getCurrentLocation() {
//
//        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        if (isGPSEnabled && isNetworkEnabled) {
//            if (locationManager != null) {
//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                if(location != null){
//                    calculateDistance(location);
////                    Log.i("currentlocation","lat:"+location.getLatitude()+" long: "+location.getLongitude());
//                }
//                else{
//                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    if(location != null){
//                        calculateDistance(location);
//                        Log.i("CURRENT","lat: "+location.getLatitude()+" long: "+location.getLongitude());
//                    }
//                }
//            }
//        }
//        else{
//            Toast.makeText(getApplicationContext(), "Make sure you have GPS or Network service available.", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQCODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                getCurrentLocation();
//            }
//        }
//    }
//
//    // calculate distance between user and stations
//    public void calculateDistance(Location location){
//        float[] results = new float[1];
//        HashMap<String,Float> dstWithStation = new HashMap<String, Float>();
//        for(Station s: stationData){
//            Location.distanceBetween(location.getLatitude(),location.getLongitude(),s.getLatitude(),s.getLongitude(),results);
//            dstWithStation.put(s.getId(),results[0]);
//        }
//
//        Comparator<Map.Entry<String,Float>> valueComparator = new Comparator<Map.Entry<String, Float>>() {
//            @Override
//            public int compare(Map.Entry<String, Float> t1, Map.Entry<String, Float> t2) {
//                return t1.getValue().compareTo(t2.getValue());
//            }
//        };
//
//        Set<Map.Entry<String, Float>> stationDistanceSet = dstWithStation.entrySet();
//        List<Map.Entry<String, Float>> stationDistanceList = new ArrayList<>(stationDistanceSet);
//
//        // get nearest station
//        Collections.sort(stationDistanceList,valueComparator);
//        String nearestStation = stationDistanceList.get(0).getKey();
//        Log.i("NEAREST STATION",nearestStation);
//
//        getCurrentWBGTData(nearestStation);
////        getXDayForecast(nearestStation);
////        getXHourForecast(nearestStation);
//    }
//
//    // get current wbgt value of nearest station by calling api
//    public void getCurrentWBGTData(String stationId){
//        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
//        Call<Object> callApi = apiInterface.getCurrentWBGT(stationId);
//        callApi.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> call, Response<Object> response) {
//                Object obj = response.body();
//                JSONArray jsonArray;
//                try {
//                    jsonArray = new JSONArray(obj.toString());
//                    JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
//                    Optional<String> station= stationData.stream().filter(s -> s.getId() == stationId).map(Station::getName).findFirst();
//                    currentWbgtValue = jsonObject.get("WBGT").toString();
//                    currentStationName = station.get();
//                    Log.i("Current WBGT", jsonObject.toString());
//
//                } catch (JSONException e) {
//                    Log.e("Error",e.getMessage());
//                    jsonArray = null;
//                }
//
////                createNotificationChannel();
////                createNotification(35);
//            }
//
//            @Override
//            public void onFailure(Call<Object> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

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


    // get user current location
    public void getCurrentLocation() {

//    public void getXHourForecast(String stationId){
//        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
//        Call<Object> callApi = apiInterface.getXHourForecast(12,stationId);
//        callApi.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> call, Response<Object> response) {
//                Object obj = response.body();
//                JSONArray jsonArray;
//                JSONObject jsonObject;
//                try {
//                    jsonArray = new JSONArray(obj.toString());
//                    jsonObject = new JSONObject(jsonArray.get(0).toString());
//                    Log.i("json",jsonObject.toString());
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                Log.i("XHourForecast",obj.toString());
////                createNotificationChannel();
////                createNotification(35);
//                Intent intent = new Intent(MainActivity.this, FirebaseNotificationReceiver.class);
//                startService(intent);
//            }
//
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    lastKnownLocation = task.getResult();
//                    if (lastKnownLocation != null) {
//                        calculateDistance(lastKnownLocation);
//                    }
//                }
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQCODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

//                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//                locationResult = fusedLocationClient.getLastLocation();
                getCurrentLocation();
            }
        }
    }

    // calculate distance between user and stations
    public void calculateDistance(Location location){
        float[] results = new float[1];
        HashMap<String,Float> dstWithStation = new HashMap<String, Float>();

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),banyanStation.getLatitude(),banyanStation.getLongitude(),results);
        dstWithStation.put(banyanStation.getId(),results[0]);

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),westCoastStation.getLatitude(),westCoastStation.getLongitude(),results);
        dstWithStation.put(westCoastStation.getId(),results[0]);

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),sentosaStation.getLatitude(),sentosaStation.getLongitude(),results);
        dstWithStation.put(sentosaStation.getId(),results[0]);

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),clementiStation.getLatitude(),clementiStation.getLongitude(),results);
        dstWithStation.put(clementiStation.getId(),results[0]);

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),eastCoastStation.getLatitude(),eastCoastStation.getLongitude(),results);
        dstWithStation.put(eastCoastStation.getId(),results[0]);

        Location.distanceBetween(location.getLatitude(),location.getLongitude(),kimChuanStation.getLatitude(),kimChuanStation.getLongitude(),results);
        dstWithStation.put(kimChuanStation.getId(),results[0]);
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

        getCurrentWBGTData(nearestStation);
        getXDayForecast(nearestStation);
        getXHourForecast(nearestStation);
    }

    // get current wbgt value of nearest station by calling api
    public void getCurrentWBGTData(String stationId){
        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
        Call<Object> callApi = apiInterface.getCurrentWBGT(stationId);
        callApi.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Object obj = response.body();
                Log.i("Current WBGT",obj.toString());
//                createNotificationChannel();
//                createNotification(35);
                Intent intent = new Intent(MainActivity.this, FirebaseNotificationReceiver.class);
                startService(intent);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later."+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getXDayForecast(String stationId){
        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
        Call<Object> callApi = apiInterface.getXDayForecast(5,stationId);
        callApi.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Object obj = response.body();
                Log.i("XDayForecast",obj.toString());
//                createNotificationChannel();
//                createNotification(35);
                Intent intent = new Intent(MainActivity.this, FirebaseNotificationReceiver.class);
                startService(intent);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later."+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getXHourForecast(String stationId){
        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
        Call<Object> callApi = apiInterface.getXHourForecast(12,stationId);
        callApi.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Object obj = response.body();
                Log.i("XHourForecast",obj.toString());
//                createNotificationChannel();
//                createNotification(35);
                Intent intent = new Intent(MainActivity.this, FirebaseNotificationReceiver.class);
                startService(intent);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later."+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}