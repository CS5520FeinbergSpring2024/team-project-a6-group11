package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class PublicDiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_diary);

        RecyclerView publicEntriesRecyclerView = findViewById(R.id.publicEntriesRecyclerView);

        List<DiaryPreviewItem> testData = new ArrayList<>();
        testData.add(new DiaryPreviewItem("test author 1", "1/1/01", "Track 1"));
        testData.add(new DiaryPreviewItem("test author 2", "1/2/01", "Track 2"));
        testData.add(new DiaryPreviewItem("test author 3", "1/3/01", "Track 3"));
        testData.add(new DiaryPreviewItem("test author 4", "1/4/01", "Track 4"));
        DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(testData);

        publicEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        publicEntriesRecyclerView.setAdapter(diaryBookAdapter);
    }
}