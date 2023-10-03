package com.example.proyectoedia.menu.Chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.example.proyectoedia.menu.Buscador.ModeloUsuarios;
import com.example.proyectoedia.notificaciones.Data;
import com.example.proyectoedia.notificaciones.Sender;
import com.example.proyectoedia.notificaciones.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static com.squareup.picasso.Picasso.get;

public class ChatActivity extends AppCompatActivity {

    //Vistas
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView perfilIv;
    TextView nombreTv, estadoUsuarioTv;
    EditText mensajeEt;
    ImageButton enviarBtn;

    //Autentificacion de FireBase
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    //Comprobar si se ha visto el mensaje o no
    ValueEventListener vistoListener;
    DatabaseReference userRefVisto;

    List<ModeloChat> chatList;
    AdaptadorChat adaptadorChat;

    String idUsuario1;
    String idUsuario2;
    String imagenU2;

    //para la notificacion del mensaje
    private  RequestQueue requestQueue;

    private boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Inicializar las vistas

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        perfilIv = findViewById(R.id.perfilIv);
        nombreTv = findViewById(R.id.nameTv);
        estadoUsuarioTv = findViewById(R.id.estadoUsuarioTV);
        mensajeEt = findViewById(R.id.mensajeEt);
        enviarBtn = findViewById(R.id.enviarBtn);

        requestQueue = Volley.newRequestQueue(getApplicationContext());


        //El recycler

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //propiedades del recycler

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        //Intent para recuperar a través del id del usuario el resto de su informacion
        Intent intent = getIntent();
        idUsuario2 = intent.getStringExtra("idUsuario");

        //Inicializar el fireBase
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //----->>BUSCAR en la BBDD para la informacion del usuario<<<---//

        Query usuarioQuery = usersDbRef.orderByChild("uid").equalTo(idUsuario2); //--ID del Usuario con el que hablas

