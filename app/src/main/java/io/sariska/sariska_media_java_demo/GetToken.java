package io.sariska.sariska_media_java_demo;

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

public class GetToken {

    public interface HttpRequestCallback {
        void onResponse(String response) throws JSONException;
        void onFailure(Throwable throwable);
    }

    protected static void generateToken(String userID, final HttpRequestCallback callback) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://api.sariska.io/api/v1/misc/generate-token";
                OkHttpClient client = new OkHttpClient();
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String json = "{\n" +
                        "    \"apiKey\": \"249202aabed00b41363794b526eee6927bd35cbc9bac36cd3edcaa\",\n" +
                        "    \"user\": {\n" +
                        "        \"name\": \""+userID+"\",\n" +
                        "        \"moderator\": true,\n" +
                        "        \"email\": \"dipak@work.com\",\n" +
                        "        \"avatar\":\"null\"\n" +
                        "    }\n" +
                        "}";
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        // Handle the error response
                        callback.onFailure(new IOException("Request not successful"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle the request failure
                    callback.onFailure(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}

