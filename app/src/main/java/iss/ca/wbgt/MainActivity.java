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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private boolean isClickedNotification = false;
    private Drawable originalDrawable;
    private Drawable tintedDrawable;
    private ImageView notificationBell;

    //To Store Notification
    File mTargetFile;
    private ArrayList<Notification> notifications = new ArrayList<Notification>();
    private ArrayList<Notification> notificationsTest = new ArrayList<Notification>();

    private int LOCATION_PERMISSION_REQCODE = 1111;
//    private int BACKGROUND_PERMISSION_REQCODE = 2222;

    private final String apiKey = BuildConfig.MAPS_API_KEY;
    private PlacesClient placesClient;

    private List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
    private FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

    private String[] locationPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION};
//    private String[] backgroundPermission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //notification icon

        originalDrawable = AppCompatResources.getDrawable(MainActivity.this, R.drawable.notification);
        tintedDrawable = originalDrawable.getConstantState().newDrawable().mutate();
        DrawableCompat.setTint(tintedDrawable, Color.GREEN);

        notificationBell = findViewById(R.id.notification_badge);
        //switch to notification fragment
        notificationBell.setOnClickListener(v->{
            isClickedNotification = !isClickedNotification;
            if(isClickedNotification){
                notificationBell.setImageDrawable(tintedDrawable);
                //switch to notification fragment
                Fragment notificationFragment = new NotificationFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right
                ).replace(R.id.fragment_container, notificationFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }else{
                notificationBell.setImageDrawable(originalDrawable);
                Fragment mainFragment = new MainFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_left_to_right, R.anim.exit_left_to_right,
                        R.anim.enter_right_to_left, R.anim.exit_right_to_left
                ).replace(R.id.fragment_container, mainFragment).commit();
            }
        });

        //store notifications to file
        String folder = "NotificationsTest";
        String fileName = "notification_list";
        mTargetFile = new File(getFilesDir(), folder+"/"+fileName);
        writeToFile();
        readFromFile();
        System.out.println("NotiTest"+notificationsTest);



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
        checkPermission();
        super.onStart();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(item.getItemId() == R.id.nav_station){
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
        } else if (item.getItemId()==R.id.nav_faq) {
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
        if (isClickedNotification){
            notificationBell.setImageDrawable(originalDrawable);
        }

    }

    //permission
    private void checkPermission(){
        if(checkSelfPermission(locationPermission[0]) == PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(this,"Location Permission already granted.",Toast.LENGTH_LONG).show();
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    //Toast.makeText(this,"Find Current Place Done",Toast.LENGTH_LONG).show();
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i("CurrentPlace",String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                }
                else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("CurrentPlaceError", "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(this,locationPermission,LOCATION_PERMISSION_REQCODE);
        }
    }

    protected void writeToFile(){
        ArrayList<String> notificationString = getNotificationString();
        try {
            File parent = mTargetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IllegalStateException("Could not create dir: "+ parent);
            }
            FileOutputStream fos = new FileOutputStream(mTargetFile);
            for (String notification: notificationString){
                fos.write((notification+"\n").getBytes());
            }
            fos.close();
            Toast.makeText(getApplicationContext(), "Write File OK!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void readFromFile(){
        try{
            FileInputStream fis = new FileInputStream(mTargetFile);
            DataInputStream dis = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            //Assign the lines from buffer reader to strline and check if it is null
            while ((strLine = br.readLine())!= null){
                notificationsTest.add(convertStringToNotification(strLine));
            }
            dis.close();
            //mInputTxt.setText(data);
            Toast.makeText(this, "Read File OK!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    protected ArrayList<String> getNotificationString(){
        //initialize some notification data for testing purpose
        notifications.add(new Notification("Title1", "Body1", "Time1"));
        notifications.add(new Notification("Title2", "Body2", "Time2"));
        notifications.add(new Notification("Title3", "Body3", "Time3"));
        notifications.add(new Notification("Title4", "Body4", "Time4"));
        notifications.add(new Notification("Title5", "Body5", "Time5"));
        notifications.add(new Notification("Title6", "Body6", "Time6"));
        notifications.add(new Notification("Title7", "Body7", "Time7"));
        notifications.add(new Notification("Title8", "Body8", "Time8"));

        ArrayList<String> notificationStrings = new ArrayList<String>();

        for(Notification notification: notifications){
            String notiString = notification.getTitle()+"|"+notification.getMessage()+"|"+notification.getTime();
            notificationStrings.add(notiString);
        }
        return notificationStrings;
    }

    private Notification convertStringToNotification(String notiString){
        Notification notification = new Notification();
        String[] stringArr = notiString.split("\\|");
        notification.setTitle(stringArr[0]);
        notification.setMessage(stringArr[1]);
        notification.setTime(stringArr[2]);

        return notification;
    }
}