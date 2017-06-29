package com.fourheronsstudios.noted.dto;

public class Note {

    private int id;

    private String noteId;

    private String title;

    private String body;

    private String date;

    public Note (int id, String noteId, String title, String body, String date) {
        this.id = id;
        this.noteId = noteId;
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public Note (String title, String body) {
        this.title = title;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    public String toJson(){
        return "{" +
                "\"id\":" + id +"," +
                "\"noteId\":\"" + noteId + "\"," +
                "\"title\":\"" + title + "\"," +
                "\"body\":\"" + body + "\"," +
                "\"date\":" + date +
                "}";
    }
}
