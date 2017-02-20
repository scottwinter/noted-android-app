package com.fourheronsstudios.noted;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.dto.Note;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes;
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

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
        try {
            DBHelper dbHelper = new DBHelper(this);
            //dbHelper.resetDatabaseTable();
            notes = dbHelper.getAllNotes();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        mAdapter = new MyAdapter(notes);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.newNote) {
            Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
            intent.putExtra("noteId", -1);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
