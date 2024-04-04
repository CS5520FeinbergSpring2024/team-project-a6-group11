package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingleEntryActivity extends AppCompatActivity {
    OkHttpClient client;
    ImageButton albumImageView;
    TextView trackNameTextView;
    TextView artistTextView;
    TextView extraTextView;
    EditText trackEditText;
    EditText artistEditText;
    private static final String ACCESS_TOKEN = MainActivity.accessToken;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DatabaseReference userDiaryReference;
    private DateTimeFormatter dateTimeFormatter;
    private String currentDate;
    private String imageUrl;
    private String trackName;
    private String textPost;
    private boolean isPlaying = false;
    private String trackURI;
    private static String currEntryId;
    private SpotifyAppRemote mSpotifyAppRemote;

//    String sampleURL = "https://api.spotify.com/v1/search?q=First%2520Love%2520artist%253AHikaru%2520Utada&type=track&market=US&limit=1";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        albumImageView = findViewById(R.id.albumCoverButton);
        trackNameTextView = findViewById(R.id.trackTitleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        extraTextView = findViewById(R.id.postText);
        Toolbar toolbar = findViewById(R.id.diaryToolbar);
        setSupportActionBar(toolbar);

        String openedEntryDate = getIntent().getStringExtra("openedEntryDate");
        String openedEntryTrackName = getIntent().getStringExtra("openedEntryTrackName");
        String openedEntryCoverURL = getIntent().getStringExtra("openedEntryCoverURL");
        String openedEntryPostText = getIntent().getStringExtra("openedEntryPostText");

        LocalDate localDate = LocalDate.now();
        dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDate = dateTimeFormatter.format(localDate);

        if (getSupportActionBar() != null) {
            if (openedEntryDate == null) {
                getSupportActionBar().setTitle(currentDate);
            } else {
                getSupportActionBar().setTitle(openedEntryDate);
            }
        }

        if (openedEntryTrackName != null) {
            trackNameTextView.setText(openedEntryTrackName);
        }

        if (openedEntryCoverURL != null) {
            Picasso.get().load(openedEntryCoverURL).into(albumImageView);
        }

        if (openedEntryPostText != null){
            extraTextView.setText(openedEntryPostText);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = new OkHttpClient();

//        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == 0) {
//                Intent data = result.getData();
//                if (data != null) {
//                    String trackName = data.getStringExtra("trackName");
//                    String trackArtists = data.getStringExtra("trackArtists");
//
//                    trackEditText.setText(trackName);
//                    artistEditText.setText(trackArtists);
//                }
//            }
//        });

//        SpotifyAppRemote.connect(this, new ConnectionParams.Builder(MainActivity.CLIENT_ID)
//                .setRedirectUri(MainActivity.REDIRECT_URI)
//                .showAuthView(true)
//                .build(), new Connector.ConnectionListener() {
//            @Override
//            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                mSpotifyAppRemote = spotifyAppRemote;
//                Log.d("SpotifyAppRemote", "Connected to Spotify");
//                albumImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (isPlaying == false) playMusic();
//                        else if(isPlaying == true) stopMusic();
//                    }
//                });
//
//                mSpotifyAppRemote.getPlayerApi().
//                        subscribeToPlayerState()
//                        .setEventCallback(playerState -> {
//                            boolean isPaused = playerState.isPaused;
//                            if (isPaused) {
//                                Log.d("Playstate", "Song is paused");
//                            } else {
//                                Log.d("Playstate", "Song is playing");
//                            }
//                        });
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                Log.e("SpotifyAppRemote", "Failed to connect to Spotify", throwable);
//            }
//        });




        userDiaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
    }

    public void updateEntryData(String newTrack, String newArtist, String newTextPost) {
        String apiURL = "https://api.spotify.com/v1/search";
        try {
            String trackEncoded = URLEncoder.encode(newTrack, "UTF-8");
            String artistEncoded = URLEncoder.encode(newArtist, "UTF-8");
            String query = "track:" + trackEncoded + " artist:" + artistEncoded;

            apiURL += "?q=" + query + "&type=track&market=US&limit=1";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder().url(apiURL).addHeader("Authorization", "Bearer " + ACCESS_TOKEN).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(SingleEntryActivity.this, "Failed to search track", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    Log.d("SingleEntry", "run on uni thread");
                    try {
                        String jsonData = response.body().string();
                        JSONObject resposneObject = new JSONObject(jsonData);
                        Log.d("SingleEntry", "JSON Data: " + jsonData);
                        JSONObject tracksObject = resposneObject.getJSONObject("tracks");
                        JSONArray itemsArray = tracksObject.getJSONArray("items");

                        if (itemsArray.length() > 0) {
                            JSONObject firstItem = itemsArray.getJSONObject(0);
                            trackName = firstItem.getString("name");
                            Log.d("SingleEntry", "Track Name: " + trackName);
                            trackNameTextView.setText(trackName);

                            String trackUri = firstItem.getString("uri");
                            Log.d("SingleEntry", "Track uri: " + trackURI);
                            trackURI = trackUri;

                            JSONObject album = firstItem.getJSONObject("album");
                            JSONArray imagesArray = album.getJSONArray("images");
                            if (imagesArray.length() > 0) {
                                JSONObject firstImage = imagesArray.getJSONObject(0);
                                imageUrl = firstImage.getString("url");
                                Log.d("SingleEntry", "Image URL: " + imageUrl);
                                Picasso.get().load(imageUrl).into(albumImageView);
                            }

                            JSONArray artistsArray = firstItem.getJSONArray("artists");

                            if (artistsArray.length() > 0) {
                                StringBuilder concatenatedArtists = new StringBuilder();
                                for (int i = 0; i < artistsArray.length(); i++) {
                                    JSONObject artistObject = artistsArray.getJSONObject(i);
                                    String artistName = artistObject.getString("name");

                                    concatenatedArtists.append(artistName);
                                    if (i < artistsArray.length() - 1) {
                                        concatenatedArtists.append(", ");
                                    }
                                    Log.d("SingleEntry", "Artist Name: " + artistName);

                                }
                                String allArtists = concatenatedArtists.toString();
                                artistTextView.setText(allArtists);
                            }
                            textPost = newTextPost;
                            extraTextView.setText(textPost);


                        } else {
                            Toast.makeText(SingleEntryActivity.this, "No tracks found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | IOException exception) {
                        runOnUiThread(() -> {
                            Toast toast = Toast.makeText(getApplicationContext(), "Failed to update the playlist information!", Toast.LENGTH_SHORT);
                            toast.show();
                        });
                    }
                    checkEntryExists(currentDate);
                });
            }
        });
    }

    private void checkEntryExists(String currentDate) {
        userDiaryReference.orderByChild("date").equalTo(currentDate).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            DataSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String entryKey = dataSnapshot.getKey();
                                    Log.d("Firebase", "Entry with key " + entryKey + " exists. Updating entry.");
                                    updateExistingEntry(entryKey, trackName, imageUrl, textPost);
                                }
                            } else {
                                Log.d("Firebase", "No entry exists for date " + currentDate + ". Creating a new entry.");
                                DiaryPreviewItem entry = new DiaryPreviewItem(MainActivity.username, currentDate, trackName, imageUrl, textPost);
                                currEntryId = userDiaryReference.push().getKey();
                                userDiaryReference.child(currEntryId).setValue(entry);
                                Toast.makeText(SingleEntryActivity.this, "Entry created successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void updateExistingEntry(String entryKey, String newTrack, String newImageUrl, String newTextPost) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("trackName", newTrack);
        updateFields.put("coverURL", newImageUrl);
        updateFields.put("postText", newTextPost);

        userDiaryReference.child(entryKey).updateChildren(updateFields)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SingleEntryActivity.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SingleEntryActivity.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onClickUpdateEntry(View view) {
        onCreateDialog().show();
    }

    public Dialog onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);
        builder.setView(view);

        trackEditText = view.findViewById(R.id.trackNameEditText);
        artistEditText = view.findViewById(R.id.artistEditText);
//        EditText albumEditText = view.findViewById(R.id.albumEditText);
        EditText textPostEditText = view.findViewById(R.id.textPostEditText);

        Button searchMusicButton = view.findViewById(R.id.searchMusicButton);
        searchMusicButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchMusicActivity.class);
            activityResultLauncher.launch(intent);
        });

        builder.setPositiveButton("Update Entry", (dialog, id) -> {
            String newTrack = trackEditText.getText().toString().trim();
            String newArtist = artistEditText.getText().toString().trim();
//                String newAlbum = albumEditText.getText().toString().trim();
            String newTextPost = textPostEditText.getText().toString().trim();

            if (!newTrack.isEmpty() && !newArtist.isEmpty()) {
                updateEntryData(newTrack, newArtist, newTextPost);
            } else {
                Toast.makeText(SingleEntryActivity.this, "PLease enter track name and artist name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

//        private void playMusic() {
//        if (trackURI != null) {
//            mSpotifyAppRemote.getPlayerApi().play(trackURI);
//            Log.d("PlayMusic", "Music is playing");
//        } else {
//            Log.d("PlayMusic", "Failed to play music: trackId is null");
//        }
//        isPlaying = true;
//
//    }
//
//    private void stopMusic() {
//        mSpotifyAppRemote.getPlayerApi().pause();
//        isPlaying = false;
//        Log.d("PlayMusic", "Music playback paused");
//    }
}
