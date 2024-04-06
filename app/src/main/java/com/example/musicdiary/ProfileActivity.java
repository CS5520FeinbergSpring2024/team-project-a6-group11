package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText(MainActivity.username);
        String userid = MainActivity.userid;
        DatabaseReference mDatabase;
        // get username from database and show
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getUserNameByUserId(userid, mDatabase, textViewUsername);
        ImageView imageViewProfilePicture = findViewById(R.id.imageViewProfilePic);
        if (MainActivity.profilePictureURL != null) {
            Picasso.get().load(MainActivity.profilePictureURL).into(imageViewProfilePicture);
        }

        // enable edit button
        Button editButton = findViewById(R.id.buttonEditProfile);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ProfileActivity.this, "to be continued", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
//                startActivity(intent);
                startEditProfileActivity();
            }
        });
    }

    private void getUserNameByUserId(String userid, DatabaseReference mDatabase, TextView textViewUsername) {
        mDatabase.child("diary_users").child(userid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Username exists in the database
                    String username = snapshot.getValue(String.class);
                    textViewUsername.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void startEditProfileActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
    }

    // Override onActivityResult() to handle the result from EditProfileActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the updated username from the data intent
            String updatedUsername = data.getStringExtra("updatedUsername");
            // Update the UI with the updated username
            TextView textViewUsername = findViewById(R.id.textViewUsername);
            textViewUsername.setText(updatedUsername);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkIfAutoTimeEnabled(this);
    }
}


