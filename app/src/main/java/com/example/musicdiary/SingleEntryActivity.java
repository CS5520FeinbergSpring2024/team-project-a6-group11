package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private String trackURI;
    private static String currEntryId;
    private String previewURL = null;
    private LocalDate openedEntryLocalDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        albumImageView = findViewById(R.id.albumCoverButton);
        albumImageView.setOnClickListener(this::onClickAlbumCover);
        trackNameTextView = findViewById(R.id.trackTitleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        extraTextView = findViewById(R.id.postText);
        Button updateButton = findViewById(R.id.updateButton);
        Toolbar toolbar = findViewById(R.id.diaryToolbar);
        setSupportActionBar(toolbar);

        String openedEntryDate = getIntent().getStringExtra("openedEntryDate");
        String openedEntryTrackName = getIntent().getStringExtra("openedEntryTrackName");
        String openedEntryCoverURL = getIntent().getStringExtra("openedEntryCoverURL");
        String openedEntryPostText = getIntent().getStringExtra("openedEntryPostText");
        String openedPreviewURL = getIntent().getStringExtra("openedPreviewURL");

        LocalDate localDate = LocalDate.now();
        dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDate = dateTimeFormatter.format(localDate);

        if (getSupportActionBar() != null) {
            if (openedEntryDate == null) {
                getSupportActionBar().setTitle(currentDate);
                openedEntryLocalDate = localDate;
                updateButton.setVisibility(View.VISIBLE);
            } else {
                getSupportActionBar().setTitle(openedEntryDate);

                try {
                    openedEntryLocalDate = LocalDate.parse(openedEntryDate, dateTimeFormatter);
                    // only allow users to update an entry if the current date matches the entry date
                    if (localDate.equals(openedEntryLocalDate)) {
                        updateButton.setVisibility(View.VISIBLE);
                    }
                } catch (DateTimeParseException ignored) {

                }
            }
        }

        if (openedEntryTrackName != null) {
            trackNameTextView.setText(openedEntryTrackName);
        }

        if (openedEntryCoverURL != null) {
            loadAlbumImage(openedEntryCoverURL);
        }

        if (openedPreviewURL != null) {
            previewURL = openedPreviewURL;
        }

        if (openedEntryPostText != null) {
            extraTextView.setText(openedEntryPostText);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = new OkHttpClient();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 0) {
                Intent data = result.getData();
                if (data != null) {
                    String trackName = data.getStringExtra("trackName");
                    String trackArtists = data.getStringExtra("trackArtists");

                    trackEditText.setText(trackName);
                    artistEditText.setText(trackArtists);
                }
            }
        });

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
                Toast.makeText(SingleEntryActivity.this, "Failed to search for a track", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    Log.d("SingleEntry", "run on uni thread");
                    try {
                        if (response.body() != null) {
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

                                    loadAlbumImage(imageUrl);
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

                                if (firstItem.has("preview_url")) {
                                    previewURL = firstItem.getString("preview_url");
                                }

                            } else {
                                Toast.makeText(SingleEntryActivity.this, "No tracks were found", Toast.LENGTH_SHORT).show();
                            }
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
        userDiaryReference.orderByChild("date").equalTo(currentDate).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String entryKey = dataSnapshot.getKey();
                                Log.d("Firebase", "Entry with key " + entryKey + " exists. Updating entry.");
                                updateExistingEntry(entryKey, trackName, imageUrl, previewURL, textPost);
                            }
                        } else {
                            Log.d("Firebase", "No entry exists for date " + currentDate + ". Creating a new entry.");
                            DiaryPreviewItem entry = new DiaryPreviewItem(MainActivity.username, currentDate, trackName, imageUrl, previewURL, textPost);
                            currEntryId = userDiaryReference.push().getKey();
                            if (currEntryId != null) {
                                userDiaryReference.child(currEntryId).setValue(entry);
                            }
                            Toast.makeText(SingleEntryActivity.this, "Entry created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void updateExistingEntry(String entryKey, String newTrack, String newImageUrl, String newPreviewURL, String newTextPost) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("trackName", newTrack);
        updateFields.put("coverURL", newImageUrl);
        updateFields.put("previewURL", newPreviewURL);
        updateFields.put("postText", newTextPost);

        userDiaryReference.child(entryKey).updateChildren(updateFields)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SingleEntryActivity.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SingleEntryActivity.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
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

        EditText textPostEditText = view.findViewById(R.id.textPostEditText);

        Button searchMusicButton = view.findViewById(R.id.searchMusicButton);
        searchMusicButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchMusicActivity.class);
            activityResultLauncher.launch(intent);
        });

        builder.setPositiveButton("Update Entry", (dialog, id) -> {
            String newTrack = trackEditText.getText().toString().trim();
            String newArtist = artistEditText.getText().toString().trim();
            String newTextPost = textPostEditText.getText().toString().trim();

            if (!LocalDate.now().equals(openedEntryLocalDate)) { // if the date has changed while on this activity
                Toast.makeText(SingleEntryActivity.this, "You can only edit today's entry!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newTrack.isEmpty() && !newArtist.isEmpty()) {
                updateEntryData(newTrack, newArtist, newTextPost);
            } else {
                Toast.makeText(SingleEntryActivity.this, "Please enter a track name and an artist name.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void loadAlbumImage(String imageURL) {
        Picasso.get().load(imageURL).into(new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                albumImageView.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public void onClickAlbumCover(View view) {
        if (previewURL != null) {
            if (previewURL.equals("null")) { // no preview available
                Toast toast = Toast.makeText(this, "Preview unavailable for this track.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (!MediaPlayerClient.mediaPlayer.isPlaying()) {
                playTrack();
            } else {
                pauseTrack();
            }
        }
    }

    private void playTrack() {
        MediaPlayerClient.playTrack(previewURL, this);
        albumImageView.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseTrack() {
        MediaPlayerClient.resetMediaPlayer();
        albumImageView.setImageResource(android.R.drawable.ic_media_play);
    }


    @Override
    protected void onPause() {
        super.onPause();
        pauseTrack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseTrack();
    }
}
