package com.example.musicdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiaryBookActivity extends AppCompatActivity {
    private RecyclerView diaryRecyclerView;
    private DatabaseReference userDiaryReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book);

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        userDiaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");

        populateDiaryEntries();
    }

    public void onClickToEntry(View view) {
        Intent intent = new Intent(this, SingleEntryActivity.class);
        startActivity(intent);
    }

    private void populateDiaryEntries() {
        userDiaryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<DiaryPreviewItem> diaryEntries = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DiaryPreviewItem diaryPreviewItem = child.getValue(DiaryPreviewItem.class);
                        diaryEntries.add(diaryPreviewItem);
                    }

                    runOnUiThread(() -> {
                        DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(diaryEntries);
                        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        diaryRecyclerView.setAdapter(diaryBookAdapter);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
