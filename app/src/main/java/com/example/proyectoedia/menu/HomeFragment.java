package com.example.proyectoedia.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class HomeFragment extends Fragment {
    //Autentificacion de FireBase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    List<ModeloPublicacion> publicacionList;
    AdaptadorPublicacion adaptadorPublicacion;
    TextView nomUsuario;


    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container,false);

        //Inicializar la autentificacion
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        recyclerView = view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);

        nomUsuario = view.findViewById(R.id.nomUsuario);

        publicacionList = new ArrayList<>();

        cargarPublicacion();

        nombreUsuario(); //--> para que muestre en el inicio el nombre de quien está registrado

        return view;
    }

    private void nombreUsuario() {

        //Optenemos la informacion del usuario actualmente registrado.
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Verificamos hasta que obtenemos los datos que necesitamos.
                for(DataSnapshot ds: snapshot.getChildren()){
                    String nombre = "" + ds.child("name").getValue();
                    nomUsuario.setText(nombre);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void cargarPublicacion() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                publicacionList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloPublicacion modeloPublicacion = ds.getValue(ModeloPublicacion.class);
                    publicacionList.add(modeloPublicacion);

                    adaptadorPublicacion = new AdaptadorPublicacion(getActivity(), publicacionList);
                    recyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarPosts(final String buscarQuery){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                publicacionList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloPublicacion modeloPublicacion = ds.getValue(ModeloPublicacion.class);

                    if(modeloPublicacion.getpTitulo().toLowerCase().contains(buscarQuery.toLowerCase()) ||
                            modeloPublicacion.getpDescripcion().contains(buscarQuery.toLowerCase())){

                        publicacionList.add(modeloPublicacion);
                    }


                    adaptadorPublicacion = new AdaptadorPublicacion(getActivity(), publicacionList);
                    recyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ////----- VERIFICAR QUE EL USUARIO EXISTE-----///
    private void verificarUsuarios(){

        //--Obtener el usuario con fireBase:
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){ //-- Si el usuario está en la bbdd de FireBase:
            //mPerfilTv.setText(user.getEmail());
        }else { //-- Sino, no está registrado en la app, vuelve a la pagina principal para que se registre

            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);//-->> para mostrarlo como opción en el menú fragment
        super.onCreate(savedInstanceState);

    }

    //--Inflador de opciones del menú
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main,menu);

        //Busqueda de posts a través del título o descripcion.
        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if(!TextUtils.isEmpty(s)){
                    buscarPosts(s);
                }else{
                    cargarPublicacion();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!TextUtils.isEmpty(s)){
                    buscarPosts(s);
                }else{
                    cargarPublicacion();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    //-- On click del menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.action_logout){//el usuario que NO está dentro de la app
            startActivity(new Intent(getActivity(),MainActivity.class));
            firebaseAuth.signOut();
            verificarUsuarios();
        }

        return super.onOptionsItemSelected(item);
    }
}

