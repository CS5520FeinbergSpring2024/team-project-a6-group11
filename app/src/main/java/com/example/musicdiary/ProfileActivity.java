package com.example.musicdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText(MainActivity.username);

        ImageView imageViewProfilePicture = findViewById(R.id.imageViewProfilePic);
        if (MainActivity.profilePictureURL != null) {
            Picasso.get().load(MainActivity.profilePictureURL).into(imageViewProfilePicture);
        }
    }
}