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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryBookAdapter extends RecyclerView.Adapter<DiaryBookAdapter.DiaryPreviewViewHolder> {
    public static List<DiaryPreviewItem> diaryPreviewList;

    public DiaryBookAdapter(List<DiaryPreviewItem> diaryPreviewList) {
        DiaryBookAdapter.diaryPreviewList = diaryPreviewList;
    }

    public static class DiaryPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateTextView;
        TextView trackNameTextView;
        TextView authorPostTextView;
        ImageView albumCoverImageView;
        ImageButton sendEntryButton;

        public DiaryPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            trackNameTextView = itemView.findViewById(R.id.trackNameTextView);
            authorPostTextView = itemView.findViewById(R.id.authorPostTextView);
            albumCoverImageView = itemView.findViewById(R.id.imageViewAlbumCover);
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

        sendButton.setOnClickListener(view -> {
            String toUsername = usernameEditText.getText().toString().trim();

            if (toUsername.isEmpty()) {
                Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show();

                return;
            }
            if (toUsername.equals(MainActivity.username)) {
                Toast.makeText(context, "You can not send an entry to yourself!", Toast.LENGTH_SHORT).show();

                return;
            }

            DatabaseReference diaryUsersReference = MainActivity.mDatabase.child("diary_users");
            diaryUsersReference.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(context, "Failed to contact the database!", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    return;
                }

                String toUsernameKey = null;
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    Object _toUsernameObject = dataSnapshot.child("username").getValue();
                    if (_toUsernameObject != null) {
                        String _toUsername = _toUsernameObject.toString();

                        if (toUsername.equals(_toUsername)) {
                            toUsernameKey = dataSnapshot.getKey();
                            break;
                        }
                    }
                }

                if (toUsernameKey == null) {
                    Toast.makeText(context, "There is no user by the name '" + toUsername + "'!", Toast.LENGTH_SHORT).show();

                    return;
                }

                DatabaseReference diaryUserReference = MainActivity.mDatabase.child("diary_users").child(toUsernameKey);

                Map<String, Object> updateFields = new HashMap<>();
                updateFields.put("authorID", diaryPreviewList.get(position).getAuthorID());
                updateFields.put("author", diaryPreviewList.get(position).getAuthor());
                updateFields.put("date", diaryPreviewList.get(position).getDate());
                updateFields.put("trackName", diaryPreviewList.get(position).getTrackName());
                updateFields.put("trackArtists", diaryPreviewList.get(position).getTrackArtists());
                updateFields.put("coverURL", diaryPreviewList.get(position).getCoverURL());
                updateFields.put("postText", diaryPreviewList.get(position).getPostText());
                updateFields.put("previewURL", diaryPreviewList.get(position).getPreviewURL());
                updateFields.put("mood", diaryPreviewList.get(position).getMood());

                String messageID = diaryUserReference.child("recv_diary_entries").push().getKey();

                if (messageID == null) {
                    Toast.makeText(context, "Failed to send the entry to the user '" + toUsername + "'!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Task<Void> sendDiaryEntryTask = diaryUserReference.child("recv_diary_entries").child(messageID).updateChildren(updateFields);
                sendDiaryEntryTask.addOnCompleteListener(_sendDiaryEntryTask -> {
                    if (!_sendDiaryEntryTask.isSuccessful()) {
                        Toast.makeText(context, "Failed to send the entry to the user '" + toUsername + "'!", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        return;
                    }

                    Toast.makeText(context, "Sent the diary entry to the user '" + toUsername + "'!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
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
        if (item.getCoverURL() != null) {
            Picasso.get().load(item.getCoverURL()).into(holder.albumCoverImageView);
        }

        if (item.getAuthor() != null && !(item.getAuthor().equals(MainActivity.username))) {
            holder.sendEntryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return diaryPreviewList.size();
    }
}
