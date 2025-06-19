package com.techinfinitystudios.notesplus;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{
private List<Note> noteList;
private DatabaseHelper dbHelper;
private List<Note> selectedNotes;
private CheckBox checkBox;
private LinearLayout containerLayout;
private boolean multiSelectMode = false;


    public NoteAdapter(List<Note> noteList, DatabaseHelper dbHelper, List<Note> selectedNotes) {
        this.noteList = noteList;
        this.dbHelper = dbHelper;
        this.selectedNotes = selectedNotes;

    }

    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    public void setSelectedNotes(List<Note> selectedNotes) {
        this.selectedNotes = selectedNotes;
    }
    public void setMultiSelectMode(boolean multiSelectMode) {
        this.multiSelectMode = multiSelectMode;
    }

    private OnNoteLongClickListener longClickListener;
    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, int position);
    }

    public void setOnNoteLongClickListener(OnNoteLongClickListener listener) {
        this.longClickListener = listener;
    }
    public interface OnNoteClickListener {
        void onNoteClick(Note note, int position);
    }

    private OnNoteClickListener clickListener;

    public void setNoteOnClickListener(OnNoteClickListener listener) {
        this.clickListener = listener;
    }



    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteText.setText(note.getText());

        // Show or hide checkbox
        if (multiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        checkBox.setChecked(note.isSelected());

        // Prevent triggering the listener when setting the checked state
        holder.checkBox.setOnCheckedChangeListener(null);

        // Update selection state on checkbox click
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            note.setSelectedItem(isChecked);
            if (isChecked) {
                if (!selectedNotes.contains(note)) {
                    selectedNotes.add(note);
                }
            } else {
                selectedNotes.remove(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNoteLongClick(note, position);
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteClick(note, position);
            }
        });


    }


    @Override
    public int getItemCount() {
        return noteList.size();
    }



    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteText;
        CheckBox checkBox;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.noteText);
            checkBox = itemView.findViewById(R.id.checkBox);
            noteTitle = itemView.findViewById(R.id.noteTitle);
        }

    }
}
