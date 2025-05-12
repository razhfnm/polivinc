package com.example.polivinc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> taskList;
    private final List<Boolean> taskStatusList;
    private final DatabaseReference personalTasksRef;
    private final UploadFileCallback uploadFileCallback; // Callback para subir archivos

    // Constructor actualizado
    public TaskAdapter(Context context, List<String> taskList, List<Boolean> taskStatusList, DatabaseReference personalTasksRef, UploadFileCallback uploadFileCallback) {
        super(context, R.layout.task_item, taskList);
        this.context = context;
        this.taskList = taskList;
        this.taskStatusList = taskStatusList;
        this.personalTasksRef = personalTasksRef;
        this.uploadFileCallback = uploadFileCallback;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView taskName = convertView.findViewById(R.id.taskName);
        CheckBox taskCheckBox = convertView.findViewById(R.id.taskCheckBox);
        ImageButton btnEditTask = convertView.findViewById(R.id.btnEditTask);
        ImageButton btnDeleteTask = convertView.findViewById(R.id.btnDeleteTask);
        ImageButton btnSubtasks = convertView.findViewById(R.id.btnSubtasks);
        ImageButton btnUploadFile = convertView.findViewById(R.id.btnUploadFile); // Botón para subir archivos

        String task = taskList.get(position);
        Boolean isCompleted = taskStatusList.get(position);

        taskName.setText(task);
        taskCheckBox.setChecked(isCompleted);

        // Actualizar estado de la tarea
        taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            personalTasksRef.child(task).child("isCompleted").setValue(isChecked);
        });

        // Editar tarea
        btnEditTask.setOnClickListener(v -> showEditDialog(task));

        // Eliminar tarea
        btnDeleteTask.setOnClickListener(v -> personalTasksRef.child(task).removeValue());

        // Manejar subtareas
        btnSubtasks.setOnClickListener(v -> {
            Intent intent = new Intent(context, SubtasksActivity.class);
            intent.putExtra("taskName", task);
            context.startActivity(intent);
        });

        // Subir archivo
        btnUploadFile.setOnClickListener(v -> {
            if (uploadFileCallback != null) {
                uploadFileCallback.uploadFile(task); // Llamar al callback
            } else {
                Toast.makeText(context, "Callback no definido", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    private void showEditDialog(String task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar tarea");

        final TextView input = new TextView(context);
        input.setText(task);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newTaskName = input.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                personalTasksRef.child(task).removeValue((error, ref) -> {
                    if (error == null) {
                        personalTasksRef.child(newTaskName).child("isCompleted").setValue(false);
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Interfaz para el callback de subida de archivos
    public interface UploadFileCallback {
        void uploadFile(String taskName);
    }
}


