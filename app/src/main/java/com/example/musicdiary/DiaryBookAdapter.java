package com.example.musicdiary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaryBookAdapter extends RecyclerView.Adapter<DiaryBookAdapter.DiaryPreviewViewHolder> {
    public static List<DiaryPreviewItem> diaryPreviewList;

    public DiaryBookAdapter(List<DiaryPreviewItem> diaryPreviewList) {
        DiaryBookAdapter.diaryPreviewList = diaryPreviewList;
    }

    public static class DiaryPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateTextView;
        TextView trackNameTextView;

        public DiaryPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            trackNameTextView = itemView.findViewById(R.id.trackNameTextView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            Intent intent = new Intent(context, SingleEntryActivity.class);
            intent.putExtra("openedEntryDate", diaryPreviewList.get(getAdapterPosition()).getDate());
            intent.putExtra("openedEntryTrackName", diaryPreviewList.get(getAdapterPosition()).getTrackName());
            context.startActivity(intent);
        }
    }


    @NonNull
    @Override
    public DiaryPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiaryPreviewViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryPreviewViewHolder holder, int position) {
        holder.dateTextView.setText(diaryPreviewList.get(position).getDate());
        holder.trackNameTextView.setText(diaryPreviewList.get(position).getTrackName());
    }

    @Override
    public int getItemCount() {
        return diaryPreviewList.size();
    }
}
