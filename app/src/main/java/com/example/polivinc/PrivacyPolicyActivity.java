package com.example.polivinc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private Button btnAccept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Inicializar el botón
        btnAccept = findViewById(R.id.btnAccept);

        // Configurar el botón para regresar a la actividad principal
        btnAccept.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyPolicyActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}