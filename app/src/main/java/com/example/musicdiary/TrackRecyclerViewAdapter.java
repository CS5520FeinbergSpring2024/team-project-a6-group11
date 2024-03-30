package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicdiary.databinding.FragmentTrackBinding;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TrackItem}.
 */
public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {
    private final List<TrackItem> trackItems;
    public static final MediaPlayer mediaPlayer = new MediaPlayer();
    private int lastPlayingTrackPosition = -1;

    public TrackRecyclerViewAdapter(List<TrackItem> trackItems) {
        this.trackItems = trackItems;
        mediaPlayer.setOnCompletionListener(mp -> {
            if (lastPlayingTrackPosition != -1) {
                notifyItemChanged(lastPlayingTrackPosition);
                lastPlayingTrackPosition = -1;
            }
        });
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

        Context context = holder.itemView.getContext();

        String trackIconURL = trackItems.get(position).trackIconURL;

        if (trackIconURL != null) {
            Picasso.get().load(trackIconURL).into(new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.playButton.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }

        int currentPosition = holder.getAdapterPosition();

        if (currentPosition == lastPlayingTrackPosition) {
            holder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }

        holder.playButton.setOnClickListener(view -> AsyncTask.execute(() -> {
                    try {
                        if (trackItems.get(position).trackPreviewURL.equals("null")) {
                            view.post(() -> {
                                Toast toast = Toast.makeText(view.getContext(), "Preview unavailable for this track.", Toast.LENGTH_SHORT);
                                toast.show();
                            });
                        } else if (lastPlayingTrackPosition == currentPosition) {
                            mediaPlayer.pause();
                            lastPlayingTrackPosition = -1;
                        } else {
                            resetMediaPlayer();
                            mediaPlayer.setDataSource(trackItems.get(position).trackPreviewURL);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            lastPlayingTrackPosition = currentPosition;
                        }
                        view.post(this::notifyDataSetChanged);
                    } catch (IOException ioException) {
                        view.post(() -> {
                            Toast toast = Toast.makeText(view.getContext(), "Failed to play the song preview!", Toast.LENGTH_SHORT);
                            toast.show();
                        });
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