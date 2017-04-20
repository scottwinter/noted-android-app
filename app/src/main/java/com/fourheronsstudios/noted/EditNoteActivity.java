package com.fourheronsstudios.noted;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_check_white_24dp);
        noteTitle = (TextView) findViewById(R.id.noteTitle);
        noteBody = (TextView) findViewById(R.id.noteBody);

        Intent intent = getIntent();
        final int noteId = (Integer) intent.getExtras().get("noteId");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

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
            noteBody.requestFocus();
        } else {
            noteTitle.requestFocus();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void saveNote() {
        Intent intent;
        String toastMessage;
        if(!noteTitle.getText().toString().equals("") || !noteBody.getText().toString().equals("")) {
            if (note == null) {
                note = new Note(noteTitle.getText().toString(), noteBody.getText().toString());
                try {
                    long id = dbHelper.createNewNote(note.getTitle(), note.getBody(), System.currentTimeMillis());
                    note.setId((int) id);
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
            intent = new Intent(EditNoteActivity.this, ReadNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            toastMessage = "Note Saved.";
        } else {
            intent = new Intent(EditNoteActivity.this, MainActivity.class);
            intent.putExtra("noteId", -1);
            toastMessage = "Nothing to save.";
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveNote) {
            saveNote();
        } else if (item.getItemId() == R.id.cancelEdit) {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Edit")
                    .setMessage("Are you sure you want to cancel editing?  All changes will be lost.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(note == null) {
                                Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(EditNoteActivity.this, ReadNoteActivity.class);
                                intent.putExtra("noteId", note.getId());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
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
