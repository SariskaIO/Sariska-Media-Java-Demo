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
    protected static String generateToken(String userID) throws IOException {
        OkHttpClient client = new OkHttpClient();
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = "https://api.sariska.io/api/v1/misc/generate-token";
        String json = "{\n" +
                "    \"apiKey\": \"249202aabed00b41363794b526eee6927bd35cbc9bac36cd3edcaa\",\n" +
                "    \"user\": {\n" +
                "        \"id\": \"dsds\",\n" +
                "        \"name\": \"SomeOne\",\n" +
                "        \"moderator\": true,\n" +
                "        \"email\": \"dipak@work.com\",\n" +
                "        \"avatar\":\"null\"\n" +
                "    }\n" +
                "}";
        Log.d("Generated Token", "generateToken: ");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try(Response response = client.newCall(request).execute()){
            String responseString = response.body().string();
            responseString = "[" + responseString + "]";
            JSONArray array = new JSONArray(responseString);
            String finalResponse = null;
            for(int i=0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                finalResponse = object.getString("token");
            }
            return finalResponse;
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("This cannot be done");
            return null;
        }
    }
}

