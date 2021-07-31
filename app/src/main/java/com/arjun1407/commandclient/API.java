package com.arjun1407.commandclient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {
    
    @POST("cmd")
    Call<JSONObject> sendCmd(@Header("request") JSONObject object);
}
