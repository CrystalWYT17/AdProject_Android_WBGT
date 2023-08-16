package iss.ca.wbgt.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    private static final String BASE_URL ="https://wbgtgroup9.azurewebsites.net";

    public ApiClient(){
        //empty constructor
    }

    public static Retrofit buildRetrofitApi(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit;
    }
}
