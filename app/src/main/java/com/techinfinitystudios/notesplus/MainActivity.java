package com.techinfinitystudios.notesplus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private FloatingActionButton addNoteButton, multiDeleteButton, multiCancelButton, filterButton;
    private List<Note> noteList;
    private List<Note> selectedNotes;
    private NoteAdapter adapter;
    private CheckBox selectAllCheckBox;

    private RecyclerView recyclerView;
    private String filter = "";
    private String click = "0";
    private boolean multiSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View rootView = findViewById(R.id.main); // Root ConstraintLayout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fabLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            layoutParams.bottomMargin = systemBars.bottom + (int) (16 * getResources().getDisplayMetrics().density); // 16dp padding
            v.setLayoutParams(layoutParams);

            return insets;
        });



        getStartFilter();


        dbHelper = new DatabaseHelper(this);
        addNoteButton = findViewById(R.id.addNoteButton);
        recyclerView = findViewById(R.id.notesRecyclerView);

        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        multiDeleteButton = findViewById(R.id.multiDeleteButton);
        multiCancelButton = findViewById(R.id.multiCancelButton);
        selectedNotes = new ArrayList<>();

        ConstraintLayout constraintLayout = findViewById(R.id.multiSelectToolbar);

        int screenWidthDp = getResources().getConfiguration().screenWidthDp;

        int spanCount = Math.max(1, screenWidthDp / 160);
        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList, dbHelper, selectedNotes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));


        showNotes();

        filterButton = findViewById(R.id.filterButton);

        filterButton.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(this, filterButton);
            popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.filter_date){
                        filter = "date";
                        saveFilter(filter);
                    } else if (item.getItemId() == R.id.filter_title) {
                        filter = "title";
                        saveFilter(filter);
                    }

                if(click.equals("0")){
                    click = "1";

                    saveClick(click);
                }
                else {
                    click = "0";
                    saveClick(click);
                }

                showNotes();  // <-- Move here to apply after filter changes
                return true;        // Return true to indicate event handled
            });

        });



            addNoteButton.setOnClickListener(v -> {
                if (multiSelectMode) return;
                Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.activity_edit_note);
                dialog.show();

                ImageButton backButton = dialog.findViewById(R.id.backButton);
                ImageButton deleteButton = dialog.findViewById(R.id.deleteButton);
                EditText noteTitle = dialog.findViewById(R.id.noteTitle);
                EditText noteText = dialog.findViewById(R.id.noteText);

                deleteButton.setVisibility(View.GONE);

                backButton.setOnClickListener(v1 -> {
                    String title = noteTitle.getText().toString();
                    String text = noteText.getText().toString();

                    if (!text.isEmpty() || !title.isEmpty()) {
                        dbHelper.insertNote(title, text);
                        showNotes();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }


                });

                dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                        String title = noteTitle.getText().toString();
                        String text = noteText.getText().toString();

                        if (!text.isEmpty() || !title.isEmpty()) {
                            dbHelper.insertNote(title, text);
                            showNotes();
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                        }

                    }
                    return false;
                });
            });
            adapter.setNoteOnClickListener((note, position) -> {

                if (!multiSelectMode) {

                    Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.setContentView(R.layout.activity_edit_note);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Remove grey dim

                    dialog.show();

                    ImageButton backButton = dialog.findViewById(R.id.backButton);
                    ImageButton deleteButton = dialog.findViewById(R.id.deleteButton);
                    EditText noteTitle = dialog.findViewById(R.id.noteTitle);
                    EditText noteText = dialog.findViewById(R.id.noteText);

                    noteTitle.setText(note.getTitle());
                    noteText.setText(note.getText());

                    backButton.setOnClickListener(v1 -> {
                        String title = noteTitle.getText().toString();
                        String text = noteText.getText().toString();
                        dbHelper.updateNote(note.getId(), title, text);
                        showNotes();
                        dialog.dismiss();
                    });
                    deleteButton.setOnClickListener(v1 -> {
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Delete Note");
                        alertDialog.setMessage("Are you sure you want to delete this note?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog12, which) -> {
                                    alertDialog.dismiss();
                                    dbHelper.deleteNote(note.getId());
                                    dialog.dismiss();
                                    Toast toast = Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT);
                                    toast.show();
                                    dialog.dismiss();
                                    showNotes();

                                }
                        );
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog12, which) -> {
                                    alertDialog.dismiss();
                                }
                        );
                        alertDialog.show();

                    });
                }
                else {

                    if (selectedNotes.contains(note)) {
                        selectedNotes.remove(note);
                        note.setSelectedItem(false);

                    } else {
                        selectedNotes.add(note);
                        note.setSelectedItem(true);

                    }
                }
                adapter.setSelectedNotes(selectedNotes);
                adapter.notifyDataSetChanged();
            });


        multiDeleteButton.setOnClickListener(v1 -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Delete Notes");
            alertDialog.setMessage("Are you sure you want to delete these notes?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog12, which) -> {
                alertDialog.dismiss();

                for (Note note : new ArrayList<>(selectedNotes)) {
                    dbHelper.deleteNote(note.getId());  // Must actually delete from DB
                }

                selectedNotes.clear();
                multiSelectMode = false;
                constraintLayout.setVisibility(View.GONE);
                filterButton.setVisibility(View.VISIBLE);
                addNoteButton.setVisibility(View.VISIBLE);
                selectAllCheckBox.setVisibility(View.GONE);

                showNotes();  // Reload notes from DB
                adapter.setMultiSelectMode(false);
                adapter.setSelectedNotes(selectedNotes);
                adapter.notifyDataSetChanged();

                Toast.makeText(this, "Notes deleted", Toast.LENGTH_SHORT).show();
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog12, which) -> alertDialog.dismiss());
            alertDialog.show();
        });



        adapter.setOnNoteLongClickListener((note, position) -> {

            constraintLayout.setVisibility(View.VISIBLE);
            selectAllCheckBox.setVisibility(View.VISIBLE);
            filterButton.setVisibility(View.GONE);
            addNoteButton.setVisibility(View.GONE);
            multiSelectMode = true;

            selectedNotes.add(note);
            adapter.setSelectedNotes(selectedNotes);
            note.setSelectedItem(true);
            adapter.setMultiSelectMode(true);
            adapter.notifyDataSetChanged();
        });

        selectAllCheckBox.setOnClickListener( v1 -> {
            if(selectAllCheckBox.isChecked()){
                selectedNotes.clear();
                for (Note note : noteList) {
                    note.setSelectedItem(true);
                    selectedNotes.add(note);
                }
            }

            adapter.setSelectedNotes(selectedNotes);
            adapter.notifyDataSetChanged();

        });





    }

    private void saveClick(String click) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("click", click).apply();
    }

    private void getStartFilter() {
        filter = getSharedPreferences("prefs", MODE_PRIVATE).getString("current_filter", "date");
        click = getSharedPreferences("prefs", MODE_PRIVATE).getString("click", "0");
    }
    private void saveFilter(String filter) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("current_filter", filter).apply();
    }

    private void showNotes() {
        noteList.clear();
        List<Note> notesFromDB = dbHelper.getAllNotes(filter, click);
        Log.d("NotesActivity", "Notes loaded from DB: " + notesFromDB.size());
        noteList.addAll(notesFromDB);
        adapter.notifyDataSetChanged();
    }

}

