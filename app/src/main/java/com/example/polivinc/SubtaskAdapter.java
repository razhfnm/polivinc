package com.example.polivinc;

import android.app.AlertDialog;
import android.content.Context;
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

public class SubtaskAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> subtaskList;
    private final List<Boolean> subtaskStatusList;
    private final DatabaseReference subtasksRef;

    public SubtaskAdapter(Context context, List<String> subtaskList, List<Boolean> subtaskStatusList, DatabaseReference subtasksRef) {
        super(context, R.layout.subtask_item, subtaskList);
        this.context = context;
        this.subtaskList = subtaskList;
        this.subtaskStatusList = subtaskStatusList;
        this.subtasksRef = subtasksRef;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.subtask_item, parent, false);
        }

        TextView subtaskName = convertView.findViewById(R.id.subtaskName);
        CheckBox subtaskCheckBox = convertView.findViewById(R.id.subtaskCheckBox);
        ImageButton btnEditSubtask = convertView.findViewById(R.id.btnEditSubtask);
        ImageButton btnDeleteSubtask = convertView.findViewById(R.id.btnDeleteSubtask);

        String subtask = subtaskList.get(position);
        Boolean isCompleted = subtaskStatusList.get(position);

        subtaskName.setText(subtask);
        subtaskCheckBox.setChecked(isCompleted);

        // Marcar subtarea como completada
        subtaskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            subtasksRef.child(subtask).child("isCompleted").setValue(isChecked);
        });

        // Editar subtarea
        btnEditSubtask.setOnClickListener(v -> showEditDialog(subtask));

        // Eliminar subtarea
        btnDeleteSubtask.setOnClickListener(v -> subtasksRef.child(subtask).removeValue());

        return convertView;
    }

    private void showEditDialog(String subtask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar subtarea");

        final TextView input = new TextView(context);
        input.setText(subtask);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newSubtaskName = input.getText().toString().trim();
            if (!newSubtaskName.isEmpty()) {
                subtasksRef.child(subtask).removeValue((error, ref) -> {
                    if (error == null) {
                        subtasksRef.child(newSubtaskName).child("isCompleted").setValue(false);
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
