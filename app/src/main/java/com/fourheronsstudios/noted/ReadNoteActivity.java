package com.fourheronsstudios.noted;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.dto.Note;

public class ReadNoteActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_read_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReadNoteActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        noteTitle = (TextView) findViewById(R.id.noteTitleRead);
        noteBody = (TextView) findViewById(R.id.noteBodyRead);

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

        FloatingActionButton fab;
        fab = (FloatingActionButton) findViewById(R.id.fabEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
                intent.putExtra("noteId", note.getId());
                startActivity(intent);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void editNote() {
        Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
        intent.putExtra("noteId", note.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.read_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editNote) {
            editNote();
        } else if(item.getItemId() == R.id.deleteNote) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteNote(note.getId());
                            Intent intent = new Intent(ReadNoteActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
