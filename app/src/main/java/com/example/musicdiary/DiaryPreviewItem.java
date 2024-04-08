package com.example.musicdiary;

import androidx.annotation.NonNull;

public class DiaryPreviewItem {
    private String authorID;
    private String author;
    private String date;
    private String trackName;
    private String trackArtists;
    private String coverURL;
    private String postText;
    private String previewURL;
    private String mood;

    public DiaryPreviewItem() { }

    public DiaryPreviewItem(String authorID, String author, String date, String trackName, String trackArtists, String coverURL, String postText, String previewURL, String mood) {
        this.authorID = authorID;
        this.author = author;
        this.date = date;
        this.trackName = trackName;
        this.trackArtists = trackArtists;
        this.coverURL = coverURL;
        this.postText = postText;
        this.previewURL = previewURL;
        this.mood = mood;
    }

    public String getAuthorID() {
        return authorID;
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

    public String getTrackArtists() {
        return trackArtists;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public String getPostText() {
        return postText;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public String getMood() {
        return mood;
    }

    @NonNull
    @Override
    public String toString() {
        return "DiaryPreviewItem{" +
                "authorID='" + authorID + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", trackName='" + trackName + '\'' +
                ", trackArtists='" + trackArtists + '\'' +
                ", coverURL='" + coverURL + '\'' +
                ", postText='" + postText + '\'' +
                ", previewURL='" + previewURL + '\'' +
                ", mood='" + mood + '\'' +
                '}';
    }
}
