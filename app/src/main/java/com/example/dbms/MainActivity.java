package com.example.dbms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class MainActivity extends AppCompatActivity implements DatabaseAdapter.OnDeleteClickListener, DatabaseAdapter.OnDatabaseClickListener {

    private RecyclerView recyclerViewDatabases;
    private List<DatabaseCard> databaseList;
    private DatabaseAdapter databaseAdapter;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewDatabases = findViewById(R.id.recyclerViewDatabases);
        recyclerViewDatabases.setLayoutManager(new LinearLayoutManager(this));
        databaseList = new ArrayList<>();
        databaseAdapter = new DatabaseAdapter(databaseList, this, this); // Pass the activity itself as the click listener
        recyclerViewDatabases.setAdapter(databaseAdapter);

        FloatingActionButton fabAddDatabase = findViewById(R.id.fabAddDatabase);
        fabAddDatabase.setOnClickListener(v -> showAddDatabaseDialog());

        // Initialize Room database
        appDatabase = AppDatabase.getDatabase(this);

        // Observe changes in the database and update UI accordingly
        appDatabase.databaseDao().getAllDatabases().observe(this, new Observer<List<DatabaseEntity>>() {
            @Override
            public void onChanged(List<DatabaseEntity> databaseEntities) {
                databaseList.clear();
                for (DatabaseEntity entity : databaseEntities) {
                    databaseList.add(new DatabaseCard(entity.name, entity.createdDate.toString()));
                }
                databaseAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddDatabaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Database Name");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String databaseName = input.getText().toString().trim();
                if (!databaseName.isEmpty()) {
                    addDatabase(databaseName);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a valid database name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addDatabase(String databaseName) {
        // Add the database to Room
        DatabaseEntity databaseEntity = new DatabaseEntity();
        databaseEntity.name = databaseName;
        databaseEntity.createdDate = new Date();
        AppDatabase.databaseWriteExecutor.execute(() -> appDatabase.databaseDao().insert(databaseEntity));
    }

    @Override
    public void onDeleteClick(int position) {
        Log.d("DeleteClick", "Delete button clicked at position: " + position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Database");
        builder.setMessage("Are you sure you want to delete this database?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String databaseName = databaseList.get(position).getName();
                Log.d("DeleteDatabase", "Deleting database with name: " + databaseName);

                // Fetch the database entity from Room based on its name
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    DatabaseEntity databaseEntity = appDatabase.databaseDao().getDatabaseByName(databaseName);
                    if (databaseEntity != null) {
                        // Perform deletion if the entity exists
                        appDatabase.databaseDao().deleteDatabaseByName(databaseName);
                    } else {
                        Log.e("DeleteDatabase", "Database entity not found!");
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onDatabaseClick(String databaseName) {
        // Start TableActivity with the selected database name
        Intent intent = new Intent(MainActivity.this, TableActivity.class);
        intent.putExtra("DATABASE_NAME", databaseName);
        startActivity(intent);
    }
}
