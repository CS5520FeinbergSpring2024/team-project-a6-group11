package com.example.musicdiary;

import androidx.annotation.NonNull;

public class DiaryPreviewItem {
    private String author;
    private String date;
    private String trackName;
    private String coverURL;
    private String postText;
    private String previewURL;

    public DiaryPreviewItem() { }

    public DiaryPreviewItem(String author, String date, String trackName, String coverURL, String postText, String previewURL) {
        this.author = author;
        this.date = date;
        this.trackName = trackName;
        this.coverURL = coverURL;
        this.postText = postText;
        this.previewURL = previewURL;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public String getPreviewURL() { return previewURL; }

    public String getPostText() {
        return postText;
    }

    @NonNull
    @Override
    public String toString() {
        return "DiaryPreviewItem{" +
                "author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", trackName='" + trackName + '\'' +
                ", coverURL='" + coverURL + '\'' +
                ", postText='" + postText + '\'' +
                ", previewURL='" + previewURL + '\'' +
                '}';
    }
}
