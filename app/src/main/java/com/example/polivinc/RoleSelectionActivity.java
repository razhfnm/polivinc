package com.example.polivinc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoleSelectionActivity extends AppCompatActivity {

    private Button btnProfesor, btnEstudiante;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Inicializar FirebaseAuth y DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // Si no hay usuario autenticado, redirigir a la pantalla de inicio de sesión
            Intent intent = new Intent(RoleSelectionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        // Inicializar los botones
        btnProfesor = findViewById(R.id.btnProfesor);
        btnEstudiante = findViewById(R.id.btnEstudiante);

        // Manejar clics en los botones
        btnProfesor.setOnClickListener(v -> {
            // Guardar rol en Firebase y redirigir a ProfessorActivity
            userRef.child("role").setValue("Profesor").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(RoleSelectionActivity.this, ProfessorActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RoleSelectionActivity.this, "Error al guardar el rol", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnEstudiante.setOnClickListener(v -> {
            // Guardar rol en Firebase y redirigir a DashboardActivity
            userRef.child("role").setValue("Estudiante").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(RoleSelectionActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RoleSelectionActivity.this, "Error al guardar el rol", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}