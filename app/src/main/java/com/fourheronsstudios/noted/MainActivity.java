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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes;
    private RecyclerView mRecyclerView;
    private NotedUtils utils;

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


        // FIREBASE get list of results for specific user
        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
        DatabaseReference database = databaseInstance.getReference("users").child("scott");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                for(DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    Log.i("Firebase", "--------------------------------------Note from Firebase: " + note);
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

        // END FIREBASE
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
        } else if (item.getItemId() == R.id.sync) {
            Log.i("Info log", "Cloud syncing.");
            utils.cloudSync(this);
        }
//        else if (item.getItemId() == R.id.importNotes) {
//            Log.i("Notes Import", "Importing Notes");
//            importData();
//        }
        return super.onOptionsItemSelected(item);
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