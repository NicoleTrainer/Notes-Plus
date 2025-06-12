package com.techinfinitystudios.notesplus;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private FloatingActionButton createNoteButton;
    private List<Note> noteList;
    private NoteAdapter adapter;

    private RecyclerView recyclerView;
    private String filter = "";
    private String click = "0";

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
        createNoteButton = findViewById(R.id.addNoteButton);
        recyclerView = findViewById(R.id.notesRecyclerView);

        int screenWidthDp = getResources().getConfiguration().screenWidthDp;

        int spanCount = Math.max(1, screenWidthDp / 160);
        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList, dbHelper);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        showNotes();

        FloatingActionButton filterButton = findViewById(R.id.filterButton);

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


        createNoteButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.activity_edit_note);
            dialog.show();

            ImageButton backButton = dialog.findViewById(R.id.backButton);
            EditText noteTitle = dialog.findViewById(R.id.noteTitle);
            EditText noteText = dialog.findViewById(R.id.noteText);
            backButton.setOnClickListener(v1 -> {
                        String title = noteTitle.getText().toString();
                        String text = noteText.getText().toString();

                        if (!text.isEmpty() || !title.isEmpty()){
                            dbHelper.insertNote(title, text);
                            showNotes();
                            dialog.dismiss();
                        }
                        else{
                            dialog.dismiss();
                        }


            });

        });
        adapter.setNoteOnClickListener( (note, position) -> {
            Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.activity_edit_note);
            dialog.show();

            ImageButton backButton = dialog.findViewById(R.id.backButton);
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

        });

        adapter.setOnNoteLongClickListener((note, position) -> {
            //Allow user to select notes to delete

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

