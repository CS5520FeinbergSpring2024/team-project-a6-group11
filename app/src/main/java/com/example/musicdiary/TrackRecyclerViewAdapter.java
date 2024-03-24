package com.example.musicdiary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicdiary.databinding.FragmentTrackBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TrackItem}.
 */
public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {

    private final List<TrackItem> trackItems;

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
    }

    @Override
    public int getItemCount() {
        return trackItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView trackName;
        public final TextView trackArtists;

        public ViewHolder(FragmentTrackBinding binding) {
            super(binding.getRoot());
            trackName = binding.trackName;
            trackArtists = binding.trackArtists;
        }
    }
}