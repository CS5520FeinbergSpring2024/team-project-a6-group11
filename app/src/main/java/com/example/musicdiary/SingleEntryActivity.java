package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingleEntryActivity extends AppCompatActivity {
    OkHttpClient client;
    String searchBaseURL = "https://api.spotify.com/v1/search";
    String sampleURL = "https://api.spotify.com/v1/search?q=First%2520Love%2520artist%3AHikaru%2520Utada&type=track&market=US";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);

        client = new OkHttpClient();
        Request request = new Request.Builder().url(sampleURL).addHeader("Authorization", "Bearer " + "accessToken").build();
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
                            JSONObject trackObject = new JSONObject(jsonData);
                            JSONArray itemsArray = trackObject.getJSONArray("items");
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    public void onClickUpdateEntry(View view) {
            onCreateDialog().show();
    }

    private Dialog onCreateDialog(){
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


                if (!newTrack.isEmpty() && !newArtist.isEmpty() && !newTextPost.isEmpty()) {
                    //TODO:update single_entry UI
                } else {
                    Toast.makeText(SingleEntryActivity.this, "Information missed", Toast.LENGTH_SHORT).show();
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
