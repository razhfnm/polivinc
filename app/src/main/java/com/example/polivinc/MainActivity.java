package com.example.polivinc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity { 

    private EditText etUsername, etPassword;
    private Button btnLogin, btnPrivacyPolicy, btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Verificar si ya hay un usuario autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser.getUid());
            return; // Evitar continuar cargando la interfaz de inicio de sesión
        }

        // Inicializar los botones de la interfaz
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy); // Botón de aviso de privacidad
        progressBar = findViewById(R.id.progressBar);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            // Redirigir a la actividad RegisterActivity
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Ocultar la barra de carga inicialmente
        progressBar.setVisibility(View.GONE);

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Mostrar la barra de carga y deshabilitar el botón
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);

                // Intentar iniciar sesión
                signInUser(email, password);
            }
        });

        // Configurar el botón de aviso de privacidad
        btnPrivacyPolicy.setOnClickListener(v -> {
            // Abrir la actividad que muestra el aviso de privacidad
            Intent intent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Inicia sesión con Firebase Authentication.
     *
     * @param email    Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Ocultar la barra de carga y habilitar el botón
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        } else {
                            Toast.makeText(MainActivity.this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Verifica el rol del usuario (Profesor o Estudiante) desde la base de datos.
     * Redirige a la actividad correspondiente según el rol.
     *
     * @param userId ID del usuario autenticado.
     */
    private void checkUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    if ("Profesor".equals(role)) {
                        // Redirigir a la actividad de profesor
                        navigateToActivity(ProfessorActivity.class);
                    } else if ("Estudiante".equals(role)) {
                        // Redirigir a la actividad de estudiante
                        navigateToActivity(DashboardActivity.class);
                    }
                } else {
                    // Si no hay rol guardado, mostrar RoleSelectionActivity
                    navigateToActivity(RoleSelectionActivity.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al verificar el rol del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navega a una actividad específica.
     *
     * @param targetActivity Actividad a la que se redirige.
     */
    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(MainActivity.this, targetActivity);
        startActivity(intent);
        finish(); // Finaliza la actividad actual para evitar volver atrás
    }
}
