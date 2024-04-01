package com.example.musicdiary;

import androidx.annotation.NonNull;

public class DiaryPreviewItem {

    private String author;
    private String date;
    private String trackName;

    public DiaryPreviewItem(String author, String date, String trackName) {
        this.author = author;
        this.date = date;
        this.trackName = trackName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    @NonNull
    @Override
    public String toString() {
        return "DiaryPreviewItem{" +
                "author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", trackName='" + trackName + '\'' +
                '}';
    }
}
