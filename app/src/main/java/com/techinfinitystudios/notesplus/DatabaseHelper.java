package com.techinfinitystudios.notesplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "note.db";
    private static final int DATABASE_VERSION = 4;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, text TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }


    public long insertNote(String title, String text) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("text", text);

        long id = db.insert("notes", null, values);


        db.close();
        return id;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notes", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    public void updateNote(Long id, String title, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("text", text);
        db.update("notes", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();

    }

    public List<Note> getAllNotes( String filter, String click) {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        if (filter.equals("date")) {
            if(Objects.equals(click, "1")){
                cursor = db.rawQuery("SELECT * FROM notes ORDER BY id DESC", null);
            }
            else {
                cursor = db.rawQuery("SELECT * FROM notes ORDER BY id ASC", null);
            }
        } else if (filter.equals("title")) {
            if(Objects.equals(click, "1")){
                cursor = db.rawQuery("SELECT * FROM notes ORDER BY title DESC", null);
            }
            else {
                cursor = db.rawQuery("SELECT * FROM notes ORDER BY title ASC", null);
            }
        }
        else {
             cursor = db.rawQuery("SELECT * FROM notes", null);
        }


        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id")); // Ensure this matches column name
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));

                noteList.add(new Note(id, title, text));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }



}