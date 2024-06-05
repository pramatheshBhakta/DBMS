package com.example.dbms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DatabaseAdapter extends RecyclerView.Adapter<DatabaseAdapter.DatabaseViewHolder> {

    private List<DatabaseCard> databaseList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnDatabaseClickListener onDatabaseClickListener;

    private OnDeleteClickListener deleteClickListener;
    private OnDatabaseClickListener databaseClickListener;

    public DatabaseAdapter(List<DatabaseCard> databaseList, OnDeleteClickListener onDeleteClickListener) {
        this.databaseList = databaseList;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onDatabaseClickListener = onDatabaseClickListener;
    }
    public DatabaseAdapter(List<DatabaseCard> databaseList, OnDeleteClickListener deleteClickListener, OnDatabaseClickListener databaseClickListener) {
        this.databaseList = databaseList;
        this.deleteClickListener = deleteClickListener;
        this.databaseClickListener = databaseClickListener;
    }

    public interface OnDatabaseClickListener {
        void onDatabaseClick(String databaseName);
    }
    @NonNull
    @Override
    public DatabaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_database_card, parent, false);
        return new DatabaseViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DatabaseViewHolder holder, int position) {
        DatabaseCard databaseCard = databaseList.get(position);
        holder.textViewDatabaseName.setText(databaseCard.getName());
        holder.textViewDatabaseDate.setText(databaseCard.getDate());

        // Set click listener for delete button
        holder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(position);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (databaseClickListener != null) {
                    // Get the database name and pass it to the listener
                    String databaseName = databaseList.get(position).getName();
                    databaseClickListener.onDatabaseClick(databaseName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return databaseList.size();
    }

    public static class DatabaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDatabaseName;
        TextView textViewDatabaseDate;
        ImageButton imageButtonDelete;

        public DatabaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDatabaseName = itemView.findViewById(R.id.textViewDatabaseName);
            textViewDatabaseDate = itemView.findViewById(R.id.textViewDatabaseDate);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
        }
    }

    interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
