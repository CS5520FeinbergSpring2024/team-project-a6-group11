package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        RecyclerView publicEntriesRecyclerView = findViewById(R.id.publicEntriesRecyclerView);

        List<DiaryPreviewItem> testData = new ArrayList<>();
        testData.add(new DiaryPreviewItem("1/1/01", "Track 1"));
        testData.add(new DiaryPreviewItem("1/2/01", "Track 2"));
        testData.add(new DiaryPreviewItem("1/3/01", "Track 3"));
        testData.add(new DiaryPreviewItem("1/4/01", "Track 4"));
        DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(testData);

        publicEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        publicEntriesRecyclerView.setAdapter(diaryBookAdapter);
    }

    public void startProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onClickToDiaryBook(View view) {
        Intent intent = new Intent(this, DiaryBookActivity.class);
        startActivity(intent);
    }
}