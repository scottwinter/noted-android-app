package com.fourheronsstudios.noted;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.dto.Note;

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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
//    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("All Notes");
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
//                mLayoutManager.getOrientation());
//        mRecyclerView.addItemDecoration(dividerItemDecoration);
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
        mAdapter = new MyAdapter(notes, dbHelper);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_note_menu, menu);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
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
        } else if (item.getItemId() == R.id.backupNotes) {
            Log.i("Info log", "Backup option menu item clicked.");
            exportData();
        } else if (item.getItemId() == R.id.importNotes) {
            Log.i("Notes Import", "Importing Notes");
            importData();
        }
        return super.onOptionsItemSelected(item);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private void exportData() {
        Log.i("All Notes List", notes.toString());
        JSONArray notesJson = new JSONArray();

        for (Note note : notes) {
            try {
                notesJson.put(new JSONObject(note.toJson()));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        File noteDir = getNotesStorageDir(this, "notedExport");

        final File file = new File(noteDir, "notes.json");

        // Save your stream, don't forget to flush() it before closing it.
        if(file.exists()){
            file.delete();
        }
        try
        {
            boolean fileCreated = file.createNewFile();
            if(fileCreated) {
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(notesJson.toString());

                myOutWriter.close();

                fOut.flush();
                fOut.close();
            } else {
                Log.e("File Creation Error", "File was not created.");
            }
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void importData(){
        DBHelper dbHelper = new DBHelper(this);
        String notesString = getTextFileData("notes.json");
        Log.i("Read Notes", notesString);

        JSONArray notesArray = null;
        try {
            notesArray = new JSONArray(notesString);
        } catch (JSONException je){
            Log.e("JSON Exception", je.getMessage());
        }

        for (int i = 0; i < notesArray.length(); i++) {
            JSONObject noteJson;
            boolean noteExists = false;
            try {
                noteJson = notesArray.getJSONObject(i);

                for(Note note : notes) {
                    Log.i("Note ID Note", note.getNoteId());
                    Log.i("Note ID JSON", noteJson.getString("noteId"));
                    if(note.getNoteId().equals(noteJson.getString("noteId"))){
                        noteExists = true;
                    }
                }

                if(!noteExists) {
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
    }

    public File getNotesStorageDir(Context context, String notesName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), notesName);
        if (!file.mkdirs()) {
            Log.e("External File Storage", "Directory not created");
        }
        return file;
    }

    public String getTextFileData(String fileName) {

        // Get the dir of SD Card
        File noteDir = getNotesStorageDir(this, "notedExport");

        // Get The Text file
        File txtFile = new File(noteDir, fileName);

        // Read the file Contents in a StringBuilder Object
        StringBuilder text = new StringBuilder();

        try {

            BufferedReader reader = new BufferedReader(new FileReader(txtFile));

            String line;

            while ((line = reader.readLine()) != null) {
                text.append(line + '\n');
            }
            reader.close();
        } catch (IOException e) {
            Log.e("Error Reading File", "Error occurred while reading text file!!");

        }

        return text.toString();

    }


}
