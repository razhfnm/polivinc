package com.example.polivinc;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.util.List;

public class GroupAdvancesActivity extends AppCompatActivity {

    private ProgressBar groupProgressBar;
    private TextView groupProgressText;
    private ListView memberProgressList;

    private DatabaseReference teamRef;
    private List<MemberProgress> memberProgressListData;
    private MemberProgressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_progress);

        // Inicializar vistas
        groupProgressBar = findViewById(R.id.groupProgressBar);
        groupProgressText = findViewById(R.id.groupProgressText);
        memberProgressList = findViewById(R.id.memberProgressList);

        // Inicializar lista y adaptador
        memberProgressListData = new ArrayList<>();
        adapter = new MemberProgressAdapter(this, memberProgressListData);
        memberProgressList.setAdapter(adapter);

        // Obtener el código del equipo desde el intent
        String teamCode = getIntent().getStringExtra("teamCode");
        if (teamCode != null) {
            teamRef = FirebaseDatabase.getInstance().getReference("Teams").child(teamCode);
            loadGroupProgress();
        } else {
            Toast.makeText(this, "Código de equipo no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGroupProgress() {
        teamRef.child("progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalTasks = 0;
                int completedTasks = 0;
                memberProgressListData.clear();

                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    // Obtener datos del miembro
                    String memberId = memberSnapshot.getKey();
                    String memberName = memberSnapshot.child("memberName").getValue(String.class); // Campo correcto
                    Integer memberCompletedTasks = memberSnapshot.child("completedTasks").getValue(Integer.class);
                    Integer memberTotalTasks = memberSnapshot.child("totalTasks").getValue(Integer.class);

                    // Validar valores nulos
                    memberCompletedTasks = (memberCompletedTasks != null) ? memberCompletedTasks : 0;
                    memberTotalTasks = (memberTotalTasks != null) ? memberTotalTasks : 0;

                    totalTasks += memberTotalTasks;
                    completedTasks += memberCompletedTasks;

                    // Agregar los datos a la lista
                    memberProgressListData.add(new MemberProgress(
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
                Toast.makeText(GroupAdvancesActivity.this, "Error al cargar el progreso grupal", Toast.LENGTH_SHORT).show();
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
}
