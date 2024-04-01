package com.example.musicdiary;

// Code referenced from: https://developer.spotify.com/documentation/android/tutorials/getting-started

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "4b83fe61c320426e85d8c3ceeee4773e";
    private static final String REDIRECT_URI = "com.example.musicdiary://callback";
    private static final int REQUEST_CODE = 0;
    private SpotifyAppRemote mSpotifyAppRemote = null;
    public static String accessToken;
    private OkHttpClient client = new OkHttpClient();
    public static String username;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        {
            PackageManager packageManager = this.getPackageManager();
            try {
                packageManager.getPackageInfo("com.spotify.music", 0);
            } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                new AlertDialog.Builder(this)
                        .setTitle("Spotify is not installed!")
                        .setMessage("This application requires Spotify to be installed on your phone! " +
                                "Please install it in order to use this application!")
                        .setPositiveButton("Download Spotify", (dialog, which) -> AuthorizationClient.openDownloadSpotifyActivity(this))
                        .setCancelable(false)
                        .show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    accessToken = response.getAccessToken();
                    handleResponse();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to log into your Spotify account!\n" +
                            "Please try again later.", Toast.LENGTH_SHORT);
                    toast.show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void handleResponse() {
        if (accessToken != null) {
            Request request = new Request.Builder().url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (response.body() != null) {
                            String responseString = response.body().string();
                            System.out.println(responseString);
                            JSONObject userData = new JSONObject(responseString);
                            username = userData.getString("display_name");
                            startHomepageActivity();
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                                .setTitle("This app is in development mode.")
                                .setMessage("To log in, you must be registered in the Developer Dashboard.\nPlease contact chin.jef@northeastern.edu to have your email whitelisted.")
                                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                                .show());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }
            });
        }
    }

    public void openLoginPage(View view) {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    public void startHomepageActivity() {
        Intent intent = new Intent(this, HomepageActivity.class);
        startActivity(intent);
    }
}