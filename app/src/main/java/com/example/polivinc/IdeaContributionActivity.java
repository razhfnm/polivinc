package com.example.polivinc;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class IdeaContributionActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private DatabaseReference messagesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_contribution);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // Referencia a la base de datos de Firebase
        messagesDatabase = FirebaseDatabase.getInstance().getReference("chatMessages");

        // Cargar mensajes existentes
        loadMessages();

        // Enviar mensaje
        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadMessages() {
        messagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar errores
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String userName = "Usuario"; // Cambia esto con el nombre real del usuario
            long timestamp = System.currentTimeMillis();

            Message message = new Message(userName, messageText, timestamp);
            messagesDatabase.push().setValue(message);

            editTextMessage.setText("");
        }
    }
}