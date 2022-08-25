package com.andromeda.calloutmanager;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalloutManager {

    private static final String TAG = "Callout";

    public interface ResponseListener {
        void onSuccess(String response);

        void onError(String response);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //main api callout
    public static void makeCall(String url, String method, JSONObject params, ResponseListener responseListener) {
        //execute request in a new thread
        new Thread(() -> {
            try {
                //wait until token is refreshed
                while (!Session.isTokenValid) {
                }
                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder reqBuilder = new Request.Builder();
                reqBuilder.url(url); //url
                RequestBody body = RequestBody.create(JSON, params.toString());
                reqBuilder.addHeader("Authorization", "Bearer " + Session.ACCESS_TOKEN);
                if (method.equals("POST")) {
                    reqBuilder.post(body);
                }
                Request request = reqBuilder.build();
                Response response = okHttpClient.newCall(request).execute();
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    if (responseListener != null)
                        responseListener.onSuccess(responseData);
                } else {
                    if (!isTokenValid(responseData)) {
                        Session.isTokenValid = false;
                        Session.storeAccessToken(url, responseListener);
                        // re enqueue request as token will be refreshed
                        makeCall(url, method, params, responseListener);
                    } else if (responseListener != null)
                        responseListener.onError(responseData);
                }
                Log.e(TAG, "makeCall: isSuccessful=" + response.isSuccessful() + " response=" + responseData);
            } catch (IOException e) {
                e.printStackTrace();
                if (responseListener != null)
                    responseListener.onError(e.getMessage());
            }
        }).start();
    }

    // for no response listener
    public static void makeCall(String url, String method, JSONObject params) {
        makeCall(url, method, params, null);
    }

    private static boolean isTokenValid(String responseData) {
        try {
            JSONArray array = new JSONArray(responseData);
            JSONObject obj = array.getJSONObject(0);
            String message = obj.getString("message");
            if (message.equals("INVALID_HEADER_TYPE") || message.equals("Session expired or invalid")) {
                Log.e(TAG, "isTokenValid: not valid");
                return false;
            }
        } catch (JSONException ignored) {
        }
        return true;
    }
}
