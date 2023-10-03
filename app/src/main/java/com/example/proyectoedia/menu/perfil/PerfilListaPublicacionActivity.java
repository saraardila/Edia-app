package com.example.proyectoedia.menu.perfil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.example.proyectoedia.publicacion.AdaptadorPublicacion;
import com.example.proyectoedia.publicacion.ModeloPublicacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PerfilListaPublicacionActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ImageView avatarIv, portadaIv;
    TextView nombreTv, descripcionTv, lugarTv;
    RecyclerView postsRecyclerView;

    List<ModeloPublicacion> publicacionList;
    AdaptadorPublicacion adaptadorPublicacion;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_lista_publicacion);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        avatarIv = findViewById(R.id.avatar);
        portadaIv = findViewById(R.id.portadaIv);
        descripcionTv = findViewById(R.id.descripcion);
        nombreTv = findViewById(R.id.nombre);
        lugarTv = findViewById(R.id.lugar);
        postsRecyclerView = findViewById(R.id.recyclerView_posts);

        firebaseAuth =  FirebaseAuth.getInstance();

        //obtener el uid del usuario seleccionado para recuperar sus publicaciones
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Verificamos hasta que obtenemos los datos que necesitamos.
                for(DataSnapshot ds: snapshot.getChildren()){
                    String nombre = "" + ds.child("name").getValue();
                    String descripcion = "" + ds.child("descripcion").getValue();
                    String imagen = "" + ds.child("imagen").getValue();
                    String portada = "" + ds.child("portada").getValue();
                    String lugar = "" + ds.child("lugar").getValue();

                    nombreTv.setText(nombre);
                    descripcionTv.setText(descripcion);
                    lugarTv.setText(lugar);

                    try {
                        Picasso.get().load(imagen).into(avatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.icon_person).into(avatarIv);
                    }

                    try {
                        Picasso.get().load(portada).into(portadaIv);
                    }catch (Exception e){
                        //Picasso.get().load(R.drawable.foto_predeterminada).into(predeterminadaIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        publicacionList = new ArrayList<>();

        verificarUsuarios();
        cargarTodosLosPosts();
    }

    private void cargarTodosLosPosts() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);

        //Cargar el nuevo post primero.
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //Consulta para cargar los posts.
        Query query = ref.orderByChild("uid").equalTo(uid);

        //Obtenemos los datos.
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                publicacionList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    ModeloPublicacion misPosts = ds.getValue(ModeloPublicacion.class);

                    publicacionList.add(misPosts);

                    adaptadorPublicacion = new AdaptadorPublicacion(PerfilListaPublicacionActivity.this, publicacionList);
                    postsRecyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PerfilListaPublicacionActivity.this, ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarTodosLosPosts(final String buscarQuery){

        LinearLayoutManager layoutManager = new LinearLayoutManager(PerfilListaPublicacionActivity.this,LinearLayoutManager.HORIZONTAL,false);

        //Cargar el nuevo post primero.
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //Consulta para cargar los posts.
        Query query = ref.orderByChild("uid").equalTo(uid);

        //Obtenemos los datos.
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                publicacionList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    ModeloPublicacion misPosts = ds.getValue(ModeloPublicacion.class);

                    if(misPosts.getpTitulo().toLowerCase().contains(buscarQuery.toLowerCase()) ||
                            misPosts.getpDescripcion().toLowerCase().contains(buscarQuery.toLowerCase())){

                        publicacionList.add(misPosts);

                    }

                    adaptadorPublicacion = new AdaptadorPublicacion(PerfilListaPublicacionActivity.this, publicacionList);
                    postsRecyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PerfilListaPublicacionActivity.this, ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ////----- VERIFICAR QUE EL USUARIO EXISTE-----///
    private void verificarUsuarios(){

        //--Obtener el usuario con fireBase:
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){ //-- Si el usuario está en la bbdd de FireBase:



        }else { //-- Sino, no está registrado en la app, vuelve a la pagina principal para que se registre

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        //Para buscar publicaciones de usuarios especificos.
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Cuando se pulsa el boton de busqueda.
                if(!TextUtils.isEmpty(s)){

                    buscarTodosLosPosts(s);

                }else{
                    cargarTodosLosPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Cuando se escribe cualquier letra en la busqueda.
                if(!TextUtils.isEmpty(s)){

                    buscarTodosLosPosts(s);

                }else{
                    cargarTodosLosPosts();
                }

                return false;
            }
        });
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