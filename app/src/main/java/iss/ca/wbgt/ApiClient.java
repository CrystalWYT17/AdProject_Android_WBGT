package iss.ca.wbgt;

import com.android.volley.RequestQueue;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private RequestQueue requestQueue;

    private static Retrofit retrofit;

//    private static final String BASE_URL = "http://127.0.0.1:8081";

    private static final String BASE_URL ="https://api.data.gov.sg";
    //empty constructor
    public ApiClient(){
    }

    public static Retrofit buildRetrofitApi(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit;
    }
}
