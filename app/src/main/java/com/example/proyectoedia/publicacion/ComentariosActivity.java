package com.example.proyectoedia.publicacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ComentariosActivity extends AppCompatActivity {

    //Para los detalles de usuario y del post:
    String  suUid, miUid, miNombre, miDp,postId, suDp,suNombre, pImagen, cId;


    boolean mComentarioEnProcesso = false;
    boolean mProcesoLikes = false;

    //Barra de progreso
    ProgressDialog pd;

    //Vistas

    ImageView uFotoIv, pImagenIv;
    TextView uNombreTv, pTiempov, pTituloTv, pDescripcionTv, pLikesTv, pComentariosTv;
    ImageButton botonMas;
    Button likeBoton;
    LinearLayout perfilLayout;
    RecyclerView recyclerView;

    List<ModeloComentarios> comentariosList;
    AdaptadorComentarios adaptadorComentarios;

    //Vistas comentarios

    EditText comentarioEt;
    ImageButton enviarBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_post);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("COMENTARIOS");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //obtener el id de usuario
        Intent intent = getIntent();

        postId = intent.getStringExtra("postId");

        //Inicializar las vistas

        uFotoIv = findViewById(R.id.uImagenIv);
        pImagenIv = findViewById(R.id.pImagenIv);
        uNombreTv = findViewById(R.id.uNameTv);
        pTiempov = findViewById(R.id.pTiempoTv);
        pTituloTv = findViewById(R.id.pTituloTv);
        pDescripcionTv = findViewById(R.id.pDescripcionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pComentariosTv = findViewById(R.id.pComentarioTv);
        botonMas = findViewById(R.id.opcionesBtn);
        likeBoton = findViewById(R.id.likeBtn);
        perfilLayout = findViewById(R.id.perfilLayout);
        recyclerView = findViewById(R.id.recyclerView_lista_comentarios);

        comentarioEt = findViewById(R.id.comentarioEt);
        enviarBtn = findViewById(R.id.enviarBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);


        cargarInfoPost();
        estadoUsuario();
        cargaInfoUsuario();
        actionBar.setSubtitle("Comenta qué te ha parecido el post!");

        cargarComentarios();

        enviarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comentarioPost();
            }
        });


        botonMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarMasOpciones();
            }
        });
    }

    private  void añadirNotificaciones(String hisUid, String pId, String notificacion){
        String timestamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notificacion", notificacion);
        hashMap.put("sUid", miUid);
      /*  hashMap.put("sName", );
        hashMap.put("sEmail", );
        hashMap.put("sImage", );*/

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notificaciones").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void cargarComentarios() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        //inicializar la lista de comentarios
        comentariosList = new ArrayList<>();

       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comentarios");

       ref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               comentariosList.clear();
               for (DataSnapshot ds: snapshot.getChildren()){

                   ModeloComentarios modeloComentarios = ds.getValue(ModeloComentarios.class);

                   comentariosList.add(modeloComentarios);

                   adaptadorComentarios = new AdaptadorComentarios(getApplicationContext(), comentariosList);
                   recyclerView.setAdapter(adaptadorComentarios);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }

    private void MostrarMasOpciones() {
        final PopupMenu popupMenu = new PopupMenu(this, botonMas, Gravity.END);

        if(suUid.equals(miUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Borrar");
            //popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int id = menuItem.getItemId();

                if(id == 0){
                    //Si pulsa el boton se elimina.
                    comenzarBorrar();

                }else  if(id == 1){

                    Intent intent = new Intent(ComentariosActivity.this, ComentariosActivity.class);
                    intent.putExtra("postId", postId); //-- para tener los detalles de los post
                    startActivity(intent);
                }


                return false;
            }
        });

        popupMenu.show();
    }

    private void comenzarBorrar() {
        //El post puede ser con o sin imagen.
        if(pImagen.equals("noImage")){
            borrarSinImagen();
        }else{
            borrarConImagen();
        }
    }

    private void borrarConImagen() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Borrando");

        StorageReference imagenRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImagen);
        imagenRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Se borra la imagen, y luego de la base de datos.
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(ComentariosActivity.this, "Borrado correctamente", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(ComentariosActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void borrarSinImagen() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Borrando");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(ComentariosActivity.this, "Borrado correctamente", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


  /*  private void setLikes() {


        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(postId).hasChild(miUid)){
                    likeBoton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.like_color, 0, 0, 0);
                    likeBoton.setText("");

                }else {
                    likeBoton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
                    likeBoton.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }/*

    /*private void likePost() {

        mProcesoLikes = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(mProcesoLikes){
                    //Quitamos el like, que ya estaba.
                    if(dataSnapshot.child(postId).hasChild(miUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(miUid).removeValue();

                        mProcesoLikes = false;

                    }else {
                        //Añadimos un like.
                        pLikes = "0";
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(miUid).setValue("Liked");

                        mProcesoLikes = false;


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }*/

    private void comentarioPost() {

        pd = new ProgressDialog(this);
        pd.setMessage("Añadiendo comentario...");

        //añadir los datos al edit text del comentario

        String comentario = comentarioEt.getText().toString().trim();

        if(TextUtils.isEmpty(comentario)){
            Toast.makeText(this, "Ya no puedes escribir más...", Toast.LENGTH_SHORT).show();
            return;

        }

        String fechayHora = String .valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comentarios");

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("cId",fechayHora);
        hashMap.put("horadia",fechayHora);
        hashMap.put("uid",miUid);
        hashMap.put("uDp",miDp);
        hashMap.put("pComentario",comentario);
        hashMap.put("uNombre",miNombre);

        //Guardarlo en la bbdd

        ref.child(fechayHora).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //añadido
                        pd.dismiss();
                        Toast.makeText(ComentariosActivity.this, "Comentario añadido!", Toast.LENGTH_SHORT).show();
                        comentarioEt.setText("");
                        contadorComentarios();

                        añadirNotificaciones(""+suUid, ""+postId, "Han comentado en tu post");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //algo ha fallado y no se ha añadido
                        pd.dismiss();
                        Toast.makeText(ComentariosActivity.this, "Algo salió mal: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }



    private void contadorComentarios() {

        mComentarioEnProcesso = true;
        final DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mComentarioEnProcesso){
                    String comentarios = ""+ snapshot.child("pComentarios").getValue();
                    int nuevoComentario = Integer.parseInt(comentarios) +1;

                    ref.child("pComentarios").setValue(""+nuevoComentario);
                    mComentarioEnProcesso = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void cargaInfoUsuario() {
        final Query miRef = FirebaseDatabase.getInstance().getReference("Users");
        miRef.orderByChild("uid").equalTo(miUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()) {
                    //recuperar datos
                    miNombre = ""+ds.child("name").getValue();
                    miDp = ""+ds.child("imagen").getValue();

                    try{
                        Picasso.get().load(miDp).placeholder(R.drawable.icon_person).into(cAvatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.icon_person).into(cAvatarIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cargarInfoPost() {
        //obtener el post con su Id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //recuperar datos
                    String pTitulo = ""+ds.child("pTitulo").getValue();
                    String pDescripcion = ""+ds.child("pDescripcion").getValue();
                   String pLikes = ""+ds.child("pLikes").getValue();
                    String pTime = ""+ds.child("pTime").getValue();
                    pImagen = ""+ds.child("pImagen").getValue();
                    suDp = ""+ds.child("uDp").getValue();
                    suUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    suNombre = ""+ds.child("uName").getValue();
                    String contadorComentario = ""+ds.child("pComentarios").getValue();


                    //Convertimos el tiempo a la fecha actual.
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTime));
                    String pTiempo = DateFormat.format("dd/MM/yyyy", calendar).toString();

                    pTituloTv.setText(pTitulo);
                    pDescripcionTv.setText(pDescripcion);
                    pTiempov.setText(pTiempo);
                    pComentariosTv.setText(contadorComentario + " Comentarios");

                    uNombreTv.setText(suNombre);

                    //Poner la imagen de los usuarios que han subido un post
                    //Si no hay imagen, entonces ocualtar ImageView.
                    if(pImagen.equals("noImagen")){
                        //Ocultar ImageView
                        pImagenIv.setVisibility(View.GONE);
                    }else{

                        //Mostrat ImageView
                        pImagenIv.setVisibility(View.VISIBLE);

                        try{
                            Picasso.get().load(pImagen).into(pImagenIv);
                        }catch (Exception e){

                        }
                    }

                    //Poner la imagen del usuario en el comentario
                    try {
                        Picasso.get().load(suDp).placeholder(R.drawable.icon_person).into(uFotoIv);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.icon_person).into(uFotoIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void estadoUsuario(){
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null ){
            //el usuario que está dentro de la app
            miUid = usuario.getUid();


        }else {
            //el usuario que NO está dentro de la app
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void guardarComentario(String guardar){

        final DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pComentarios", guardar);

        //Actualizar el estado del usuario
        ref.updateChildren(hashMap);
    }

    /*private void guardarLike(String guardar){
        pLikes = "0";
        final DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Posts").child(pLikes);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pLikes", guardar);

        //Actualizar el estado del usuario
        ref.updateChildren(hashMap);
    }*/

    @Override
    protected void onStart() {
        estadoUsuario();

        super.onStart();
    }

    @Override
    protected void onResume() {

        estadoUsuario();
        super.onResume();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            estadoUsuario();
        }
        return super.onOptionsItemSelected(item);
    }
}