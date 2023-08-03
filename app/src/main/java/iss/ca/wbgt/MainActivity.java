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

import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private boolean isClickedNotification = false;

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

        Drawable originalDrawable = AppCompatResources.getDrawable(MainActivity.this, R.drawable.notification);

        ImageView notificationBell = findViewById(R.id.notification_badge);
        //switch to notification fragment
        notificationBell.setOnClickListener(v->{
            isClickedNotification = !isClickedNotification;
            if(isClickedNotification){
                Drawable tintedDrawable = originalDrawable.getConstantState().newDrawable().mutate();
                DrawableCompat.setTint(tintedDrawable, Color.GREEN);
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
}