package com.example.polivinc;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword); // Nuevo campo para confirmar contraseña
        btnRegister = findViewById(R.id.btnRegister);

        // Inicializar Firebase Auth y Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Configurar el botón de registro
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim(); // Obtener la confirmación de contraseña

        // Validar campos
        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es requerido");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo electrónico es requerido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es requerida");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Debe confirmar la contraseña");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Registrar en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Guardar datos adicionales en Firebase Database
                String userId = mAuth.getCurrentUser().getUid();
                User user = new User(name, email);

                databaseReference.child(userId).setValue(user).addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        finish(); // Cerrar la actividad de registro
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al guardar los datos: " + databaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Clase para el modelo de datos del usuario
    public static class User {
        public String memberName;
        public String email;

        public User() {
            // Constructor vacío requerido por Firebase
        }

        public User(String name, String email) {
            this.memberName = name;
            this.email = email;
        }
    }
}