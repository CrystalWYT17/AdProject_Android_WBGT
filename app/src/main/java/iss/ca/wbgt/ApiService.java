package iss.ca.wbgt;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiService extends AppCompatActivity {
    private String currentStationName;
    private String currentWbgtValue;
    private Map<String,List<String>> dayForecast = new HashMap<>();
    private List<Station> stationData = new ArrayList<>();
    private UserCurrentData currentData = new UserCurrentData();
    public ApiService(){

    }

    public UserCurrentData getCurrentData(){
        return this.currentData;
    }

    public void setStationData(List<Station> stationData){
        this.stationData = stationData;
    }

    public Map<String, List<String>> getDayForecast() {
        return dayForecast;
    }

    public void getXHourForecast(String stationId){
        ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
        Call<Object> callApi = apiInterface.getXHourForecast(12,stationId);
        callApi.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Object obj = response.body();
                JSONArray jsonArray;
                JSONObject jsonObject;
                try {
                    jsonArray = new JSONArray(obj.toString());
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.i("json",jsonObject.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.i("XHourForecast",obj.toString());
//                createNotificationChannel();
//                createNotification(35);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get current wbgt value of nearest station by calling api
    public void getCurrentWBGTData(String stationId){

        Thread bkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
                Call<Object> callApi = apiInterface.getCurrentWBGT(stationId);
                callApi.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        Object obj = response.body();
                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(obj.toString());
                            JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
                            Optional<String> station= stationData.stream().filter(s -> s.getId() == stationId).map(Station::getName).findFirst();
                            currentWbgtValue = jsonObject.get("WBGT").toString();
                            currentStationName = station.get();
                            currentData.setWbgtValue(currentWbgtValue);
                            currentData.setStationName(currentStationName);
                            Log.i("Current WBGT", jsonObject.toString());

                        } catch (JSONException e) {
                            Log.e("Error",e.getMessage());
                            jsonArray = null;
                        }

//                createNotificationChannel();
//                createNotification(35);
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
        bkThread.run();

        //        if(callApi.isExecuted()){
//
//        }
//        UserCurrentData currentData = new UserCurrentData(currentStationName,currentWbgtValue);
//        Log.i("currentData",currentData.toString());
//        return currentData;
    }

    public void getXDayForecast(String stationId){

        Thread bkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ApiInterface apiInterface = ApiClient.buildRetrofitApi().create(ApiInterface.class);
                Call<Object> callApi = apiInterface.getXDayForecast(7,stationId);
                callApi.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        Object obj = response.body();
                        JSONArray jsonArray;
                        JSONObject jsonObject;
//                Dictionary<String,Map<String,Double>> dayForecastDict = new Hashtable<>();


                        try {
                            jsonArray = new JSONArray(obj.toString());

                            if(jsonArray.length() > 0){
                                for(int i=0; i<jsonArray.length(); i++){
                                    List<String> wbgtList = new ArrayList<String>();
                                    jsonObject = new JSONObject(jsonArray.get(i).toString());
                                    String day = checkDay(jsonObject.get("timestamp").toString());
                                    wbgtList.add(String.valueOf(jsonObject.getDouble("min_wbgt")));
                                    wbgtList.add(String.valueOf(jsonObject.getDouble("max_wbgt")));
//                            minMaxWbgt.put("low",jsonObject.getDouble("min_wbgt"));
//                            minMaxWbgt.put("high",jsonObject.getDouble("max_wbgt"));
                                    dayForecast.put(day,wbgtList);
                                }
                            }
                            System.out.println("HEllo");
                            Log.i("data", dayForecast.toString());

                        } catch (JSONException e) {
                            jsonArray = null;
                        } catch (ParseException e){
                            jsonArray = null;
                        }

                        Log.i("MyData","sorry");

//                createNotificationChannel();
//                createNotification(35);
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Unexpected event happens. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        bkThread.run();


    }

    public String checkDay(String timeStamp) throws ParseException {
        String checkedDay="";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Date now = new Date();
        Date apiData = inputFormat.parse(timeStamp);

        Date parsedNow = outputFormat.parse(outputFormat.format(now));
        Date parsedApiDate = outputFormat.parse(outputFormat.format(apiData));
//        LocalDateTime currentDate = LocalDateTime.parse(now.toString(),formatter);
//        LocalDateTime apiData = LocalDateTime.parse(timeStamp,formatter);

//        String apiDate = String.format(timeStamp, formatter);
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
}
