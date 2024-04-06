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
    private DatabaseReference diaryReference;
    private FloatingActionButton addEntryButton;
    private List<DiaryPreviewItem> diaryEntries;
    private ProgressBar progressBar;
    public static String sharedDiaryReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book);

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        addEntryButton = findViewById(R.id.addEntryButton);
        Toolbar diaryBookToolbar = findViewById(R.id.diaryBookToolbar);
        diaryEntries = new ArrayList<>();
        progressBar = findViewById(R.id.diaryBookProgressBar);

        setSupportActionBar(diaryBookToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedDiaryReference = getIntent().getStringExtra("sharedDiaryReference"); // redefine every time this activity is created
        if (sharedDiaryReference != null) {
            diaryReference = MainActivity.mDatabase.child(sharedDiaryReference);
            setToolbarTitle("Shared Diary");
        } else {
            diaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
            setToolbarTitle("Your Diary");
        }

        populateDiaryEntries();
    }

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void onClickToEntry(View view) {
        Intent intent = new Intent(this, SingleEntryActivity.class);
        if (sharedDiaryReference != null) {
            intent.putExtra("sharedDiaryReference", sharedDiaryReference);
        }
        startActivity(intent);
    }

    private Boolean currentEntryCreated(List<DiaryPreviewItem> diaryEntries) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        String currentDateString = dateTimeFormatter.format(currentDate);

        for (DiaryPreviewItem entry : diaryEntries) {
            if (entry.getDate() != null && entry.getDate().equals(currentDateString)) {
                return true;
            }
        }

        return false;
    }

    private void populateDiaryEntries() {
        diaryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diaryEntries.clear();
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
