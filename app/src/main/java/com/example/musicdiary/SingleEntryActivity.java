package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingleEntryActivity extends AppCompatActivity {
    OkHttpClient client;
    private String baseSearchURL = "https://api.spotify.com/v1/search";
    ImageButton albumImageView;
    TextView trackNameTextView;
    TextView artistTextView;
    TextView extraTextView;
    private static final String ACCESS_TOKEN = MainActivity.accessToken;

//    String sampleURL = "https://api.spotify.com/v1/search?q=First%2520Love%2520artist%253AHikaru%2520Utada&type=track&market=US&limit=1";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        albumImageView = findViewById(R.id.albumCoverButton);
        trackNameTextView = findViewById(R.id.trackTitleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        extraTextView = findViewById(R.id.postText);

        client = new OkHttpClient();

    }

    public void updateEntryData(String newTrack, String newArtist, String newTextPost) {
        String apiURL = null;
        try {
            String trackEncoded = URLEncoder.encode(newTrack, "UTF-8");
            String artistEncoded = URLEncoder.encode(newArtist, "UTF-8");
            String query = "track:" + trackEncoded + " artist:" + artistEncoded;
            apiURL = baseSearchURL + "?q=" + query + "&type=track&market=US&limit=1";
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
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SingleEntry","run on uni thread");
                        try {
                            String jsonData = response.body().string();
                            JSONObject resposneObject = new JSONObject(jsonData);
                            Log.d("SingleEntry", "JSON Data: " + jsonData);
                            JSONObject tracksObject = resposneObject.getJSONObject("tracks");
                            JSONArray itemsArray = tracksObject.getJSONArray("items");


                            if (itemsArray.length() > 0) {
                                JSONObject firstItem = itemsArray.getJSONObject(0);
                                String trackName = firstItem.getString("name");
                                Log.d("SingleEntry", "Track Name: " + trackName);
                                trackNameTextView.setText(trackName);

                                JSONObject album = firstItem.getJSONObject("album");
                                JSONArray imagesArray = album.getJSONArray("images");
                                if (imagesArray.length() > 0) {
                                    JSONObject firstImage = imagesArray.getJSONObject(0);
                                    String imageUrl = firstImage.getString("url");
                                    Log.d("SingleEntry", "Image URL: " + imageUrl);
                                    Picasso.get().load(imageUrl).into(albumImageView);
                                }

                                JSONArray artistsArray = firstItem.getJSONArray("artists");
//
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
                            } else {
                                Toast.makeText(SingleEntryActivity.this, "No tracks found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                        extraTextView.setText(newTextPost);
                    }
                });
            }
        });

    }


    public void onClickUpdateEntry(View view) {
            onCreateDialog().show();
    }

    public Dialog onCreateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);
        builder.setView(view);

        EditText trackEditText = view.findViewById(R.id.trackNameEditText);
        EditText artistEditText = view.findViewById(R.id.artistEditText);
//        EditText albumEditText = view.findViewById(R.id.albumEditText);
        EditText textPostEditText = view.findViewById(R.id.textPostEditText);

        builder.setPositiveButton("Update Entry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String newTrack = trackEditText.getText().toString().trim();
                String newArtist = artistEditText.getText().toString().trim();
//                String newAlbum = albumEditText.getText().toString().trim();
                String newTextPost = textPostEditText.getText().toString().trim();


                if (!newTrack.isEmpty() && !newArtist.isEmpty()) {
                    updateEntryData(newTrack, newArtist, newTextPost);
                } else {
                    Toast.makeText(SingleEntryActivity.this, "PLease enter track name and artist name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();

    }


}
