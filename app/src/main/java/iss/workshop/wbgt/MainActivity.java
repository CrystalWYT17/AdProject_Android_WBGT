package iss.workshop.wbgt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int FOREGROUND_PERMISSION_REQCODE = 1111;
//    private int BACKGROUND_PERMISSION_REQCODE = 2222;

    private final String apiKey = BuildConfig.MAPS_API_KEY;

    private String[] foregroundPermission = {android.Manifest.permission.ACCESS_COARSE_LOCATION};
//    private String[] backgroundPermission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    private PlacesClient placesClient;

    private List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
    private FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final String apiKey = BuildConfig.MAPS_API_KEY;
        Places.initialize(getApplicationContext(), apiKey);
        placesClient = Places.createClient(this);

        checkPermission();
    }

    public void checkPermission(){
        if(checkSelfPermission(foregroundPermission[0]) == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this,"Foreground Permission already granted.",Toast.LENGTH_LONG).show();
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this,"Find Current Place Done",Toast.LENGTH_LONG).show();
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
            ActivityCompat.requestPermissions(this,foregroundPermission,FOREGROUND_PERMISSION_REQCODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == FOREGROUND_PERMISSION_REQCODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Foreground Permission successfully granted.",Toast.LENGTH_LONG).show();
            }
        }
    }
}