        usuarioQuery.addValueEventListener(new ValueEventListener() { //-->>obtener el nombre y la foto del usuario

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){

                    //Obtener los datos
                    String nombre =""+ ds.child("name").getValue(); //-- Recuperar de la bbdd(tiene que llamarse igual que las tablas)
                    imagenU2 =""+ ds.child("imagen").getValue();
                   String estadoEscribiendo = ""+ ds.child("escribiendoA").getValue();

                   //Para saber si el usuario está escribiendo

                    if(estadoEscribiendo.equals(idUsuario1)){
                        estadoUsuarioTv.setText("escribiendo...");
                    }else{
                        //Obtener el valor del estado (online o ultima conexion)
                        String estadoOnline = ""+ ds.child("estado").getValue();

                        if(estadoOnline.equals("online")){//--> le pasamos el estado online
                            estadoUsuarioTv.setText(estadoOnline);
                        }else { //--> y sino que nos devuelva la última conexion

                            //Convertir la fecha y hora en el formato Date
                            Calendar calendar = Calendar.getInstance(Locale.forLanguageTag("ES"));
                            try {
                                calendar.setTimeInMillis(Long.parseLong(estadoOnline));
                            }catch (Exception e){
                                e.printStackTrace();;
                            }
                            String dateTime = android.text.format.DateFormat.format(" dd/MM/yyyy hh:mm aa  ",calendar).toString();
                            estadoUsuarioTv.setText(" Última conexión: "+dateTime);
                        }
                    }


                    //Settearlos
                    nombreTv.setText(nombre);
                    try{
                        //La imagen que llega la ponemos en el imageview

                        Picasso.get().load(imagenU2).placeholder(R.drawable.icon_person).into(perfilIv);

                    }catch(Exception e){ //--> La excepcion pone la foto por defecto si no tiene
                        get().load(R.drawable.icon_person).into(perfilIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //OnClick para enviar el mensaje

        enviarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;

                //Obtener el texto desde el editor
                String mensaje = mensajeEt.getText().toString().trim();

                //Comprobar si el mensaje está vacío o no
                if(TextUtils.isEmpty(mensaje)){

                    Toast.makeText(ChatActivity.this, "No puedes enviar un mensaje vacío!", Toast.LENGTH_SHORT).show();

                }else {

                    enviarMensaje(mensaje);
                }
                //Resetear el editor de texto
                mensajeEt.setText("");
            }
        });

        //Listener para saber que el editor de texto está activo

        mensajeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() == 0){
                    estadoEscribiendo("nadie");
                } else{
                    estadoEscribiendo(idUsuario2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        leerMensaje();

        mensajeVisto();
    }

    private void mensajeVisto() {

        userRefVisto = FirebaseDatabase.getInstance().getReference("Chats");
        vistoListener = userRefVisto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for(DataSnapshot ds: datasnapshot.getChildren()){

                    ModeloChat chat = ds.getValue(ModeloChat.class);
                    if(chat.getRecibido().equals(idUsuario1) && chat.getEnviado().equals(idUsuario2)){

                        HashMap<String, Object> hasVistoHasgMap = new HashMap<>();
                        hasVistoHasgMap.put("isVisto",true); /// no se si es isVisto o Visto
                        ds.getRef().updateChildren(hasVistoHasgMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void leerMensaje() {

        chatList = new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                chatList.clear();

                for (DataSnapshot ds: datasnapshot.getChildren()){
                    ModeloChat chat = ds.getValue(ModeloChat.class);
                    if(chat.getRecibido().equals(idUsuario1) && chat.getEnviado().equals(idUsuario2) ||
                            chat.getRecibido().equals(idUsuario2) && chat.getEnviado().equals(idUsuario1)){

                        chatList.add(chat);
                    }

                    //Adaptador
                    adaptadorChat = new AdaptadorChat(ChatActivity.this, chatList, imagenU2);
                    adaptadorChat.notifyDataSetChanged();
                    // settear adaptador al recycler

                    recyclerView.setAdapter(adaptadorChat);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void enviarMensaje(final String mensaje) {

        String horadia = String.valueOf(System.currentTimeMillis());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("enviado",idUsuario1);
        hashMap.put("recibido", idUsuario2);
        hashMap.put("mensaje",mensaje);
        hashMap.put("horadia",horadia);
        hashMap.put("isVisto",false);

        databaseReference.child("Chats").push().setValue(hashMap);

        String msg = mensaje;

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(idUsuario1);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModeloUsuarios usuarios = snapshot.getValue(ModeloUsuarios.class);

                if(notify){
                    enviarNotificacion(idUsuario2,usuarios.getName(), mensaje);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Creamos en la base de datos el nodo para la lista del chat.
        final DatabaseReference chatListaRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(idUsuario1)
                .child(idUsuario2);
        chatListaRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatListaRef1.child("id").setValue(idUsuario2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatListaRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(idUsuario2)
                .child(idUsuario1);
        chatListaRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatListaRef2.child("id").setValue(idUsuario1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void enviarNotificacion(final String idUsuario2, final String name, final String mensaje) {

        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(idUsuario2);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(idUsuario1,name + ": "+ mensaje, "Nuevo mensaje", idUsuario2,R.drawable.icon_default);

                    Sender sender = new Sender(data,token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                            //Respuesta
                                        Log.d("JSON_RESPONSE","onResponse: " +response.toString());


                                    }
                                } ,new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.d("JSON_RESPONSE","onResponse: " +error.toString());

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                Map<String,String> headers = new HashMap<>();
                                headers.put("Content-type", "application/json");
                                //Nuestra clave de la bbdd de firebase, es la clave del servidor
                                headers.put("Authorization", "key=AAAABW5r2dI:APA91bFEhVKYCD-dTWE8D_MXNxJ-JorFGfMXqMY6eMs5KPguAst5-8BW1ttsxoeoXKUa2tufyLh76UW3rtGcPdU2mPWY26fIzm2LxyDJ7N8shwL-ukx8fmSYqs_xqpJs4AmPdUseGS3J");

                                return headers;
                            }
                        };

                        //añadir la respuesta de la consulta

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Verificar que el usuario existe:
    private void verificarUsuarios(){

        //--Obtener el usuario con fireBase:
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){ //-- Si el usuario está en la bbdd de FireBase:

            //mPerfilTv.setText(user.getEmail());

            idUsuario1 = user.getUid();//--> Usuario que está logueado en ese momento

        }else { //-- Sino, no está registrado en la app, vuelve a la pagina principal para que se registre

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void estadoOnline(String estado){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(idUsuario1);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("estado", estado);

        //Actualizar el estado del usuario
        dbRef.updateChildren(hashMap);
    }

    private void estadoEscribiendo(String escribiendo){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(idUsuario1);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("escribiendoA", escribiendo);

        //Actualizar el estado del usuario
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        verificarUsuarios();
        estadoOnline("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Saber la ultima conexion del usuario (dia y hora)
        String ultimaConexion = String.valueOf(System.currentTimeMillis());
        estadoOnline(ultimaConexion);
        //Estado: escribiendo...
        estadoEscribiendo("nadie");

        userRefVisto.removeEventListener(vistoListener);
    }

    @Override
    protected void onResume() {
        estadoOnline("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
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