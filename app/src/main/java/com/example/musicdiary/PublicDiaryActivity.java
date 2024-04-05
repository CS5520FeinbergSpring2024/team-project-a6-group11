package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

        DatabaseReference databaseReference = MainActivity.mDatabase.child("public_diary_entries");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot entry : dataSnapshot.getChildren()) {
                        String author = entry.child("author").getValue(String.class);
                        String date = entry.child("date").getValue(String.class);
                        String trackName = entry.child("track").getValue(String.class);
                        String trackArtists = entry.child("trackArtists").getValue(String.class);
                        String coverURL = entry.child("coverURL").getValue(String.class);
                        String postText = entry.child("postText").getValue(String.class);
                        String previewURL = entry.child("previewURL").getValue(String.class);
                        String mood = entry.child("mood").getValue(String.class);

                        DiaryPreviewItem diaryPreviewItem = new DiaryPreviewItem(author, date, trackName, trackArtists, coverURL, postText, previewURL, mood);
                        testData.add(diaryPreviewItem);
                    }

                    runOnUiThread(() -> {
                        DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(testData);
                        publicEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        publicEntriesRecyclerView.setAdapter(diaryBookAdapter);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}