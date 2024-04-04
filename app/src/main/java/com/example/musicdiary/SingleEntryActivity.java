package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
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

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    private static String currEntryId;
    private String previewURL = null;
    public static final MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        albumImageView = findViewById(R.id.albumCoverButton);
        albumImageView.setOnClickListener(this::onClickAlbumCover);
        trackNameTextView = findViewById(R.id.trackTitleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        extraTextView = findViewById(R.id.postText);
        Toolbar toolbar = findViewById(R.id.diaryToolbar);
        setSupportActionBar(toolbar);

        String openedEntryDate = getIntent().getStringExtra("openedEntryDate");
        String openedEntryTrackName = getIntent().getStringExtra("openedEntryTrackName");
        String openedEntryCoverURL = getIntent().getStringExtra("openedEntryCoverURL");
        String openedPreviewURL = getIntent().getStringExtra("openedPreviewURL");

        LocalDate localDate = LocalDate.now();
        dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy ☀");
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

        if (openedPreviewURL != null) {
            previewURL = openedPreviewURL;
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
                    try {
                        String jsonData = response.body().string();
                        JSONObject resposneObject = new JSONObject(jsonData);
                        Log.d("SingleEntry", "JSON Data: " + jsonData);
                        JSONObject tracksObject = resposneObject.getJSONObject("tracks");
                        JSONArray itemsArray = tracksObject.getJSONArray("items");

                        if (itemsArray.length() == 0) {
                            Toast toast = Toast.makeText(SingleEntryActivity.this, "No tracks were found", Toast.LENGTH_SHORT);
                            toast.show();

                            extraTextView.setText(newTextPost);

                            return;
                        }

                        JSONObject firstItem = itemsArray.getJSONObject(0);
                        String trackName = firstItem.getString("name");
                        trackNameTextView.setText(trackName);
                        JSONObject album = firstItem.getJSONObject("album");
                        JSONArray imagesArray = album.getJSONArray("images");

                        String imageUrl = "";
                        if (imagesArray.length() > 0) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            imageUrl = firstImage.getString("url");
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
                            }
                            String allArtists = concatenatedArtists.toString();
                            artistTextView.setText(allArtists);
                        }

                        String previewURL = null;
                        if (firstItem.has("preview_url")) {
                            previewURL = firstItem.getString("preview_url");
                        }

                        DiaryPreviewItem entry = new DiaryPreviewItem(MainActivity.username, currentDate, trackName, imageUrl, previewURL);
                        if (currEntryId == null) {
                            currEntryId = userDiaryReference.push().getKey();
                        }
                        userDiaryReference.child(currEntryId).setValue(entry);
                    } catch (JSONException | IOException exception) {
                        runOnUiThread(() -> {
                            Toast toast = Toast.makeText(getApplicationContext(), "Failed to update the playlist information!", Toast.LENGTH_SHORT);
                            toast.show();
                        });

                        return;
                    }
                    extraTextView.setText(newTextPost);
                });
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
            String newTextPost = textPostEditText.getText().toString().trim();

            if (!newTrack.isEmpty() && !newArtist.isEmpty()) {
                updateEntryData(newTrack, newArtist, newTextPost);
            } else {
                Toast.makeText(SingleEntryActivity.this, "Please enter track name and artist name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void onClickAlbumCover(View view) {
        System.out.println(previewURL);
        if (previewURL == null) {
            return;
        }

        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(previewURL);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ioException) {
            Toast toast = Toast.makeText(this, "Failed to play the preview of the song!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
