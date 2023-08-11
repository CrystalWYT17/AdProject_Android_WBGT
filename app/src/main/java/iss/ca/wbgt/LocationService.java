package iss.ca.wbgt;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.core.app.ActivityCompat;

public class LocationService {

    private LocationManager locationManager;
    private Location location;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private List<Station> stationData = new ArrayList<>();
    public LocationService(){
        // empty constructor
    }

    public List<Station> storeStationData(){
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

        return stationData;

    }

    // get user current location
    public String getCurrentLocation(Context context) {

        String nearestStation = "";

        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled && isNetworkEnabled) {
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null){
                    nearestStation = calculateDistance(location);
//                    Log.i("currentlocation","lat:"+location.getLatitude()+" long: "+location.getLongitude());
                }
                else{
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null){
                        nearestStation = calculateDistance(location);
                        Log.i("CURRENT","lat: "+location.getLatitude()+" long: "+location.getLongitude());
                    }
                }
            }
        }
        else{
            Toast.makeText(context, "Make sure you have GPS or Network service available.", Toast.LENGTH_SHORT).show();
        }
        return nearestStation;

    }

    // calculate distance between user and stations
    public String calculateDistance(Location location){
        stationData = storeStationData();
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
        return stationDistanceList.get(0).getKey();

    }
}
