package com.example.musicdiary;

import androidx.annotation.NonNull;

public class TrackItem {
    public final String trackName;
    public final String trackArtists;
    public final String trackPreviewURL;

    public TrackItem(String trackName, String trackArtists, String trackPreviewURL) {
        this.trackName = trackName;
        this.trackArtists = trackArtists;
        this.trackPreviewURL = trackPreviewURL;
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackItem{" +
                "trackName='" + trackName + '\'' +
                ", trackArtists='" + trackArtists + '\'' +
                ", trackPreviewURL='" + trackPreviewURL + '\'' +
                '}';
    }
}
