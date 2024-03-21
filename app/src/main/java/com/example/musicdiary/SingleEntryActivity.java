package com.example.musicdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SingleEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entry);
    }

    public void onClickUpdateEntry(View view) {
            onCreateDialog().show();
    }

    private Dialog onCreateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);
        builder.setView(view);

        EditText trackEditText = view.findViewById(R.id.trackNameEditText);
        EditText artistEditText = view.findViewById(R.id.artistEditText);
        EditText albumEditText = view.findViewById(R.id.albumEditText);
        EditText textPostEditText = view.findViewById(R.id.textPostEditText);

        builder.setPositiveButton("Update Entry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String newTrack = trackEditText.getText().toString().trim();
                String newArtist = artistEditText.getText().toString().trim();
                String newAlbum = albumEditText.getText().toString().trim();
                String newTextPost = textPostEditText.getText().toString().trim();


                if (!newTrack.isEmpty() && !newArtist.isEmpty() && !newAlbum.isEmpty() && !newTextPost.isEmpty()) {
                    //TODO:update single_entry UI
                } else {
                    Toast.makeText(SingleEntryActivity.this, "Information missed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();

    }
}
