package com.example.musicdiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
        TextView authorPostTextView;
        ImageButton sendEntryButton;

        public DiaryPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            trackNameTextView = itemView.findViewById(R.id.trackNameTextView);
            authorPostTextView = itemView.findViewById(R.id.authorPostTextView);
            sendEntryButton = itemView.findViewById(R.id.sendEntryButton);

            itemView.setOnClickListener(this);
            sendEntryButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            int position = getAdapterPosition();

            if (view == sendEntryButton) {
                sendEntry(context, position);
            } else {
                viewEntry(context, position);
            }
        }
    }

    private static void viewEntry(Context context, int position) {
        Intent intent = new Intent(context, SingleEntryActivity.class);
        intent.putExtra("openedEntryDate", diaryPreviewList.get(position).getDate());
        intent.putExtra("openedEntryTrackName", diaryPreviewList.get(position).getTrackName());
        intent.putExtra("openedEntryTrackArtists", diaryPreviewList.get(position).getTrackArtists());
        intent.putExtra("openedEntryCoverURL", diaryPreviewList.get(position).getCoverURL());
        intent.putExtra("openedEntryPostText",diaryPreviewList.get(position).getPostText());
        intent.putExtra("openedPreviewURL", diaryPreviewList.get(position).getPreviewURL());
        intent.putExtra("openedMood", diaryPreviewList.get(position).getMood());
        intent.putExtra("sharedDiaryReference", DiaryBookActivity.sharedDiaryReference); // should be null if "your diary" is opened
        context.startActivity(intent);
    }

    private static void sendEntry(Context context, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send Diary Entry");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_send_entry, null);
        builder.setView(dialogView);

        EditText usernameEditText = dialogView.findViewById(R.id.usernameEditText);
        Button sendButton = dialogView.findViewById(R.id.sendButton);

        AlertDialog dialog = builder.create();

        sendButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (!username.isEmpty()) {
                System.out.println("Send data: " + username + ", " + diaryPreviewList.get(position));
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    @NonNull
    @Override
    public DiaryPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiaryPreviewViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryPreviewViewHolder holder, int position) {
        DiaryPreviewItem item = diaryPreviewList.get(position);

        String authorText;
        if (item.getPostText() != null && !item.getPostText().isEmpty()) {
            authorText = item.getAuthor() + " wrote: \"" + item.getPostText() + "\"";
        } else {
            authorText = item.getAuthor() + " listened to:";
        }

        holder.authorPostTextView.setText(authorText);
        holder.dateTextView.setText(item.getDate());
        holder.trackNameTextView.setText(item.getTrackArtists() + " - " + item.getTrackName());

        if (item.getAuthor() != null && !(item.getAuthor().equals(MainActivity.username))) {
            holder.sendEntryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return diaryPreviewList.size();
    }
}
