package com.techinfinitystudios.notesplus;

public class Note {
    private long id;
    private String title;
    private String text;


    public Note(long id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public String getText() {
        return text;
    }


}
