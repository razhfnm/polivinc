package com.example.polivinc;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

// ... [paquete e imports sin cambios]

public class ProfessorActivity extends AppCompatActivity {

    private ListView lvTeams;
    private FloatingActionButton fabAddTeam;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        lvTeams = findViewById(R.id.lvTeams);
        fabAddTeam = findViewById(R.id.fabAddTeam);

        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("teams");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadTeams();

        fabAddTeam.setOnClickListener(view -> showCreateTeamDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_professor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            navigateToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadTeams() {
        ArrayList<String> teams = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teams);
        lvTeams.setAdapter(adapter);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teams.clear();
                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    String teamCode = teamSnapshot.getKey();
                    FirebaseDatabase.getInstance().getReference("Teams").child(teamCode)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot teamSnap) {
                                    Team team = teamSnap.getValue(Team.class);
                                    if (team != null) {
                                        String display = team.name + " - Profesores: " + String.join(", ", team.professors);
                                        teams.add(display);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ProfessorActivity.this, "Error al cargar equipos.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfessorActivity.this, "Error al leer los equipos.", Toast.LENGTH_SHORT).show();
            }
        });

        lvTeams.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDisplay = (String) parent.getItemAtPosition(position);
            String selectedName = selectedDisplay.split(" - ")[0];
            findTeamCodeByName(selectedName, this::showTeamOptionsDialog);
        });
    }

    private void findTeamCodeByName(String teamName, OnTeamCodeFound callback) {
        FirebaseDatabase.getInstance().getReference("Teams")
                .orderByChild("name")
                .equalTo(teamName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            callback.onFound(child.getKey());
                            return;
                        }
                        Toast.makeText(ProfessorActivity.this, "Equipo no encontrado.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfessorActivity.this, "Error al buscar equipo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTeamOptionsDialog(String teamCode) {
        FirebaseDatabase.getInstance().getReference("Teams").child(teamCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Team team = snapshot.getValue(Team.class);
                        if (team == null) {
                            Toast.makeText(ProfessorActivity.this, "Equipo no encontrado.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfessorActivity.this);
                        builder.setTitle("Opciones de equipo: " + team.name);

                        builder.setItems(new String[]{"Ver equipo", "Unirse a equipo", "Eliminar equipo", "Editar profesores"}, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent intent = new Intent(ProfessorActivity.this, TeamDetailsActivity.class);
                                    intent.putExtra("teamCode", teamCode);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    Toast.makeText(ProfessorActivity.this, "Unido al equipo: " + team.name, Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    confirmDeleteTeam(teamCode, team.name);
                                    break;
                                case 3:
                                    showUpdateProfessorsDialog(team);
                                    break;
                            }
                        });

                        builder.create().show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfessorActivity.this, "Error al obtener detalles del equipo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteTeam(String teamCode, String teamName) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar equipo")
                .setMessage("¿Estás seguro de que deseas eliminar el equipo '" + teamName + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteTeamFromFirebase(teamCode, teamName))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteTeamFromFirebase(String teamCode, String teamName) {
        FirebaseDatabase.getInstance().getReference("Teams").child(teamCode)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRef.child(teamCode).removeValue();
                        Toast.makeText(this, "Equipo '" + teamName + "' eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al eliminar equipo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateTeamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear equipo");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_team, null);
        EditText etTeamName = dialogView.findViewById(R.id.etTeamName);
        EditText etProfessors = dialogView.findViewById(R.id.etProfessors);
        TextView tvGeneratedCode = dialogView.findViewById(R.id.tvGeneratedCode);

        String generatedCode = generateTeamCode();
        tvGeneratedCode.setText("Código del equipo: " + generatedCode);

        builder.setView(dialogView);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String name = etTeamName.getText().toString().trim();
            String profs = etProfessors.getText().toString().trim();

            if (name.isEmpty() || profs.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> professorList = new ArrayList<>(Arrays.asList(profs.split(",")));
            professorList.replaceAll(String::trim);

            createTeamWithProfessors(name, generatedCode, professorList);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void createTeamWithProfessors(String name, String code, ArrayList<String> professors) {
        Team team = new Team(name, code);
        team.professors.addAll(professors);
        saveTeamToFirebase(team);
    }

    private void saveTeamToFirebase(Team team) {
        FirebaseDatabase.getInstance().getReference("Teams").child(team.code)
                .setValue(team)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRef.child(team.code).setValue(team.name);
                        Toast.makeText(this, "Equipo creado con éxito.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al guardar equipo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateTeamCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    private void showUpdateProfessorsDialog(Team team) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar profesores");

        final EditText input = new EditText(this);
        input.setText(String.join(", ", team.professors));
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String inputText = input.getText().toString().trim();
            if (!inputText.isEmpty()) {
                ArrayList<String> updatedList = new ArrayList<>(Arrays.asList(inputText.split(",")));
                updatedList.replaceAll(String::trim);
                team.professors = updatedList;

                FirebaseDatabase.getInstance().getReference("Teams").child(team.code)
                        .setValue(team)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Profesores actualizados.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error al guardar profesores.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    public static class Team {
        public String name;
        public String code;
        public ArrayList<String> professors = new ArrayList<>();

        public Team() {}

        public Team(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    private interface OnTeamCodeFound {
        void onFound(String teamCode);
    }
}
