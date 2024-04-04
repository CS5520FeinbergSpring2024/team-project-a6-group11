package com.example.musicdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiaryBookActivity extends AppCompatActivity {
    private RecyclerView diaryRecyclerView;
    private DatabaseReference userDiaryReference;
    private FloatingActionButton addEntryButton;
    private List<DiaryPreviewItem> diaryEntries;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book);

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        addEntryButton = findViewById(R.id.addEntryButton);
        userDiaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
        diaryEntries = new ArrayList<>();

        populateDiaryEntries();
    }

    public void onClickToEntry(View view) {
        Intent intent = new Intent(this, SingleEntryActivity.class);
        startActivity(intent);
    }

    private Boolean currentEntryCreated(List<DiaryPreviewItem> diaryEntries) {
        if (diaryEntries.isEmpty()) {
            return false;
        }

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String currentDateString = dateTimeFormatter.format(currentDate);
        String lastEntryDate = diaryEntries.get(diaryEntries.size() - 1).getDate();

        return lastEntryDate.equals(currentDateString);
    }

    private void populateDiaryEntries() {
        userDiaryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DiaryPreviewItem diaryPreviewItem = child.getValue(DiaryPreviewItem.class);
                        diaryEntries.add(diaryPreviewItem);
                    }

                    runOnUiThread(() -> {
                        if (currentEntryCreated(diaryEntries)) {
                            addEntryButton.setVisibility(View.INVISIBLE);
                        }

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

    @Override
    protected void onResume() {
        super.onResume();
        if (currentEntryCreated(diaryEntries)) {
            addEntryButton.setVisibility(View.INVISIBLE);
        } else {
            addEntryButton.setVisibility(View.VISIBLE);
        }
    }
}
