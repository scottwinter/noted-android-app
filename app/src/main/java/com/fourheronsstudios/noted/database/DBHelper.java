package com.fourheronsstudios.noted.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fourheronsstudios.noted.model.Note;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Notes";
    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String NOTE_ID = "note_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_BODY = "body";
    private static final String COLUMN_LAST_UPDATED = "last_updated";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " + NOTE_ID + " VARCHAR, " + COLUMN_TITLE +
                " VARCHAR, " + COLUMN_BODY + " VARCHAR, " + COLUMN_LAST_UPDATED + " VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void resetDatabaseTable() {
        SQLiteDatabase notesDB = this.getWritableDatabase();
        notesDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(notesDB);
    }

    public long createNewNote(String noteId, String title, String body, long datetime) {
        long id;
        SQLiteDatabase notesDB = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put(NOTE_ID, noteId);
        content.put(COLUMN_TITLE, title);
        content.put(COLUMN_BODY, body);
        content.put(COLUMN_LAST_UPDATED, datetime);
        try {
            id = notesDB.insert(TABLE_NAME, null, content);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return id;
    }

    public void updateNote(int id, String noteId, String title, String body, long datetime) {
        SQLiteDatabase notesDB = this.getWritableDatabase();

        String[] whereArgs = new String[1];
        whereArgs[0] = "" + id;
        String whereClaus = "id = ?";

        ContentValues content = new ContentValues();
        content.put(NOTE_ID, noteId);
        content.put(COLUMN_TITLE, title);
        content.put(COLUMN_BODY, body);
        content.put(COLUMN_LAST_UPDATED, datetime);
        try {
            notesDB.update(TABLE_NAME, content, whereClaus, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Note> getAllNotes() {
        SQLiteDatabase notesDB = this.getReadableDatabase();
        Cursor c = null;
        ArrayList<Note> notes = new ArrayList<>();
        try {
            c = notesDB.rawQuery("SELECT " + COLUMN_ID + ", " + NOTE_ID + ", " + COLUMN_TITLE + ", " +
                    COLUMN_BODY + ", " + COLUMN_LAST_UPDATED + " FROM " +
                    TABLE_NAME + " ORDER BY " + COLUMN_LAST_UPDATED + " DESC", null);

            int idIndex = c.getColumnIndex(COLUMN_ID);
            int noteIdIndex = c.getColumnIndex(NOTE_ID);
            int titleIndex = c.getColumnIndex(COLUMN_TITLE);
            int bodyIndex = c.getColumnIndex(COLUMN_BODY);
            int dateIndex = c.getColumnIndex(COLUMN_LAST_UPDATED);

            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                Note currentNote =
                        new Note(c.getInt(idIndex), c.getString(noteIdIndex), c.getString(titleIndex), c.getString(bodyIndex),
                                c.getString(dateIndex));
                notes.add(currentNote);
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(c != null) {
                c.close();
            }
        }
        return notes;
    }

    public Note getSingleNote(int id) {
        SQLiteDatabase notesDB = this.getReadableDatabase();
        Cursor c = null;
        Note note = null;
        try {
            c = notesDB.rawQuery("SELECT " + COLUMN_ID + ", " + NOTE_ID + ", " + COLUMN_TITLE + ", " +
                    COLUMN_BODY + ", " + COLUMN_LAST_UPDATED + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + id, null);

            int idIndex = c.getColumnIndex(COLUMN_ID);
            int noteIdIndex = c.getColumnIndex(NOTE_ID);
            int titleIndex = c.getColumnIndex(COLUMN_TITLE);
            int bodyIndex = c.getColumnIndex(COLUMN_BODY);
            int dateIndex = c.getColumnIndex(COLUMN_LAST_UPDATED);

            c.moveToFirst();
            note = new Note(c.getInt(idIndex), c.getString(noteIdIndex), c.getString(titleIndex), c.getString(bodyIndex),
                    c.getString(dateIndex));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(c != null) {
                c.close();
            }
        }
        return note;
    }

    public void deleteNote(int id) {
        SQLiteDatabase notesDB = this.getWritableDatabase();
        try {
            notesDB.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllNotes() {
        SQLiteDatabase notesDB = this.getWritableDatabase();
        notesDB.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
