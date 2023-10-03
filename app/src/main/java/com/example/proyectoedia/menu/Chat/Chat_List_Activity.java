package com.example.proyectoedia.menu.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.example.proyectoedia.menu.Buscador.ModeloUsuarios;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Chat_List_Activity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView_listaChat;
    List<ModeloChatList> chatList;
    List<ModeloUsuarios> usuariosList;
    DatabaseReference reference;
    FirebaseUser usuarioActual;
    AdaptadorChatList adaptadorChatList;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__list_);

        //inflater.inflate(R.layout.fragment_lista_chat, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
       // verificarUsuarios();

        recyclerView_listaChat = findViewById(R.id.recyclerView_lista_chat);
        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(usuarioActual.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloChatList chatList1 = ds.getValue(ModeloChatList.class);
                    chatList.add(chatList1);
                }
                cargarChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return  super.onSupportNavigateUp();
    }

    private void cargarChats() {
        usuariosList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuariosList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloUsuarios ususario = ds.getValue(ModeloUsuarios.class);
                    for(ModeloChatList chatlist: chatList){
                        if(ususario.getUid() != null && ususario.getUid().equals(chatlist.getId())){
                            usuariosList.add(ususario);
                            break;
                        }
                    }

                    adaptadorChatList = new AdaptadorChatList(Chat_List_Activity.this, usuariosList);

                    recyclerView_listaChat.setAdapter(adaptadorChatList);

                    for(int i=0; i<usuariosList.size(); i++){
                        ultimoMensaje(usuariosList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ultimoMensaje(final String userId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String elUltimoMensaje = "default";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloChat chat = ds.getValue(ModeloChat.class);
                    if(chat == null){
                        continue;
                    }
                    String remitente = chat.getEnviado();
                    String recibir = chat.getRecibido();
                    if(remitente == null || recibir == null){
                        continue;
                    }

                    if(chat.getRecibido().equals(usuarioActual.getUid()) && chat.getEnviado().equals(userId) ||
                            chat.getRecibido().equals(userId) && chat.getEnviado().equals(usuarioActual.getUid())){

                        elUltimoMensaje = chat.getMensaje();
                    }
                }
                adaptadorChatList.fijarUltimoMensaje(userId, elUltimoMensaje);
                adaptadorChatList.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    ////----- VERIFICAR QUE EL USUARIO EXISTE-----///
    private void verificarUsuarios(){

        //--Obtener el usuario con fireBase:
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){ //-- Si el usuario está en la bbdd de FireBase:
            //mPerfilTv.setText(user.getEmail());
            uid = user.getUid();

        }else { //-- Sino, no está registrado en la app, vuelve a la pagina principal para que se registre
            startActivity(new Intent(Chat_List_Activity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            verificarUsuarios();
        }

        return super.onOptionsItemSelected(item);
    }
}