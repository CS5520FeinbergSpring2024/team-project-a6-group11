package com.example.musicdiary;

import androidx.annotation.NonNull;

public class DiaryPreviewItem {

    private String author;
    private String date;
    private String trackName;
    private String coverURL;

    public DiaryPreviewItem() {
    }

    public DiaryPreviewItem(String author, String date, String trackName, String coverURL) {
        this.author = author;
        this.date = date;
        this.trackName = trackName;
        this.coverURL = coverURL;
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

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    @NonNull
    @Override
    public String toString() {
        return "DiaryPreviewItem{" +
                "author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", trackName='" + trackName + '\'' +
                ", coverURL='" + coverURL + '\'' +
                '}';
    }
}
