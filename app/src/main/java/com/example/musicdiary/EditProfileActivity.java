package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar editProfileToolbar = findViewById(R.id.editProfileToolbar);
        editProfileToolbar.setTitle("Edit Username");
        setSupportActionBar(editProfileToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        EditText editUsernameEditText = findViewById(R.id.editUsernameEditText);
        Button saveButton = findViewById(R.id.saveButton);
        String userid = MainActivity.userid;
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        saveButton.setOnClickListener(v -> updateUserName(userid, editUsernameEditText, mDatabase));
    }

    private void updateUserName(String userid, EditText editUsernameEditText, DatabaseReference mDatabase) {
        String newUserName = editUsernameEditText.getText().toString().trim();
        if (newUserName.isEmpty()) {
            Toast toast = Toast.makeText(this, "Please enter a username!", Toast.LENGTH_SHORT);
            toast.show();

            return;
        }

        // Update the "author" fields of each diary entry:
        DatabaseReference userDiaryReference =  mDatabase.child("diary_users").child(userid).child("diary_entries");

        userDiaryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey();

                    if (key != null) {
                        userDiaryReference.child(key).child("author").setValue(newUserName);
                    }
                }

                mDatabase.child("diary_users").child(userid).child("username").setValue(newUserName);
                MainActivity.username = newUserName;

                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Username updated successfully.", Toast.LENGTH_SHORT).show());
                // Pass the updated username back to the previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedUsername", newUserName);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}