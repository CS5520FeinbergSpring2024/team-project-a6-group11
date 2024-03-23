package com.example.musicdiary;

// Code referenced from: https://developer.spotify.com/documentation/android/tutorials/getting-started

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "4b83fe61c320426e85d8c3ceeee4773e";
    private static final String REDIRECT_URI = "com.example.musicdiary://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    public static String accessToken;

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
                                "Please install it in order to use this application!\n\n" +
                                "This application will now close.")
                        .setPositiveButton("Ok", (dialog, which) -> System.exit(0))
                        .show();
                return;
            }
        }

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginInBrowser(this, request);
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

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:

                    accessToken = response.getAccessToken();

                    break;
                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;
                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    public void OnClickToEntry(View view){
        Intent intent = new Intent(this, SingleEntryActivity.class);
        startActivity(intent);
    }

    public void startSearchMusicActivity(View view) {
        Intent intent = new Intent(this, SearchMusicActivity.class);
        startActivity(intent);
    }
}