package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class HomepageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }

    public void startProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onClickToDiaryBook(View view) {
        Intent intent = new Intent(this, DiaryBookActivity.class);
        startActivity(intent);
    }

    public void startSharedDiaryActivity(View view) {
        Intent intent = new Intent(this, DiaryBookActivity.class);
        intent.putExtra("sharedDiaryReference", "shared_diary_entries");
        startActivity(intent);
    }

    public void startMessagesActivity(View view) {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Spotify Redirect")
                .setMessage("You will be redirected to the Spotify website to log out. To log back in to Music Diary, please return to the app.")
                .setPositiveButton("OK", (dialog, which) -> {
                    String logoutURL = "https://open.spotify.com/logout";

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(logoutURL));
                        startActivity(intent);
                        System.exit(0);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Could not find an app to open the Spotify website.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}