package com.example.polivinc;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeamDetailsActivity extends AppCompatActivity {

    private ProgressBar groupProgressBar;
    private TextView groupProgressText, teamCodeTextView;
    private ListView memberListView;
    private Button deleteTeamButton;

    private DatabaseReference teamRef;
    private List<MemberProgress> memberProgressList;
    private MemberProgressAdapter adapter;

    private String teamCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_details);

        // Inicializar vistas
        groupProgressBar = findViewById(R.id.groupProgressBar);
        groupProgressText = findViewById(R.id.groupProgressText);
        teamCodeTextView = findViewById(R.id.teamCodeTextView);
        memberListView = findViewById(R.id.memberListView);
        deleteTeamButton = findViewById(R.id.deleteTeamButton);

        // Inicializar lista y adaptador
        memberProgressList = new ArrayList<>();
        adapter = new MemberProgressAdapter(this, memberProgressList);
        memberListView.setAdapter(adapter);

        // Obtener código del equipo desde el intent
        teamCode = getIntent().getStringExtra("teamCode");
        if (teamCode != null) {
            teamCodeTextView.setText("Código del equipo: " + teamCode);
            teamRef = FirebaseDatabase.getInstance().getReference("Teams").child(teamCode);
            loadTeamProgress();
        } else {
            Toast.makeText(this, "Código de equipo no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar listener para eliminar equipo
        deleteTeamButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadTeamProgress() {
        teamRef.child("progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalTasks = 0;
                int completedTasks = 0;
                memberProgressList.clear();

                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String memberId = memberSnapshot.getKey();

                    // Obtener datos del miembro
                    String memberName = memberSnapshot.child("memberName").getValue(String.class); // Campo correcto
                    Integer memberCompletedTasks = memberSnapshot.child("completedTasks").getValue(Integer.class);
                    Integer memberTotalTasks = memberSnapshot.child("totalTasks").getValue(Integer.class);

                    // Validar valores nulos
                    memberCompletedTasks = (memberCompletedTasks != null) ? memberCompletedTasks : 0;
                    memberTotalTasks = (memberTotalTasks != null) ? memberTotalTasks : 0;

                    totalTasks += memberTotalTasks;
                    completedTasks += memberCompletedTasks;

                    // Agregar datos a la lista
                    memberProgressList.add(new MemberProgress(
                            memberId,
                            memberName != null ? memberName : "Desconocido",
                            memberCompletedTasks,
                            memberTotalTasks
                    ));
                }

                // Notificar cambios al adaptador
                adapter.notifyDataSetChanged();
                updateGroupProgress(completedTasks, totalTasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeamDetailsActivity.this, "Error al cargar el progreso del equipo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupProgress(int completedTasks, int totalTasks) {
        if (totalTasks > 0) {
            int progress = (completedTasks * 100) / totalTasks;
            groupProgressBar.setProgress(progress);
            groupProgressText.setText("Progreso grupal: " + progress + "%");
        } else {
            groupProgressBar.setProgress(0);
            groupProgressText.setText("Progreso grupal: 0%");
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar equipo")
                .setMessage("¿Estás seguro de que deseas eliminar este equipo? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteTeam())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteTeam() {
        teamRef.removeValue((error, ref) -> {
            if (error == null) {
                Toast.makeText(TeamDetailsActivity.this, "Equipo eliminado con éxito", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(TeamDetailsActivity.this, "Error al eliminar el equipo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}