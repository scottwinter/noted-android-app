package com.fourheronsstudios.noted.utils;


import android.content.Context;
import android.util.Log;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.model.Note;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotedUtils {

    private DBHelper dbHelper;

    public void cloudSync(Context context){

        dbHelper = new DBHelper(context);
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
        - Update any UI elements (Local method to class calling cloudSync)
         */



        // FIREBASE TESTING
        // Write a message to the database

//        database.child("users").child("scott").setValue(user);

        // END FIREBASE TESTING

        // FIREBASE add new note to firebase
        Log.i("Sync log", "Updating firebase.");
        database.child("users").child("scott").setValue(allNotesMap);

    }
}


