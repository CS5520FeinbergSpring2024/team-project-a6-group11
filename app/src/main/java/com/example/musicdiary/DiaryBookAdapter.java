package com.example.musicdiary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaryBookAdapter extends RecyclerView.Adapter<DiaryBookAdapter.DiaryPreviewViewHolder> {
    List<DiaryPreviewItem> diaryPreviewList;

    public DiaryBookAdapter(List<DiaryPreviewItem> diaryPreviewList) {
        this.diaryPreviewList = diaryPreviewList;
    }

    public class DiaryPreviewViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView trackNameTextView;

        public DiaryPreviewViewHolder(@NonNull View itemView, TextView dateTextView, TextView trackNameTextView) {
            super(itemView);
            this.dateTextView = dateTextView;
            this.trackNameTextView = trackNameTextView;
        }
    }


    @NonNull
    @Override
    public DiaryPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryPreviewViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
