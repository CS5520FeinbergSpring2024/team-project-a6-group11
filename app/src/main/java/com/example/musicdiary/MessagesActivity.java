package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private List<DiaryPreviewItem> diaryEntries;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Toolbar toolbar = findViewById(R.id.receivedMessagesToolbar);
        toolbar.setTitle("Received Messages");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("recv_diary_entries");
        diaryEntries = new ArrayList<>();
        progressBar = findViewById(R.id.receivedMessagesProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.receivedMessagesRecyclerView);

        populateDiaryEntries();
    }

    private void populateDiaryEntries() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "You have not received any messages!", Toast.LENGTH_SHORT);
                    toast.show();

                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    DiaryPreviewItem diaryPreviewItem = child.getValue(DiaryPreviewItem.class);
                    diaryEntries.add(diaryPreviewItem);
                }

                if (diaryEntries.size() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "You have not received any messages!", Toast.LENGTH_SHORT);
                    toast.show();

                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                runOnUiThread(() -> {
                    DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(diaryEntries);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(diaryBookAdapter);

                    progressBar.setVisibility(View.INVISIBLE);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}