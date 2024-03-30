package com.example.musicdiary;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class TrackItem implements Serializable {
    public final String trackName;
    public final String trackArtists;
    public final String trackPreviewURL;
    public final String trackIconURL;

    public TrackItem(String trackName, String trackArtists, String trackPreviewURL, String trackIconURL) {
        this.trackName = trackName;
        this.trackArtists = trackArtists;
        this.trackPreviewURL = trackPreviewURL;
        this.trackIconURL = trackIconURL;
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackItem{" +
                "trackName='" + trackName + '\'' +
                ", trackArtists='" + trackArtists + '\'' +
                ", trackPreviewURL='" + trackPreviewURL + '\'' +
                ", trackIconURL='" + trackIconURL + '\'' +
                '}';
    }
}
