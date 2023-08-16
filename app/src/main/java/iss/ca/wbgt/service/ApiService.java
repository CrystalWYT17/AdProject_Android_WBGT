package iss.ca.wbgt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import iss.ca.wbgt.api.ApiClient;
import iss.ca.wbgt.api.ApiInterface;
import iss.ca.wbgt.model.Station;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiService extends Service {
    private String currentStationName;
    private String currentWbgtValue;
    private Map<String,List<String>> dayForecast = new HashMap<>();
    private List<Station> stationData = new ArrayList<>();
    public ApiService(){
        // empty constructor
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String stationId = intent.getStringExtra("stationId");
        stationData = (List<Station>) intent.getSerializableExtra("stationData");
        getCurrentWBGTData(stationId);
        getXDayForecast(stationId);
        return super.onStartCommand(intent, flags, startId);
    }

    // get current wbgt value of nearest station by calling api
    public void getCurrentWBGTData(String stationId){

        Thread bkThread = new Thread(() -> {
            ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
            Call<Object> callApi = apiInterface.getCurrentWBGT(stationId);
            callApi.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                    Object obj = response.body();
                    JSONArray jsonArray;
                    try {
                        if(obj != null){
                            jsonArray = new JSONArray(obj.toString());
                            JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
                            Optional<String> station= stationData.stream().filter(s -> s.getId().equals(stationId)).map(Station::getName).findFirst();

                            currentWbgtValue = jsonObject.get("WBGT").toString();
                            if(station.isPresent()){
                                currentStationName = station.get();
                            }
                            else{
                                currentStationName = "";
                            }

                        }
                        else{
                            currentStationName = "";
                            currentWbgtValue = "";
                        }

                    } catch (JSONException e) {
                        currentStationName = "";
                        currentWbgtValue = "";
                    }

                    Intent intent = new Intent();
                    intent.setAction("CurrentWbgtCompleted");
                    intent.putExtra("currentStationName",currentStationName);
                    intent.putExtra("currentWbgt",currentWbgtValue);
                    intent.putExtra("currentStationId",stationId);
                    sendBroadcast(intent);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later.", Toast.LENGTH_SHORT).show();
                }
            });

        });
        bkThread.run();

    }

    public void getXDayForecast(String stationId){

        Thread bkThread = new Thread(() -> {
            ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
            Call<Object> callApi = apiInterface.getXDayForecast(7,stationId);
            callApi.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    Object obj = response.body();
                    JSONArray jsonArray;
                    JSONObject jsonObject;

                    try {
                        if(obj != null){
                            jsonArray = new JSONArray(obj.toString());

                            if(jsonArray.length() > 0){
                                for(int i=0; i<jsonArray.length(); i++){
                                    List<String> wbgtList = new ArrayList<String>();
                                    jsonObject = new JSONObject(jsonArray.get(i).toString());
                                    String day = checkDay(jsonObject.get("timestamp").toString());
                                    wbgtList.add(String.valueOf(jsonObject.getDouble("min_wbgt")));
                                    wbgtList.add(String.valueOf(jsonObject.getDouble("max_wbgt")));
                                    dayForecast.put(day,wbgtList);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        dayForecast = new HashMap<>();
                    } catch (ParseException e){
                        dayForecast = new HashMap<>();
                    }

                    Intent intent = new Intent();
                    intent.setAction("DayForecastCompleted");
                    intent.putExtra("dayForecast",(Serializable) dayForecast);
                    sendBroadcast(intent);

                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Unexpected event happens while retrieving day forecast. Try again later.", Toast.LENGTH_SHORT).show();
                }
            });

        });
        bkThread.run();

    }

    // get day from timestamp
    public String checkDay(String timeStamp) throws ParseException {

        String checkedDay="";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date now = new Date();
        Date apiData = inputFormat.parse(timeStamp);

        Date parsedNow = outputFormat.parse(outputFormat.format(now));
        Date parsedApiDate = outputFormat.parse(outputFormat.format(apiData));

        long diffInMilli = parsedApiDate.getTime() - parsedNow.getTime();
        long diff = TimeUnit.MILLISECONDS.toDays(diffInMilli);
        LocalDateTime day = LocalDateTime.now().plusDays(diff);
        if(diff == 0){
            checkedDay = String.valueOf(day.getDayOfWeek());
        }
        else if(diff == 1){
            checkedDay = String.valueOf(day.getDayOfWeek());
        }
        else if(diff == 2){
            checkedDay = String.valueOf(day.getDayOfWeek());
        }
        else if(diff == 3){
            checkedDay = String.valueOf(day.getDayOfWeek());
        }
        else{
            checkedDay = String.valueOf(day.getDayOfWeek());
        }
        return checkedDay;
    }

    //get hour from datetime string
    private int getHour(String datetimeString){
        int hour = 0;
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(datetimeString, formatter);
            hour = dateTime.getHour();

        }catch (Exception e){
            e.printStackTrace();
        }
        return hour;
    }

    public LinkedHashMap<Integer, List<Double>> getXHourForecastMultiStation(String stationId) {
        CompletableFuture<Map<Integer, List<Double>>> future = new CompletableFuture<>();

        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
        Call<Object> callApi = apiInterface.getXHourForecast(24, stationId);

        try {
            Response<Object> response = callApi.execute();
            if (response.isSuccessful()) {
                LinkedHashMap<Integer, List<Double>> hourlyForecast = new LinkedHashMap<>();
                Object obj = response.body();
                JSONArray jsonArray = new JSONArray(obj.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                    String time = jsonObject.get("timestamp").toString();
                    String predicted_wbgt = jsonObject.get("predicted_value").toString();
                    double wbgt = Double.parseDouble(predicted_wbgt);
                    int hour = getHour(time);
                    if (hourlyForecast.containsKey(hour)) {
                        hourlyForecast.get(hour).add(wbgt);
                    } else {
                        List<Double> values = new ArrayList<>();
                        values.add(wbgt);
                        hourlyForecast.put(hour, values);
                    }
                }
                return hourlyForecast;
            } else {
                // Handle the failure scenario here
                return new LinkedHashMap<>();
            }
        } catch (Exception e) {
            // Handle exceptions here
        }

        // Return a default or empty map in case of failure or exceptions
        return new LinkedHashMap<>();
    }
}
