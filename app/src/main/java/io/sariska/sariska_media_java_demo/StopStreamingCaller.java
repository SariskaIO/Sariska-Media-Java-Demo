package io.sariska.sariska_media_java_demo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StopStreamingCaller {
    protected static void stopStreaming(String token, String roomName) throws IOException {
        OkHttpClient client = new OkHttpClient();
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = "https://streaming.sariska.io/user/stopRecording?room_name="+roomName;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", token)
                .build();
        try(Response response = client.newCall(request).execute()){
            assert response.body() != null;
            String responseString = response.body().string();
            responseString = "[" + responseString + "]";
            JSONArray array = new JSONArray(responseString);
            boolean stopped;
            for(int i=0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                stopped = object.getBoolean("started");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Oops, there was a problem");
        }
    }
}
