package com.fourheronsstudios.noted;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fourheronsstudios.noted.database.DBHelper;
import com.fourheronsstudios.noted.dto.Note;

import java.util.ArrayList;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<Note> mDataset;
    private DBHelper dbHelper;

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView title;
        TextView description;
        TextView lastUpdated;
        int noteId;
        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            title = (TextView) itemView.findViewById(R.id.list_title);
            description = (TextView) itemView.findViewById(R.id.list_desc);
            lastUpdated = (TextView) itemView.findViewById(R.id.last_updated);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ReadNoteActivity.class);
                    intent.putExtra("noteId", noteId);
                    context.startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this note?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeAt(getAdapterPosition());
                                    dbHelper.deleteNote(noteId);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;
                }
            });
        }
    }

    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MyAdapter(ArrayList<Note> myDataset, DBHelper dbHelper) {
        this.mDataset = myDataset;
        this.dbHelper = dbHelper;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
       return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String newDescription = mDataset.get(position).getBody();

        Spanned descFormatted = Html.fromHtml(newDescription.replace("\n", " "));

        holder.title.setText(mDataset.get(position).getTitle());
        holder.description.setText(descFormatted);
        holder.noteId = mDataset.get(position).getId();

        long timeDiff = System.currentTimeMillis() - Long.parseLong(mDataset.get(position).getDate());
        long seconds = timeDiff/1000;
        long minutes = seconds/60;
        long hours = minutes/60;
        long days = hours/24;

        String lastUpdatedDisplay;

        if( seconds < 60 ) {
            lastUpdatedDisplay = seconds + "s";
        } else if ( minutes < 60 ) {
            lastUpdatedDisplay = minutes + "m";
        } else if ( hours < 24 ) {
            lastUpdatedDisplay = hours + "h";
        } else {
            lastUpdatedDisplay = days + "d";
        }

        holder.lastUpdated.setText(lastUpdatedDisplay);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
