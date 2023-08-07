package iss.ca.wbgt;

import com.android.volley.RequestQueue;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private RequestQueue requestQueue;

    private static Retrofit retrofit;

//    private static final String BASE_URL = "http://127.0.0.1:8081";
//    https://wbgtgroup9.azurewebsites.net/predict?hour=5&station_id=S50

    private static final String BASE_URL ="https://wbgtgroup9.azurewebsites.net";
    //empty constructor
    public ApiClient(){
    }

    public static Retrofit buildRetrofitApi(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit;
    }
}
