package com.example.musicdiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A fragment representing a list of tracks.
 */
public class TracklistFragment extends Fragment implements TrackRecyclerViewAdapter.OnAddButtonPressedListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TracklistFragment() {
    }

    public static TracklistFragment newInstance(ArrayList<TrackItem> tracklist) {
        TracklistFragment fragment = new TracklistFragment();
        Bundle args = new Bundle();
        args.putSerializable("tracklist", tracklist);
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<TrackItem> getTracklist() {
        if (getArguments() != null) {
            return (ArrayList<TrackItem>) getArguments().getSerializable("tracklist");
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new TrackRecyclerViewAdapter(getTracklist(), this));
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        TrackRecyclerViewAdapter.mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        TrackRecyclerViewAdapter.mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TrackRecyclerViewAdapter.mediaPlayer.stop();
    }

    @Override
    public void onAddButtonPressed(TrackItem trackItem) {
        Intent intent = new Intent();
        intent.putExtra("trackName", trackItem.getTrackName());
        intent.putExtra("trackArtists", trackItem.getTrackArtists());
        intent.putExtra("previewURL", trackItem.getTrackPreviewURL());
        requireActivity().setResult(0, intent);
        requireActivity().finish();
    }
}