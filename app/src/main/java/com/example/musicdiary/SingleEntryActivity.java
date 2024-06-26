package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

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
    private DatabaseReference diaryReference;
    private DateTimeFormatter dateTimeFormatter;
    private String currentDate;
    private String imageUrl;
    private String trackName;
    private String textPost;
    private String trackURI;
    private String allArtists;
    private static String currEntryId;
    private String previewURL;
    private LocalDate openedEntryLocalDate;
    private CardView postTextCardView;
    private TextView moodTextView;
    private ImageView moodIcon;
    private String mood = "None";
    private LinearLayout updatingView;
    private ScrollView scrollView;
    private ImageView scrollHintView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        albumImageView = findViewById(R.id.albumCoverButton);
        albumImageView.setOnClickListener(this::onClickAlbumCover);
        trackNameTextView = findViewById(R.id.trackTitleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        extraTextView = findViewById(R.id.postText);
        postTextCardView = findViewById(R.id.postTextCardView);
        moodTextView = findViewById(R.id.moodTextView);
        moodIcon = findViewById(R.id.moodIcon);
        updatingView = findViewById(R.id.updatingView);
        Button updateButton = findViewById(R.id.updateButton);
        Toolbar toolbar = findViewById(R.id.diaryToolbar);
        setSupportActionBar(toolbar);

        TextView cannotEditTextView = findViewById(R.id.cannotEditTextView);

        String openedEntryAuthorID = getIntent().getStringExtra("openedEntryAuthorID");
        String openedEntryDate = getIntent().getStringExtra("openedEntryDate");
        trackName = getIntent().getStringExtra("openedEntryTrackName");
        allArtists = getIntent().getStringExtra("openedEntryTrackArtists");
        String openedEntryCoverURL = getIntent().getStringExtra("openedEntryCoverURL");
        textPost = getIntent().getStringExtra("openedEntryPostText");
        String openedPreviewURL = getIntent().getStringExtra("openedPreviewURL");
        mood = getIntent().getStringExtra("openedMood");
        String sharedDiaryReference = getIntent().getStringExtra("sharedDiaryReference");

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
                        if ((openedEntryAuthorID != null && openedEntryAuthorID.equals(MainActivity.userid)) || sharedDiaryReference != null) { // If not in the shared diary, user IDs must match to update
                            updateButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if ((openedEntryAuthorID != null && openedEntryAuthorID.equals(MainActivity.userid)) || sharedDiaryReference != null) {
                            cannotEditTextView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (DateTimeParseException ignored) {

                }
            }
        }

        if (trackName != null) {
            trackNameTextView.setText(trackName);
        }

        if (allArtists != null) {
            artistTextView.setText(allArtists);
        }

        if (openedEntryCoverURL != null) {
            loadAlbumImage(openedEntryCoverURL);
        } else {
            albumImageView.setVisibility(View.GONE);
        }

        if (openedPreviewURL != null) {
            previewURL = openedPreviewURL;
        }

        if (textPost != null && !textPost.isEmpty()) {
            extraTextView.setText(textPost);
        } else {
            postTextCardView.setVisibility(View.GONE);
        }

        if (mood != null) {
            updateMood(mood);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = new OkHttpClient();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 0) {
                Intent data = result.getData();
                if (data != null) {
                    String trackName = data.getStringExtra("trackName");
                    String trackArtists = data.getStringExtra("trackArtists");

                    if (trackEditText == null || artistEditText == null) {
                        onCreateDialog("Edit Entry", "Update Entry").show(); // reopen the dialog if for some reason it is dismissed
                    }

                    trackEditText.setText(trackName);
                    artistEditText.setText(trackArtists);
                }
            }
        });

        if (sharedDiaryReference != null) {
            diaryReference = MainActivity.mDatabase.child(sharedDiaryReference);
        } else {
            diaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
        }

        boolean isNewEntry = getIntent().getBooleanExtra("newEntry", false);
        if (isNewEntry) {
            onCreateDialog("New Entry", "Add Entry").show();
        }

        MediaPlayerClient.mediaPlayer.setOnCompletionListener(mp -> pauseTrack());

        scrollView = findViewById(R.id.scrollView);
        scrollHintView = findViewById(R.id.scrollHintView);

        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 0) { // the user has scrolled
                scrollHintView.animate().alpha(0.0f).setDuration(200).start(); // hide the scroll hint
            }
        });

        toggleScrollHint();
    }

    private void toggleScrollHint() {
        scrollView.post(() -> {
            int totalHeight = scrollView.getChildAt(0).getHeight();
            int visibleHeight = scrollView.getHeight();
            int scrollPosition = scrollView.getScrollY();
            boolean scrollable = (totalHeight - visibleHeight) > 0;
            boolean isAtBottom = scrollPosition == (totalHeight - visibleHeight);

            scrollHintView.setVisibility((scrollable && !isAtBottom) ? View.VISIBLE : View.INVISIBLE);
        });
    }

    private void updateMoodText(String mood) {
        if (!mood.equals("None")) {
            moodTextView.setText(mood);
        } else {
            moodTextView.setText("");
        }
    }

    private void updateMood(String mood) {
        updateMoodText(mood);
        switch (mood) {
            case "I'm feeling good":
                moodIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feeling_good, null));
                moodIcon.setVisibility(View.VISIBLE);
                break;
            case "I'm feeling neutral":
                moodIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feeling_neutral, null));
                moodIcon.setVisibility(View.VISIBLE);
                break;
            case "I'm feeling bad":
                moodIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feeling_bad, null));
                moodIcon.setVisibility(View.VISIBLE);
                break;
            default:
                moodIcon.setVisibility(View.GONE);
                moodIcon.setImageDrawable(null);
        }
    }

    public void updateEntryData(String newTrack, String newArtist, String newTextPost, String newMood) {
        // to prevent long queries from returning status code 400:
        if (newTrack.length() > 100) {
            newTrack = newTrack.substring(0, 100);
        }
        if (newArtist.length() > 100) {
            newArtist = newArtist.substring(0, 100);
        }

        String apiURL = "https://api.spotify.com/v1/search";
        try {
            String trackEncoded = URLEncoder.encode(newTrack, "UTF-8");
            String artistEncoded = URLEncoder.encode(newArtist, "UTF-8");
            String query = "track:" + trackEncoded + " artist:" + artistEncoded;

            apiURL += "?q=" + query + "&type=track&market=US&limit=1";
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "The given information could not be read.", Toast.LENGTH_SHORT).show();
            return;
        }

        updatingView.setVisibility(View.VISIBLE);

        Request request = new Request.Builder().url(apiURL).addHeader("Authorization", "Bearer " + ACCESS_TOKEN).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    updatingView.setVisibility(View.GONE);
                    Toast.makeText(SingleEntryActivity.this, "Could not search for the given track.", Toast.LENGTH_SHORT).show();
                });
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
                                    albumImageView.setVisibility(View.VISIBLE);
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
                                    allArtists = concatenatedArtists.toString();
                                    artistTextView.setText(allArtists);
                                }

                                if (firstItem.has("preview_url")) {
                                    previewURL = firstItem.getString("preview_url");
                                }

                                textPost = newTextPost;
                                if (!textPost.isEmpty()) {
                                    extraTextView.setText(textPost);
                                    postTextCardView.setVisibility(View.VISIBLE);
                                } else {
                                    postTextCardView.setVisibility(View.GONE);
                                }

                                mood = newMood;
                                updateMood(mood);

                                // Replace extras with updated data
                                getIntent().putExtra("openedEntryTrackName", trackName);
                                getIntent().putExtra("openedEntryCoverURL", imageUrl);
                                getIntent().putExtra("openedEntryPostText", textPost);
                                getIntent().putExtra("openedPreviewURL", previewURL);
                                getIntent().putExtra("openedMood", mood);

                                toggleScrollHint();

                                checkEntryExists(currentDate); // in the database
                            } else {
                                updatingView.setVisibility(View.GONE);
                                Toast.makeText(SingleEntryActivity.this, "Could not find the given track.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException | IOException exception) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "An error occurred while updating.", Toast.LENGTH_SHORT).show();
                            updatingView.setVisibility(View.GONE);
                        });
                    }
                });
            }
        });
    }

    private void checkEntryExists(String currentDate) {
        diaryReference.orderByChild("date").equalTo(currentDate).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                        updatingView.setVisibility(View.GONE);
                    } else {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String entryKey = dataSnapshot.getKey();
                                Log.d("Firebase", "Entry with key " + entryKey + " exists. Updating entry.");
                                updateExistingEntry(entryKey, trackName, allArtists, imageUrl, textPost, previewURL, mood);
                            }
                        } else {
                            Log.d("Firebase", "No entry exists for date " + currentDate + ". Creating a new entry.");
                            DiaryPreviewItem entry = new DiaryPreviewItem(MainActivity.userid, MainActivity.username, currentDate, trackName, allArtists, imageUrl, textPost, previewURL, mood);
                            currEntryId = diaryReference.push().getKey();
                            if (currEntryId != null) {
                                diaryReference.child(currEntryId).setValue(entry);
                            }
                            Toast.makeText(SingleEntryActivity.this, "Entry created successfully", Toast.LENGTH_SHORT).show();
                            updatingView.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    private void updateExistingEntry(String entryKey, String newTrack, String newArtists, String newImageUrl, String newTextPost, String newPreviewURL, String newMood) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("authorID", MainActivity.userid);
        updateFields.put("author", MainActivity.username);
        updateFields.put("trackName", newTrack);
        updateFields.put("trackArtists", newArtists);
        updateFields.put("coverURL", newImageUrl);
        updateFields.put("postText", newTextPost);
        updateFields.put("previewURL", newPreviewURL);
        updateFields.put("mood", newMood);

        diaryReference.child(entryKey).updateChildren(updateFields)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SingleEntryActivity.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SingleEntryActivity.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                    }
                    updatingView.setVisibility(View.GONE);
                });
    }

    public void onClickUpdateEntry(View view) {
        onCreateDialog("Edit Entry", "Update Entry").show();
    }

    public Dialog onCreateDialog(String title, String positiveButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);
        builder.setView(view);

        trackEditText = view.findViewById(R.id.trackNameEditText);
        artistEditText = view.findViewById(R.id.artistEditText);
        EditText textPostEditText = view.findViewById(R.id.textPostEditText);
        Spinner feelingSpinner = view.findViewById(R.id.feelingSpinner);

        trackEditText.setText(trackName);
        artistEditText.setText(allArtists);
        textPostEditText.setText(textPost);

        String[] spinnerItems = getResources().getStringArray(R.array.spinner_items);
        for (int i = 0; i < spinnerItems.length; i++) {
            if (spinnerItems[i].equals(mood)) {
                feelingSpinner.setSelection(i);
                break;
            }
        }

        Button searchMusicButton = view.findViewById(R.id.searchMusicButton);
        searchMusicButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchMusicActivity.class);
            activityResultLauncher.launch(intent);
        });

        builder.setPositiveButton(positiveButtonText, (dialog, id) -> {
            String newTrack = trackEditText.getText().toString().trim();
            String newArtist = artistEditText.getText().toString().trim();
            String newTextPost = textPostEditText.getText().toString().trim();
            String newMood = feelingSpinner.getSelectedItem().toString();

            if (!LocalDate.now().equals(openedEntryLocalDate)) { // if the date has changed while on this activity
                Toast.makeText(SingleEntryActivity.this, "You can only edit today's entry!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newTrack.isEmpty() && !newArtist.isEmpty()) {
                updateEntryData(newTrack, newArtist, newTextPost, newMood);
            } else {
                Toast.makeText(SingleEntryActivity.this, "Please enter a track name and an artist name.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void loadAlbumImage(String imageURL) {
        Target target = new Target() {
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
        };

        albumImageView.setTag(target);

        Picasso.get().load(imageURL).into(target);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // the back button
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTrack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlayerClient.mediaPlayer.setOnCompletionListener(mp -> pauseTrack());
        MainActivity.checkIfAutoTimeEnabled(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseTrack();
        MediaPlayerClient.mediaPlayer.setOnCompletionListener(null);
        albumImageView.setOnClickListener(null);
        scrollView.setOnScrollChangeListener(null);
    }
}
