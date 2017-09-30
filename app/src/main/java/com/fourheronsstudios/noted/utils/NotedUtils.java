package com.fourheronsstudios.noted.utils;


import android.content.Context;
import android.util.Log;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.model.Note;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotedUtils {

//    private DBHelper dbHelper;

    public void cloudBackup(Context context){

        DBHelper dbHelper = new DBHelper(context);
        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
        DatabaseReference database = databaseInstance.getReference();

        List<Note> allNotes = dbHelper.getAllNotes();
        Map<String, Note> allNotesMap = new HashMap<>();

        for(Note note : allNotes){
//            allNotesMap.put(note.getNoteId(), note);
            database.child("users").child("scott").child(note.getNoteId()).setValue(note);
            Log.i("Sync log", "Note ID: " + note.getNoteId());
        }

        /*
        This is the general sudo code
        - get all notes from local database
        - loop through all notes
            - Update firebase using setValue for each note
        - Get all notes back from firebase
        - Update local database with all data from firebase
        - Update any UI elements (Local method to class calling cloudBackup)
         */

    }


    public void cloudRestore(Context context){
        List<Note> allNotes = new ArrayList<>();

        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
        DatabaseReference database = databaseInstance.getReference("users").child("scott");
        final DBHelper dbHelper = new DBHelper(context);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("Firebase", "On change method executed");
                dbHelper.deleteAllNotes();
                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    Note note = noteSnapshot.getValue(Note.class);
                    if(note != null) {
                        Log.i("Firebase Test", "Note was not null: " + note);

                        dbHelper.createNewNote(note.getNoteId(), note.getTitle(), note.getBody(), Long.valueOf(note.getDate()));
                    }

                    Log.i("From Firebase", note.toString());
//                    allNotes.add(note);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        database.addListenerForSingleValueEvent(postListener);

        allNotes = dbHelper.getAllNotes();
        Log.i("Firebase List", "Note size from list: " + allNotes.size());
        for(Note note : allNotes){
            Log.i("From Firebase List", note.toString());
        }
    }
}


