package com.example.musicdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class DiaryBookActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book);
    }

    public void onClickToEntry(View view) {
        Intent intent = new Intent(this, SingleEntryActivity.class);
        startActivity(intent);
    }




}
