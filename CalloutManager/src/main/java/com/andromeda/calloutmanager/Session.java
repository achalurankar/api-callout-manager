package com.andromeda.calloutmanager;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Session {


    private static final String TAG = "SESSION";
    public static String ACCESS_TOKEN = "";
    public static boolean isTokenValid = false;

    public static void storeAccessToken(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.e(TAG, "onResponse: isSuccessful " + response.isSuccessful());
                String responseData = response.body().string();
                if(response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(responseData);
                        Session.ACCESS_TOKEN = object.getString("access_token");
                        isTokenValid = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG, "onResponse: " + responseData);
            }
        });
    }
}
