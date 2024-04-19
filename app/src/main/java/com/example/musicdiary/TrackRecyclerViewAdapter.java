package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicdiary.databinding.FragmentTrackBinding;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TrackItem}.
 */
public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {
    private final List<TrackItem> trackItems;
    private int lastPlayingTrackPosition = -1;
    private final OnAddButtonPressedListener onAddButtonPressedListener;

    public TrackRecyclerViewAdapter(List<TrackItem> trackItems, OnAddButtonPressedListener onAddButtonPressedListener) {
        this.trackItems = trackItems;
        this.onAddButtonPressedListener = onAddButtonPressedListener;
        MediaPlayerClient.mediaPlayer.setOnCompletionListener(mp -> {
            if (lastPlayingTrackPosition != -1) {
                notifyItemChanged(lastPlayingTrackPosition);
                lastPlayingTrackPosition = -1;
            }
        });
    }

    public interface OnAddButtonPressedListener {
        void onAddButtonPressed(TrackItem trackItem);
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

        Target target = new Target() {
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
        };

        holder.playButton.setTag(target); // to ensure the image is loaded properly by creating a strong target reference

        if (trackIconURL != null) {
            Picasso.get().load(trackIconURL).into(target);
        }

        int currentPosition = holder.getAdapterPosition();

        if (currentPosition == lastPlayingTrackPosition) {
            holder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }

        holder.playButton.setOnClickListener(view -> {
            if (trackItems.get(position).trackPreviewURL.equals("null")) {
                Toast.makeText(view.getContext(), "Preview unavailable for this track.", Toast.LENGTH_SHORT).show();
            } else if (lastPlayingTrackPosition == currentPosition) {
                MediaPlayerClient.mediaPlayer.pause();
                lastPlayingTrackPosition = -1;
                holder.playButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                String previewURL = trackItems.get(position).trackPreviewURL;
                MediaPlayerClient.playTrack(previewURL, view.getContext());
                holder.playButton.setImageResource(android.R.drawable.ic_media_pause);

                if (lastPlayingTrackPosition != -1) {
                    notifyItemChanged(lastPlayingTrackPosition); // update the view for the last played track
                }

                lastPlayingTrackPosition = currentPosition;
            }
        });

        holder.addButton.setOnClickListener(view -> {
            if (onAddButtonPressedListener != null) {
                onAddButtonPressedListener.onAddButtonPressed(trackItems.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView trackName;
        public final TextView trackArtists;
        public final ImageButton playButton;
        public final ImageButton addButton;

        public ViewHolder(FragmentTrackBinding binding) {
            super(binding.getRoot());
            trackName = binding.trackName;
            trackArtists = binding.trackArtists;
            playButton = binding.playButton;
            addButton = binding.addButton;
        }
    }
}