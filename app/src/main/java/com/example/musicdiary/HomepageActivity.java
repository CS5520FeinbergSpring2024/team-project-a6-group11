package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        Intent intent = new Intent(this, DiaryBookActivity.class);
        intent.putExtra("messagesReference", "recv_diary_entries");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}