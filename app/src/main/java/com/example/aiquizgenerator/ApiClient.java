package com.example.aiquizgenerator;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.1.6:5000"; // Replace with your server IP

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void post(String endpoint, String jsonBody, ApiCallback callback) {
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
                Log.e("ApiClient", "Request Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }
}
