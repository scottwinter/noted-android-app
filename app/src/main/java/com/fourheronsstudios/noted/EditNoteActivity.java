package com.fourheronsstudios.noted;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.dto.Note;

public class EditNoteActivity extends AppCompatActivity {
    private TextView noteTitle;
    private TextView noteBody;

    private DBHelper dbHelper;
    private Note note;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        noteTitle = (EditText) findViewById(R.id.noteTitle);
        noteBody = (EditText) findViewById(R.id.noteBody);

        Intent intent = getIntent();
        int noteId = (Integer) intent.getExtras().get("noteId");

        dbHelper = new DBHelper(this);
        note = null;

        if (noteId != -1) {
            try {
                note = dbHelper.getSingleNote(noteId);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            noteTitle.setText(note.getTitle());
            noteBody.setText(note.getBody());
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void saveNote() {
        if (note == null) {
            note = new Note(noteTitle.getText().toString(), noteBody.getText().toString());
            try {
                dbHelper.createNewNote(note.getTitle(), note.getBody(), System.currentTimeMillis());
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            note.setBody(noteBody.getText().toString());
            note.setTitle(noteTitle.getText().toString());
            try {
                dbHelper.updateNote(note.getId(), note.getTitle(), note.getBody(), System.currentTimeMillis());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getApplicationContext(), "Note Saved", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveNote) {
            saveNote();
        }
        return super.onOptionsItemSelected(item);
    }
}
