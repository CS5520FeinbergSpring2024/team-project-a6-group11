package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchMusicActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextSearch;
    private String accessToken;
    private OkHttpClient client;
    private FragmentManager fragmentManager;
    private ArrayList<TrackItem> searchResults;
    private String searchType;
    private TextView titleTextView;
    private TextView genreTextView;
    private TextView artistTextView;
    private TextView noResultsTextView;
    private ProgressBar progressBar;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        accessToken = MainActivity.accessToken;
        client = new OkHttpClient();

        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.setOnKeyListener(this::onClickEditTextSearch);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(view -> onClickSearch());

        fragmentManager = getSupportFragmentManager();
        searchResults = new ArrayList<>();

        titleTextView = findViewById(R.id.titleTextView);
        genreTextView = findViewById(R.id.genreTextView);
        artistTextView = findViewById(R.id.artistTextView2);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        progressBar = findViewById(R.id.progressBar);

        inputMethodManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        setSearchType("title");
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
        titleTextView.setText(searchType.equals("title") ? "• Title" : "  Title");
        genreTextView.setText(searchType.equals("genre") ? "• Genre" : "  Genre");
        artistTextView.setText(searchType.equals("artist") ? "• Artist" : "  Artist");

        editTextSearch.setHint("Search by " + searchType + "...");
    }

    private void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }

    private boolean onClickEditTextSearch(View view, int keyCode, KeyEvent keyEvent) {
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            hideKeyboard();
            editTextSearch.clearFocus();
            return true;
        }
        return false;
    }

    private void onClickSearch() {
        String searchQuery = editTextSearch.getText().toString();
        if (searchQuery.trim().isEmpty()) { // Prevent search queries with only whitespaces
            Toast toast = Toast.makeText(this, "Please enter " + (searchType.equals("artist") ? "an artist" : "a " + searchType) + " to search for!", Toast.LENGTH_SHORT);
            toast.show();

            return;
        }

        switch (searchType) {
            case "title":
                getTracksByTitle(searchQuery);
                break;
            case "genre":
                getTracksByGenre(searchQuery);
                break;
            case "artist":
                getTracksByArtist(searchQuery);
                break;
        }

        hideKeyboard();
    }

    // 3cEYpjA9oz9GiPac4AsH4n
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
                        searchResults = parseTracks(tracks);

                        updateSearchResults(searchResults);
                    }
                } catch (JSONException jsonException) {
                    runOnUiThread(() -> {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please ensure that the playlist id that you entered is valid!", Toast.LENGTH_LONG);
                        toast.show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }

    private void getSavedTracks() {
        Request request = new Request.Builder().url("https://api.spotify.com/v1/me/tracks")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        String responseString = response.body().string();
                        JSONObject tracks = new JSONObject(responseString);
                        ArrayList<TrackItem> trackItems = parseTracks(tracks);

                        updateSearchResults(trackItems);
                    }
                } catch (JSONException jsonException) {
                    runOnUiThread(() -> {
                        Toast toast = Toast.makeText(getApplicationContext(), "Could not load saved tracks.", Toast.LENGTH_LONG);
                        toast.show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }

    private String encodeString(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private void getTracksByTitle(String title) {
        getFilteredTracks("track:", title);
    }

    private void getTracksByGenre(String genre) {
        getFilteredTracks("genre:", genre);
    }

    private void getTracksByArtist(String artist) {
        getFilteredTracks("artist:", artist);
    }

    private void getFilteredTracks(String filter, String query) {
        String encodedQuery = encodeString(query);

        if (encodedQuery == null) {
            Toast.makeText(this, "Please provide a valid search query.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder().url("https://api.spotify.com/v1/search?type=track&q=" + filter + encodedQuery)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        progressBar.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        ArrayList<TrackItem> trackItems = new ArrayList<>();

                        String responseString = response.body().string();
                        JSONObject tracksObject = new JSONObject(responseString);
                        JSONObject tracks = tracksObject.getJSONObject("tracks");
                        JSONArray items = tracks.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject track = items.getJSONObject(i);
                            TrackItem trackItem = getTrackData(track);
                            trackItems.add(trackItem);
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            updateSearchResults(trackItems);
                        });
                    }
                } catch (JSONException jsonException) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast toast = Toast.makeText(getApplicationContext(), "Could not load search results.", Toast.LENGTH_LONG);
                        toast.show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }

    private TrackItem getTrackData(JSONObject track) throws JSONException {
        String trackName = track.getString("name");
        JSONArray images = track.getJSONObject("album").getJSONArray("images");
        String trackIconURL = images.getJSONObject(2).getString("url"); // index 2 contains the url for the small icon

        JSONArray trackArtists = track.getJSONArray("artists");
        ArrayList<String> artists = new ArrayList<>();

        for (int j = 0; j < trackArtists.length(); j++) {
            JSONObject artist = trackArtists.getJSONObject(j);
            String artistName = artist.getString("name");
            artists.add(artistName);
        }

        return new TrackItem(trackName, String.join(", ", artists), track.get("preview_url").toString(), trackIconURL);
    }

    private ArrayList<TrackItem> parseTracks(JSONObject tracks) throws JSONException {
        JSONArray items = tracks.getJSONArray("items");
        ArrayList<TrackItem> trackItems = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject track = item.getJSONObject("track");
            TrackItem trackItem = getTrackData(track);

            trackItems.add(trackItem);
        }

        return trackItems;
    }

    private void updateSearchResults(ArrayList<TrackItem> trackItems) {
        if (trackItems.size() == 0) {
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            noResultsTextView.setVisibility(View.INVISIBLE);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        TracklistFragment tracklistFragment = TracklistFragment.newInstance(trackItems);
        transaction.replace(R.id.fragmentContainerView, tracklistFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.titleTextView) {
            setSearchType("title");
        } else if (id == R.id.genreTextView) {
            setSearchType("genre");
        } else if (id == R.id.artistTextView2) {
            setSearchType("artist");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}