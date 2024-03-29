package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicdiary.databinding.FragmentTrackBinding;

import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TrackItem}.
 */
public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {
    private final List<TrackItem> trackItems;
    private static final MediaPlayer mediaPlayer = new MediaPlayer();

    public TrackRecyclerViewAdapter(List<TrackItem> trackItems) {
        this.trackItems = trackItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentTrackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.trackName.setText(trackItems.get(position).trackName);
        holder.trackArtists.setText(trackItems.get(position).trackArtists);
        holder.playButton.setOnClickListener(view -> AsyncTask.execute(() -> {
            try {
                resetMediaPlayer();
                mediaPlayer.setDataSource(trackItems.get(position).trackPreviewURL);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException ioException) {
                Toast toast = Toast.makeText(view.getContext(), "Failed to play the song preview!", Toast.LENGTH_SHORT);
                toast.show();
            }
        })
        );
    }

    @Override
    public int getItemCount() {
        return trackItems.size();
    }

    private void resetMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView trackName;
        public final TextView trackArtists;
        public final ImageButton playButton;

        public ViewHolder(FragmentTrackBinding binding) {
            super(binding.getRoot());
            trackName = binding.trackName;
            trackArtists = binding.trackArtists;
            playButton = binding.playButton;
        }
    }
}