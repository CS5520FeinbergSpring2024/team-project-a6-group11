package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText editUsernameEditText = findViewById(R.id.editUsernameEditText);
        Button saveButton = findViewById(R.id.saveButton);
        String userid = MainActivity.userid;
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserName(userid, editUsernameEditText, mDatabase);
            }
        });
    }

    private void updateUserName(String userid, EditText editUsernameEditText, DatabaseReference mDatabase) {
        String newUserName = editUsernameEditText.getText().toString().trim();
        mDatabase.child("diary_users").child(userid).child("username").setValue(newUserName);
        Toast.makeText(EditProfileActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
        // Pass the updated username back to the previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedUsername", newUserName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}