package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Toolbar toolbar = findViewById(R.id.messagesToolbar);
        toolbar.setTitle("Received Messages");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("recv_diary_entries");
        diaryEntries = new ArrayList<>();
        progressBar = findViewById(R.id.messagesProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.messagesRecyclerView);
        textView = findViewById(R.id.messagesTextView);

        populateDiaryEntries();
    }

    private void populateDiaryEntries() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.INVISIBLE);

                if (!snapshot.exists()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "You have not received any messages!", Toast.LENGTH_SHORT);
                    toast.show();

                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    DiaryPreviewItem diaryPreviewItem = child.getValue(DiaryPreviewItem.class);
                    diaryEntries.add(diaryPreviewItem);
                }

                if (diaryEntries.size() == 0) {
                    textView.setVisibility(View.VISIBLE);

                    Toast toast = Toast.makeText(getApplicationContext(), "You have not received any messages!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                runOnUiThread(() -> {
                    DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(diaryEntries);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(diaryBookAdapter);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}