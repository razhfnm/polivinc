package com.example.polivinc;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class PersonalAdvancesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ListView taskListView;
    private EditText inputTask;
    private Button btnAddTask;

    private FirebaseAuth mAuth;
    private DatabaseReference personalTasksRef;
    private StorageReference storageRef;

    private List<String> taskList = new ArrayList<>();
    private List<Boolean> taskStatusList = new ArrayList<>();
    private TaskAdapter adapter;

    private static final int PICK_FILE_REQUEST = 1;
    private String selectedTaskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_advances);

        progressBar = findViewById(R.id.progressBar);
        taskListView = findViewById(R.id.taskListView);
        inputTask = findViewById(R.id.inputTask);
        btnAddTask = findViewById(R.id.btnAddTask);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            personalTasksRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("PersonalTasks");
            storageRef = FirebaseStorage.getInstance().getReference("Users").child(userId).child("TaskFiles");
        }

        // Inicializar el adaptador
        adapter = new TaskAdapter(this, taskList, taskStatusList, personalTasksRef, this::uploadFile);
        taskListView.setAdapter(adapter);

        // Cargar tareas desde Firebase
        loadTasks();

        // Agregar nueva tarea
        btnAddTask.setOnClickListener(v -> addTask());
    }

    private void loadTasks() {
        personalTasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                taskStatusList.clear();
                int completedTasks = 0;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String taskName = taskSnapshot.getKey();
                    Boolean isCompleted = taskSnapshot.child("isCompleted").getValue(Boolean.class);
                    boolean hasIncompleteSubtasks = hasIncompleteSubtasks(taskSnapshot);

                    taskList.add(taskName);
                    taskStatusList.add(isCompleted != null && isCompleted && !hasIncompleteSubtasks);

                    if (Boolean.TRUE.equals(isCompleted) && !hasIncompleteSubtasks) {
                        completedTasks++;
                    }
                }

                adapter.notifyDataSetChanged();
                updateProgressBar(completedTasks, taskList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalAdvancesActivity.this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasIncompleteSubtasks(DataSnapshot taskSnapshot) {
        for (DataSnapshot subtaskSnapshot : taskSnapshot.child("Subtasks").getChildren()) {
            Boolean subtaskCompleted = subtaskSnapshot.child("isCompleted").getValue(Boolean.class);
            if (subtaskCompleted == null || !subtaskCompleted) {
                return true;
            }
        }
        return false;
    }

    private void addTask() {
        String taskName = inputTask.getText().toString().trim();
        if (!taskName.isEmpty()) {
            personalTasksRef.child(taskName).child("isCompleted").setValue(false)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            inputTask.setText("");
                            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al agregar tarea", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "El nombre de la tarea no puede estar vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgressBar(int completedTasks, int totalTasks) {
        if (totalTasks > 0) {
            int progress = (completedTasks * 100) / totalTasks;
            progressBar.setProgress(progress);

            // Sincronizar progreso con el equipo
            syncProgressWithTeam(completedTasks, totalTasks);
        } else {
            progressBar.setProgress(0);
        }
    }

    private void syncProgressWithTeam(int completedTasks, int totalTasks) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);

        // Recupera el código del equipo y el nombre del usuario
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String teamCode = snapshot.child("teamCode").getValue(String.class);
                    String memberName = snapshot.child("memberName").getValue(String.class); // Recuperar el nombre

                    if (teamCode != null && memberName != null) {
                        DatabaseReference teamProgressRef = FirebaseDatabase.getInstance()
                                .getReference("Teams")
                                .child(teamCode)
                                .child("progress")
                                .child(userId);

                        // Sincroniza las tareas y el nombre
                        teamProgressRef.child("memberName").setValue(memberName);
                        teamProgressRef.child("completedTasks").setValue(completedTasks);
                        teamProgressRef.child("totalTasks").setValue(totalTasks);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalAdvancesActivity.this, "Error al sincronizar progreso con el equipo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para subir un archivo
    public void uploadFile(String taskName) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), PICK_FILE_REQUEST);

        // Guarda el nombre de la tarea seleccionada
        selectedTaskName = taskName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            if (selectedTaskName != null) {
                StorageReference fileRef = storageRef.child(selectedTaskName).child(fileUri.getLastPathSegment());
                fileRef.putFile(fileUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String fileUrl = uri.toString();
                            personalTasksRef.child(selectedTaskName).child("fileUrl").setValue(fileUrl);
                            Toast.makeText(this, "Archivo subido exitosamente", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(this, "Error al subir el archivo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
