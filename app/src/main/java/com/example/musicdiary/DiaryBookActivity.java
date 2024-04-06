package com.example.musicdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book);

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        addEntryButton = findViewById(R.id.addEntryButton);
        Toolbar diaryBookToolbar = findViewById(R.id.diaryBookToolbar);
        userDiaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
        diaryEntries = new ArrayList<>();
        progressBar = findViewById(R.id.diaryBookProgressBar);

        populateDiaryEntries();

        setSupportActionBar(diaryBookToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Your Diary");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onClickToEntry(View view) {
        Intent intent = new Intent(this, SingleEntryActivity.class);
        startActivity(intent);
    }

    private Boolean currentEntryCreated(List<DiaryPreviewItem> diaryEntries) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String currentDateString = dateTimeFormatter.format(currentDate);

        for (DiaryPreviewItem entry : diaryEntries) {
            if (entry.getDate().equals(currentDateString)) {
                return true;
            }
        }

        return false;
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
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(diaryEntries);
                    diaryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    diaryRecyclerView.setAdapter(diaryBookAdapter);

                    updateAddEntryButton();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateAddEntryButton() {
        if (currentEntryCreated(diaryEntries)) {
            addEntryButton.setVisibility(View.INVISIBLE);
        } else {
            addEntryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}
