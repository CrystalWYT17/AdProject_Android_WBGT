package iss.ca.wbgt;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.lifecycle.ViewModelProvider;
import iss.ca.wbgt.fragment.AboutFragment;
import iss.ca.wbgt.fragment.FaqFragment;
import iss.ca.wbgt.fragment.MainFragment;
import iss.ca.wbgt.fragment.NotificationFragment;
import iss.ca.wbgt.fragment.StationFragment;
import iss.ca.wbgt.model.NotificationModel;
import iss.ca.wbgt.model.UserCurrentData;
import iss.ca.wbgt.receiver.ApiReceiver;
import iss.ca.wbgt.service.LocationService;
import iss.ca.wbgt.viewModel.StationDataViewModel;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String packageName = "iss.ca.wbgt";

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

    private final String apiKey = BuildConfig.MAPS_API_KEY;
    private PlacesClient placesClient;

    private List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

    private String[] locationPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private String[] backgroundPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private int BACKGROUND_LOCATION_REQCODE = 3333;
    private String[] notificationPermission = {Manifest.permission.POST_NOTIFICATIONS};
    private Map<String,List<String>> dayForecast = new HashMap<>();
    private LocationService locationService = new LocationService();
    private UserCurrentData currentData = new UserCurrentData();

    private StationDataViewModel viewModel;

    protected BroadcastReceiver apiReceiver = new ApiReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("CurrentWbgtCompleted")){
                currentData.setStationName(intent.getStringExtra("currentStationName"));
                currentData.setWbgtValue(intent.getStringExtra("currentWbgt"));
                currentData.setStationId(intent.getStringExtra("currentStationId"));
                viewModel.setUserCurrentData(currentData);

                writeToSharedPreference("currentData");
            }
            if(action.equals("DayForecastCompleted")) {

                dayForecast = (Map<String, List<String>>) intent.getSerializableExtra("dayForecast");
                writeToSharedPreference("dayForecast");

            }

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

        // create view model object
        viewModel = new ViewModelProvider(this).get(StationDataViewModel.class);

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

        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        Fragment mainFragment = new MainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    protected void onStart() {
        register();
        checkLocationPermission();
        checkNotificationPermission();
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == BACKGROUND_LOCATION_REQCODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getUserCurrentLocation();
            }
        }
        else{
            if(grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                getUserCurrentLocation();
            }
        }
    }

    public void checkLocationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (checkSelfPermission(backgroundPermission[0]) == PackageManager.PERMISSION_GRANTED || (checkSelfPermission(locationPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(locationPermission[1]) == PackageManager.PERMISSION_GRANTED)) {
                getUserCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(this, backgroundPermission, BACKGROUND_LOCATION_REQCODE);
            }
        }
    }

    public void getUserCurrentLocation(){
        locationService.getNearestStation(this);
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
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.permission_settings){
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
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

    public void writeToSharedPreference(String state){

        SharedPreferences shr = getSharedPreferences("wbgt_main_fragment",MODE_PRIVATE);
        SharedPreferences.Editor editor = shr.edit();
        if(state.equals("currentData")){
            if(!currentData.getWbgtValue().isEmpty()){
                editor.putString("currentStationName",currentData.getStationName());
                editor.putString("currentStationId",currentData.getStationId());
                editor.putString("currentWbgt",currentData.getWbgtValue());
            }
        }
        else{
            String dayForecastString = convertMapToString(dayForecast);
            editor.putString("dayForecast",dayForecastString);
        }

        editor.commit();
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
//        filter.addAction("AlarmWakeUp");
        registerReceiver(apiReceiver,filter);
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(apiReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}