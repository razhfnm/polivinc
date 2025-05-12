package com.example.polivinc;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailsTeamDialog extends AppCompatActivity {

    private TextView tvTeamName, tvTeamCode;
    private ListView lvTeamMembers;
    private Button btnClose;

    private DatabaseReference teamRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_team_details);

        // Inicializar vistas
        tvTeamName = findViewById(R.id.tvTeamName);
        tvTeamCode = findViewById(R.id.tvTeamCode);
        lvTeamMembers = findViewById(R.id.lvTeamMembers);
        btnClose = findViewById(R.id.btnClose);

        // Obtener datos pasados desde la actividad anterior
        String teamName = getIntent().getStringExtra("teamName");
        String teamCode = getIntent().getStringExtra("teamCode");

        if (teamName == null || teamCode == null) {
            Toast.makeText(this, "Error: No se recibieron detalles del equipo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar el nombre y el código del equipo
        tvTeamName.setText("Nombre: " + teamName);
        tvTeamCode.setText("Código: " + teamCode);

        // Inicializar referencia a Firebase
        teamRef = FirebaseDatabase.getInstance().getReference("Teams").child(teamCode).child("members");

        // Cargar miembros del equipo desde Firebase
        loadTeamMembers();

        // Configurar botón de cerrar
        btnClose.setOnClickListener(v -> finish());
    }

    /**
     * Cargar los miembros del equipo desde Firebase
     */
    private void loadTeamMembers() {
        ArrayList<String> teamMembers = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teamMembers);
        lvTeamMembers.setAdapter(adapter);

        teamRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teamMembers.clear();
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String memberName = memberSnapshot.getValue(String.class);
                    if (memberName != null) {
                        teamMembers.add(memberName);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsTeamDialog.this, "Error al cargar miembros del equipo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}