package iss.ca.wbgt;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

//    @GET("/current?")
//    Call<Object> getCurrentWBGT(@Query("station_id") String stationId);

    @GET("/v1/environment/relative-humidity")
    Call<Object> getCurrentWBGT();
}
