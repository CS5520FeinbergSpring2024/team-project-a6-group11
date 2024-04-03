package com.example.musicdiary;

import androidx.annotation.NonNull;

public class DiaryPreviewItem {

    private String author;
    private String date;
    private String trackName;
    private String coverURL;
    private String postText;

    public DiaryPreviewItem() {
    }

    public DiaryPreviewItem(String author, String date, String trackName, String coverURL, String postText) {
        this.author = author;
        this.date = date;
        this.trackName = trackName;
        this.coverURL = coverURL;
        this.postText = postText;
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

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    @Override
    public String toString() {
        return "DiaryPreviewItem{" +
                "author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", trackName='" + trackName + '\'' +
                ", coverURL='" + coverURL + '\'' +
                ", postText='" + postText + '\'' +
                '}';
    }
}
