package iss.ca.wbgt.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/current?")
    Call<Object> getCurrentWBGT(@Query("station_id") String stationId);

    @GET("/day_forecast?")
    Call<Object> getXDayForecast(@Query("day") int day, @Query("station_id") String stationId);

    @GET("/predict?")
    Call<Object> getXHourForecast(@Query("hour") int hour, @Query("station_id") String stationId);

}
