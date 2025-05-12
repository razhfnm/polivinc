package com.example.polivinc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SubtasksActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ListView subtaskListView;
    private EditText inputSubtask;
    private Button btnAddSubtask;

    private DatabaseReference subtasksRef;
    private List<String> subtaskList = new ArrayList<>();
    private List<Boolean> subtaskStatusList = new ArrayList<>();
    private SubtaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtasks);

        progressBar = findViewById(R.id.subtaskProgressBar);
        subtaskListView = findViewById(R.id.subtaskListView);
        inputSubtask = findViewById(R.id.inputSubtask);
        btnAddSubtask = findViewById(R.id.btnAddSubtask);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String taskName = getIntent().getStringExtra("taskName");

            subtasksRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("PersonalTasks")
                    .child(taskName)
                    .child("Subtasks");
        }

        // Inicializar el adaptador
        adapter = new SubtaskAdapter(this, subtaskList, subtaskStatusList, subtasksRef);
        subtaskListView.setAdapter(adapter);

        // Cargar subtareas desde Firebase
        loadSubtasks();

        // Agregar una nueva subtarea
        btnAddSubtask.setOnClickListener(v -> addSubtask());
    }

    private void loadSubtasks() {
        subtasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subtaskList.clear();
                subtaskStatusList.clear();
                int completedSubtasks = 0;

                for (DataSnapshot subtaskSnapshot : snapshot.getChildren()) {
                    String subtaskName = subtaskSnapshot.getKey();
                    Boolean isCompleted = subtaskSnapshot.child("isCompleted").getValue(Boolean.class);

                    subtaskList.add(subtaskName);
                    subtaskStatusList.add(isCompleted != null && isCompleted);

                    if (Boolean.TRUE.equals(isCompleted)) {
                        completedSubtasks++;
                    }
                }

                adapter.notifyDataSetChanged();
                updateProgressBar(completedSubtasks, subtaskList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubtasksActivity.this, "Error al cargar subtareas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSubtask() {
        String subtaskName = inputSubtask.getText().toString().trim();
        if (!subtaskName.isEmpty()) {
            subtasksRef.child(subtaskName).child("isCompleted").setValue(false)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            inputSubtask.setText("");
                            Toast.makeText(this, "Subtarea agregada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al agregar subtarea", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "El nombre de la subtarea no puede estar vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgressBar(int completedSubtasks, int totalSubtasks) {
        if (totalSubtasks > 0) {
            int progress = (completedSubtasks * 100) / totalSubtasks;
            progressBar.setProgress(progress);
        } else {
            progressBar.setProgress(0);
        }
    }
}
