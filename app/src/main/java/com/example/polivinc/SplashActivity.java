package com.example.polivinc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializa FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Verifica si hay un usuario autenticado
        if (currentUser == null) {
            // Si no hay usuario autenticado, redirige a MainActivity (pantalla de inicio de sesión)
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Si hay un usuario autenticado, consulta su rol en la base de datos
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("role");

            // Escucha el valor del rol en Firebase
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.getValue(String.class);
                        if (role != null) {
                            // Redirige según el rol del usuario
                            if (role.equals("Profesor")) {
                                // Redirigir a la actividad de profesor
                                Intent intent = new Intent(SplashActivity.this, ProfessorActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (role.equals("Estudiante")) {
                                // Redirigir a la actividad de estudiante
                                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Si el rol no es válido, redirige a la selección de rol
                                Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Si no hay rol definido, redirige a la selección de rol
                            Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Si no existe el nodo "role", redirige a la selección de rol
                        Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // En caso de error, redirige a la selección de rol y registra el error
                    Log.e("SplashActivity", "Error al leer el rol: " + error.getMessage());
                    Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}