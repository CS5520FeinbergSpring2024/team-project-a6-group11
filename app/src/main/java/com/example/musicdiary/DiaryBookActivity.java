package com.example.musicdiary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private String messagesReference;
    private ValueEventListener diaryListener;

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
        messagesReference = getIntent().getStringExtra("messagesReference");
        if (sharedDiaryReference != null) {
            diaryReference = MainActivity.mDatabase.child(sharedDiaryReference);
            setToolbarTitle("Shared Diary");
        } else if (messagesReference == null) {
            diaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child("diary_entries");
            setToolbarTitle("Your Diary");
        } else {
            diaryReference = MainActivity.mDatabase.child("diary_users").child(MainActivity.userid).child(messagesReference);
            setToolbarTitle("Your Messages");
        }

        diaryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diaryEntries.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DiaryPreviewItem diaryPreviewItem = child.getValue(DiaryPreviewItem.class);
                        diaryEntries.add(diaryPreviewItem);
                    }
                }

                progressBar.setVisibility(View.INVISIBLE);
                DiaryBookAdapter diaryBookAdapter = new DiaryBookAdapter(diaryEntries);
                diaryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                diaryRecyclerView.setAdapter(diaryBookAdapter);

                updateAddEntryButton();

                // swipe to delete
                ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        DiaryPreviewItem swipedEntry = diaryEntries.get(position);
                        showDeleteConfirmationDialog(swipedEntry, position);
                    }
                };
                new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(diaryRecyclerView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        };

        diaryReference.addValueEventListener(diaryListener);
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
        intent.putExtra("newEntry", true);
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

    private void updateAddEntryButton() {
        if (currentEntryCreated(diaryEntries) || messagesReference != null) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        diaryReference.removeEventListener(diaryListener);
        sharedDiaryReference = null;
    }

    private void showDeleteConfirmationDialog(final DiaryPreviewItem entry, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Entry");
        builder.setMessage("Are you sure you want to delete this entry?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the entry from the list
                diaryEntries.remove(position);
                // Notify the adapter of the change
                diaryRecyclerView.getAdapter().notifyItemRemoved(position);
                // Delete the entry from the database
                diaryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                            DiaryPreviewItem currEntry = entrySnapshot.getValue(DiaryPreviewItem.class);
                            // As cannot get entry id from diary preview item, deleted the entry by checking if the entry matches the one you want to delete based on its attributes
                            if (currEntry != null && currEntry.getAuthor().equals(entry.getAuthor()) && currEntry.getCoverURL().equals(entry.getCoverURL())
                                    && currEntry.getDate().equals(entry.getDate()) && currEntry.getTrackName().equals(entry.getTrackName())) {
                                entrySnapshot.getRef().removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                diaryRecyclerView.getAdapter().notifyItemChanged(position);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                diaryRecyclerView.getAdapter().notifyItemChanged(position);
            }
        });
        builder.show();
    }
}
