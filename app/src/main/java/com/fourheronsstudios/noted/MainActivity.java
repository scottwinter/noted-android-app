package com.fourheronsstudios.noted;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.model.Note;
import com.fourheronsstudios.noted.utils.DatabaseUtil;
import com.fourheronsstudios.noted.utils.NotedUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes;
    private RecyclerView mRecyclerView;
    private NotedUtils utils;
    private static final int RC_SIGN_IN = 123;

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                Log.i("Firebase Auth", "User has been signed in.");
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.i("Firebase Auth", "Sign in canceled.");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.i("Firebase Auth", "No internet connection");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.i("Firebase Auth", "Unknown error.");
                    return;
                }
            }

            Log.i("Firebase Auth", "Unknown sign in response.");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Log.i("Firebase Auth", "current user UID is: " + currentUser.getUid());
            Log.i("Firebase Auth", "current user Display Name is: " + currentUser.getDisplayName());
            Log.i("Firebase Auth", "current user Provider Id is: " + currentUser.getEmail());
        } else {
            Log.i("Firebase Auth", "User is logged out.");
        }


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
            if(mAuth.getCurrentUser() != null) {
                Log.i("Info log", "Cloud syncing.");
                cloudBackup(this);
            } else {
                Toast.makeText(this, "You must sign in before backing up to the cloud",
                        Toast.LENGTH_LONG).show();
            }
        } else if (item.getItemId() == R.id.restore) {
            if(mAuth.getCurrentUser() != null) {
                Log.i("Info log", "Cloud syncing.");
                cloudRestore(this);
            } else {
                Toast.makeText(this, "You must sign in before restoring data the cloud",
                        Toast.LENGTH_LONG).show();
            }
        } else if (item.getItemId() == R.id.signout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                            finish();
                        }
                    });
            Log.i("Firebase Auth", "User has been logged out.");
        } else if (item.getItemId() == R.id.signin) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(new AuthUI.IdpConfig.Builder(
                                            AuthUI.GOOGLE_PROVIDER).build()))
                            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                            .build(),
                    RC_SIGN_IN);
        }
//        else if (item.getItemId() == R.id.importNotes) {
//            Log.i("Notes Import", "Importing Notes");
//            importData();
//        }
        return super.onOptionsItemSelected(item);
    }

    public void cloudRestore(Context context){
        final DBHelper dbHelper = new DBHelper(context);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String emailAddress = "unknown@unknown.com";
        if (currentUser != null && currentUser.getEmail() != null){
            emailAddress = currentUser.getEmail();
        }

        db.collection("users").document(emailAddress).collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            dbHelper.deleteAllNotes();
                            for (DocumentSnapshot document : task.getResult()) {
                                Note note = document.toObject(Note.class);
                                dbHelper.createNewNote(note.getNoteId(), note.getTitle(),
                                        note.getBody(), Long.valueOf(note.getDate()));
                            }
                            populateNoteList();
                        } else {
                            Log.d("Firestore Get Data", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void cloudBackup(Context context){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String emailAddress = "unknown@unknown.com";
        if (currentUser != null && currentUser.getEmail() != null){
            emailAddress = currentUser.getEmail();
        }

        CollectionReference noteRef =
                db.collection("users").document(emailAddress).collection("notes");

        // Backing up to Firestore Database
        Log.i("Firestore", "Backing up data to Firestore");
        DBHelper dbHelper = new DBHelper(context);
//        FirebaseDatabase databaseInstance = DatabaseUtil.getDatabase();
//        DatabaseReference database = databaseInstance.getReference();

        List<Note> allNotes = dbHelper.getAllNotes();

        for(Note note : allNotes){
//            noteRef.add(note);
            noteRef.document(note.getNoteId()).set(note);
            Log.i("Sync log", "Note ID: " + note.getNoteId());
        }
        // End Firestore Database
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