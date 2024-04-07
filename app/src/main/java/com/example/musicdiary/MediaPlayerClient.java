package com.example.musicdiary;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.IOException;

public class MediaPlayerClient {
    public static final MediaPlayer mediaPlayer = new MediaPlayer();

    public static void playTrack(String previewURL, Context context) {
        try {
            resetMediaPlayer();
            mediaPlayer.setDataSource(previewURL);
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast toast = Toast.makeText(context, "Failed to play the song preview.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void resetMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
}
