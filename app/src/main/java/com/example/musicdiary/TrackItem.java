package com.example.musicdiary;

import androidx.annotation.NonNull;

public class TrackItem {
    public final String trackName;
    public final String trackArtists;

    public TrackItem(String trackName, String trackArtists) {
        this.trackName = trackName;
        this.trackArtists = trackArtists;
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackItem{" +
                "trackName='" + trackName + '\'' +
                ", trackArtists='" + trackArtists + '\'' +
                '}';
    }
}
