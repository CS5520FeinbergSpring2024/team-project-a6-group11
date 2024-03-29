package com.example.musicdiary;

public class DiaryPreviewItem {
    private String date;
    private String trackName;

    public DiaryPreviewItem(String date, String trackName) {
        this.date = date;
        this.trackName = trackName;
    }

    public String getDate() {
        return date;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}
