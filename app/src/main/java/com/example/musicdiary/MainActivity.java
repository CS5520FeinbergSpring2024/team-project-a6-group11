package com.example.musicdiary;

// Code referenced from: https://developer.spotify.com/documentation/android/tutorials/getting-started

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    protected static final String CLIENT_ID = "4b83fe61c320426e85d8c3ceeee4773e";
    protected static final String REDIRECT_URI = "com.example.musicdiary://callback";
    public static String accessToken;
    public static String username;
    public static String userid;
    public static String profilePictureURL = null;
    private OkHttpClient client = new OkHttpClient();
    public static DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private TextView loginTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        progressBar = findViewById(R.id.mainProgressBar);
        loginTextView = findViewById(R.id.loginTextView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkIfAutoTimeEnabled(this);
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
                    handleResponse();

                    break;
                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(getApplicationContext(), "Failed to log into your Spotify account!\n" +
                            "Please try again later.", Toast.LENGTH_SHORT).show();
                    break;
                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void handleResponse() {
        if (accessToken != null) {
            progressBar.setVisibility(View.VISIBLE);
            loginTextView.setVisibility(View.VISIBLE);

            Request request = new Request.Builder().url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (response.body() != null) {
                            String responseString = response.body().string();
                            JSONObject userData = new JSONObject(responseString);
                            userid = userData.getString("id");
                            username = userData.getString("display_name");

                            try {
                                JSONArray imagesArray = userData.getJSONArray("images");
                                JSONObject imageObject = (JSONObject) imagesArray.get(imagesArray.length() - 1);
                                profilePictureURL = imageObject.getString("url");
                            } catch (JSONException jsonException) {
                                profilePictureURL = null;
                            }

                            // if not in database, store userid and username
                            storeNewUsersToDatabase(userid, username);
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> hideProgressUI());
                        runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                                .setTitle("This app is in development mode.")
                                .setMessage("To log in, you must be registered in the Developer Dashboard.\nPlease contact chin.jef@northeastern.edu to have your email whitelisted.")
                                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                                .show());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> hideProgressUI());
                    Toast.makeText(getApplicationContext(), "Failed to log into your Spotify account!\n" +
                            "Please try again later.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void hideProgressUI() {
        progressBar.setVisibility(View.GONE);
        loginTextView.setVisibility(View.GONE);
    }

    public void openLoginPage(View view) {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "user-read-email"});

        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginInBrowser(this, request);
    }

    public void startHomepageActivity() {
        Intent intent = new Intent(this, HomepageActivity.class);
        startActivity(intent);
    }

    private void storeNewUsersToDatabase(String userid, String username) {
        mDatabase.child("diary_users").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // User ID does not exist in the database
                    // Store the user ID and username in the database
                    checkIfUsernameExists(mDatabase.child("diary_users"), username, userid);
                } else {
                    // If the user exists in the database, replace username with app username
                    MainActivity.username = snapshot.child("username").getValue(String.class);
                    hideProgressUI();
                    startHomepageActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressUI();
                Toast.makeText(getApplicationContext(), "Failed to log into your Spotify account!\n" +
                        "Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUsernameExists(DatabaseReference userDatabase, String username, String userid) {

        userDatabase.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String newUsername = username;
                if (snapshot.getValue() != null) { // a user with the username exists
                    newUsername = username + userid;
                }
                MainActivity.username = newUsername;

                userDatabase.child(userid).child("username").setValue(newUsername)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // User data stored successfully
                                Toast.makeText(MainActivity.this, "User data stored successfully", Toast.LENGTH_SHORT).show();
                                startHomepageActivity();
                            } else {
                                // Error occurred while storing user data
                                Toast.makeText(MainActivity.this, "Failed to store user data", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase Error", "Failed to store user data", task.getException());
                            }
                            hideProgressUI();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressUI();
                Toast.makeText(MainActivity.this, "Failed to contact the database.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void checkIfAutoTimeEnabled(Context context) {
        if (!autoTimeEnabled(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("Automatic date and time disabled.")
                    .setMessage("Due to security concerns, this application requires automatic date and time to be enabled.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                            System.exit(0);
                        } else {
                            new AlertDialog.Builder(context)
                                    .setMessage("Could not open settings. The application will now close.")
                                    .setPositiveButton("OK", (dialog_, which2_) -> System.exit(0))
                                    .setCancelable(false)
                                    .show();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public static boolean autoTimeEnabled(Context context) {
        try {
            int setting = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME);
            return setting == 1; // If true, user has automatic date and time enabled.
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfAutoTimeEnabled(this);
    }
}