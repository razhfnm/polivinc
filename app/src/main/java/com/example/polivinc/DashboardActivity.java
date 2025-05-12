package com.example.polivinc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.app.AlertDialog;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView teamView;
    private View btnGroupAdvances, btnPersonalAdvances, btnIdeaContribution;
    private DatabaseReference teamRef;
    private String currentTeamCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabAddTeam = findViewById(R.id.fab_add_team);
        fabAddTeam.setOnClickListener(this::showTeamOptionsMenu);

        teamView = findViewById(R.id.teamView);
        btnGroupAdvances = findViewById(R.id.btnGroupAdvances);
        btnPersonalAdvances = findViewById(R.id.btnPersonalAdvances);
        btnIdeaContribution = findViewById(R.id.btnIdeaContribution);

        mAuth = FirebaseAuth.getInstance();

        loadTeamInfo();

        // Configurar listeners para botones de avances
        btnGroupAdvances.setOnClickListener(v -> {
            if (currentTeamCode != null) {
                Intent intent = new Intent(DashboardActivity.this, GroupAdvancesActivity.class);
                intent.putExtra("teamCode", currentTeamCode);
                startActivity(intent);
            } else {
                Toast.makeText(DashboardActivity.this, "No se encontró el equipo", Toast.LENGTH_SHORT).show();
            }
        });
        btnPersonalAdvances.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, PersonalAdvancesActivity.class)));
        btnIdeaContribution.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, IdeaContributionActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_infographic) {
            navigateToInfographic();
            return true;
        } else if (id == R.id.action_leave_team) {
            leaveTeam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTeamOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_team_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_join_team) {
                joinTeam();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void loadTeamInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("teamCode")) {
                        String teamCode = snapshot.child("teamCode").getValue(String.class);
                        displayTeam(teamCode);
                    } else {
                        showAdvancesSection(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DashboardActivity.this, "Error al cargar el equipo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void joinTeam() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unirse a un equipo");

        EditText input = new EditText(this);
        input.setHint("Ingresa el código del equipo");
        builder.setView(input);

        builder.setPositiveButton("Unirse", (dialog, which) -> {
            String teamCode = input.getText().toString().trim();
            if (!teamCode.isEmpty()) {
                validateAndJoinTeam(teamCode);
            } else {
                Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void validateAndJoinTeam(String teamCode) {
        DatabaseReference teamRef = FirebaseDatabase.getInstance().getReference("Teams").child(teamCode);
        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                        userRef.child("teamCode").setValue(teamCode);
                        teamRef.child("members").child(userId).setValue(true);

                        displayTeam(teamCode);
                        Toast.makeText(DashboardActivity.this, "Te has unido al equipo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "El equipo no existe. Verifica el código.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Error al validar el equipo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveTeam() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentTeamCode != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            teamRef.child("members").child(userId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userRef.child("teamCode").removeValue();
                    currentTeamCode = null;

                    Toast.makeText(DashboardActivity.this, "Has salido del equipo", Toast.LENGTH_SHORT).show();
                    teamView.setText("Equipo no creado");
                    showAdvancesSection(false);
                } else {
                    Toast.makeText(DashboardActivity.this, "Error al salir del equipo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayTeam(String teamCode) {
        currentTeamCode = teamCode;
        teamView.setText("Código del equipo: " + teamCode);
        teamRef = FirebaseDatabase.getInstance().getReference("Teams").child(teamCode);
        showAdvancesSection(true);
    }

    private void showAdvancesSection(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        btnGroupAdvances.setVisibility(visibility);
        btnPersonalAdvances.setVisibility(visibility);
        btnIdeaContribution.setVisibility(visibility);
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(DashboardActivity.this, MainActivity.class));
        finish();
    }

    private void navigateToInfographic() {
        String url = "https://www.ipn.mx/dems/concursos-academicos/paula.html";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}