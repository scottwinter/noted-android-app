package com.fourheronsstudios.noted;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.model.Note;
import com.fourheronsstudios.noted.utils.DatabaseUtil;
import com.fourheronsstudios.noted.utils.NotedUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes;
    private RecyclerView mRecyclerView;
    private NotedUtils utils;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new NotedUtils();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("All Notes");
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // RecyclerView Decoration
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());

        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        // End Decoration

        FloatingActionButton fab;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
                intent.putExtra("noteId", -1);
                startActivity(intent);
            }
        });

        populateNoteList();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        // FIREBASE get list of results for specific user
//        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
//        DatabaseReference database = databaseInstance.getReference("users").child("scott");
//
//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Get Post object and use the values to update the UI
//
//                for(DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
//                    Note note = noteSnapshot.getValue(Note.class);
////                    Log.i("Firebase", "--------------------------------------Note from Firebase: " + note);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Getting Post failed, log a message
//                Log.w("loadPost:onCancelled", databaseError.toException());
//                // ...
//            }
//        };
//        database.addListenerForSingleValueEvent(postListener);

        // END FIREBASE
    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.i("Firebase Auth", "current user is: " + currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateNoteList();
    }

    public void populateNoteList() {
        DBHelper dbHelper = new DBHelper(this);
        try {
            //dbHelper.resetDatabaseTable();
            notes = dbHelper.getAllNotes();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        MyAdapter mAdapter = new MyAdapter(notes, dbHelper);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_note_menu, menu);

        if (!isExternalStorageAvailable()
//                || isExternalStorageReadOnly()
                ) {
            menu.getItem(2).setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.newNote) {
            Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
            intent.putExtra("noteId", -1);
            startActivity(intent);
        } else if (item.getItemId() == R.id.backup) {
            Log.i("Info log", "Cloud syncing.");
            cloudBackup(this);
        } else if (item.getItemId() == R.id.restore) {
            Log.i("Info log", "Cloud syncing.");
            cloudRestore(this);
        }
//        else if (item.getItemId() == R.id.importNotes) {
//            Log.i("Notes Import", "Importing Notes");
//            importData();
//        }
        return super.onOptionsItemSelected(item);
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
                populateNoteList();
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

    public void cloudBackup(Context context){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference noteRef = db.collection("users").document("test-user").collection("notes").document("test-note-1");

        Note note = new Note();
        note.setNoteId("test-1234");
        note.setTitle("This is a test note AGAIN");
        note.setBody("Hoping this test note works in Firestore");

        Log.i("Firestore", "Backing up data to Firestore");

        noteRef.set(note, SetOptions.merge());


//        DBHelper dbHelper = new DBHelper(context);
//        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
//        DatabaseReference database = databaseInstance.getReference();
//
//        List<Note> allNotes = dbHelper.getAllNotes();
//        Map<String, Note> allNotesMap = new HashMap<>();
//
//        for(Note note : allNotes){
////            allNotesMap.put(note.getNoteId(), note);
//            database.child("users").child("scott").child(note.getNoteId()).setValue(note);
//            Log.i("Sync log", "Note ID: " + note.getNoteId());
//        }

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

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    private void exportData() {
        Log.i("All Notes List", notes.toString());
        JSONArray notesJson = new JSONArray();
        if (notes.size() > 0) {
            for (Note note : notes) {
                try {
                    notesJson.put(new JSONObject(note.toJson()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            File noteDir = getNotesStorageDir(this, "notedExport");

            final File file = new File(noteDir, "notes.json");

            // Save your stream, don't forget to flush() it before closing it.
            if (file.exists()) {
                file.delete();
            }
            try {
                Log.i("File Directory", file.getPath());
                boolean fileCreated = file.createNewFile();
                if (fileCreated) {
                    Log.i("File Creation", "Inside fileCreated " + fileCreated);
                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(notesJson.toString());

                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();
                } else {
                    Log.e("File Creation Error", "File was not created.");
                }
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No notes to export.", Toast.LENGTH_LONG).show();
        }
    }

    public void importData() {
        DBHelper dbHelper = new DBHelper(this);
        String notesString = getTextFileData("notes.json");
        Log.i("Read Notes", notesString);

        JSONArray notesArray = null;
        try {
            notesArray = new JSONArray(notesString);
        } catch (JSONException je) {
            Log.e("JSON Exception", je.getMessage());
        }
        if (notesArray != null) {
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject noteJson;
                boolean noteExists = false;
                try {
                    noteJson = notesArray.getJSONObject(i);

                    for (Note note : notes) {
                        Log.i("Note ID Note", note.getNoteId());
                        Log.i("Note ID JSON", noteJson.getString("noteId"));
                        if (note.getNoteId().equals(noteJson.getString("noteId"))) {
                            noteExists = true;
                        }
                    }

                    if (!noteExists) {
                        Log.i("Note Does Not Exists", noteJson.toString());

                        dbHelper.createNewNote(noteJson.getString("noteId"), noteJson.getString("title"),
                                noteJson.getString("body"), noteJson.getLong("date"));
                    } else {
                        Log.i("Note Exists", noteJson.toString());
                    }

                } catch (JSONException je) {
                    Log.e("JSON Exception", je.toString());
                }
            }

            populateNoteList();
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to import.", Toast.LENGTH_LONG).show();
        }
    }

    public File getNotesStorageDir(Context context, String notesName) {
        // Get the directory for the app's private pictures directory.
        File file;
        if (isExternalStorageAvailable()
//                && isExternalStorageReadOnly()
                ) {

            file = new File(context.getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS), notesName);
            Log.i("Storage Directory", "External Storage: " + file.getPath());
        } else {
            file = new File(context.getFilesDir(), notesName);
            Log.i("Storage Directory", "Internal Storage: " + file.getPath());
        }

        if (!file.mkdirs()) {
            Log.e("External File Storage", "Directory not created");
        }
        return file;
    }

    public String getTextFileData(String fileName) {

        // Get the directory of SD Card
        File noteDir = getNotesStorageDir(this, "notedExport");

        // Get The Text file
        File txtFile = new File(noteDir, fileName);

        // Read the file Contents in a StringBuilder Object
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(txtFile));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            Log.e("Error Reading File", "Error occurred while reading text file!!");
        }

        return text.toString();
    }
}