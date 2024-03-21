package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchMusicActivity extends AppCompatActivity {

    private String accessToken;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        accessToken = MainActivity.accessToken;
        client = new OkHttpClient();

        System.out.println(accessToken);

        getPlaylistData("3cEYpjA9oz9GiPac4AsH4n");
    }

    private void getPlaylistData(String playlistID) {
        Request request = new Request.Builder().url("https://api.spotify.com/v1/playlists/" + playlistID)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        String responseString = response.body().string();
                        JSONObject playlistData = new JSONObject(responseString);
                        JSONObject tracks = playlistData.getJSONObject("tracks");
                        JSONArray items = tracks.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject track = item.getJSONObject("track");
                            String trackName = track.getString("name");
                            JSONArray trackArtists = track.getJSONArray("artists");

                            System.out.println("Track name: " + trackName);

                            for (int j = 0; j < trackArtists.length(); j++) {
                                JSONObject artist = trackArtists.getJSONObject(j);
                                String artistName = artist.getString("name");
                                System.out.println("Track artist: " + artistName);
                            }
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }
}