package com.example.proyectoedia.menu.Buscador;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.database.FirebaseDatabase.*;


public class UsuariosFragment extends Fragment {
    RecyclerView recyclerView;
    AdaptadorUsuarios adaptadorUsuarios;
    List<ModeloUsuarios> usuariosList;

    //Autentificacion de FireBase
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // -->> El inflador
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);

        //-->Inicializar la autentificacion
        firebaseAuth = FirebaseAuth.getInstance();

        //-->El recycler view de la lista de usuarios
        recyclerView = view.findViewById(R.id.usuarios_recyclerView);

        //--> las propiedades del recycler
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //-->>ArrayList de usuarios
        usuariosList = new ArrayList<>();

        //-->> recorrer todos los usuarios para listarlos

        getTodosLosUsuarios();

        return view;

    }

    //---OBTENER TODOS LOS USUARIOS---//
    private void getTodosLosUsuarios() {

        //-->> Traernos los usuarios de FireBase:

         final FirebaseUser fUser;
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        //-->>Conseguir la informacion de esos usuarios en firebase:
        DatabaseReference ref;
        ref = getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT) //-->> Hace falta esto para que coja bien los IDs de la BBDD de fireBase
                                                            //-->> Y además poner 'Objects' en el if

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usuariosList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloUsuarios modeloUsuarios = ds.getValue(ModeloUsuarios.class);

                    if(!Objects.equals(modeloUsuarios.getUid(), fUser.getUid())){
                        usuariosList.add(modeloUsuarios);
                    } ///------>>COMENTADO PARA QUE NO SALGAN TODOS LOS USUARIOS REGISTRADOS-->>!!

                    //-->> Adaptador
                    adaptadorUsuarios = new AdaptadorUsuarios(getActivity(),usuariosList);

                    recyclerView.setAdapter(adaptadorUsuarios);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///---- BUSCAR USUARIOS---//
    private void searchUsers(final String query) {
        //-->> Traernos los usuarios de FireBase:

        final FirebaseUser fUser;
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        //-->>Conseguir la informacion de esos usuarios en firebase:
        DatabaseReference ref;
        ref = getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT) //-->> Hace falta esto para que coja bien los IDs de la BBDD de fireBase
            //-->> Y además poner 'Objects' en el if

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usuariosList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModeloUsuarios modeloUsuarios = ds.getValue(ModeloUsuarios.class);

                    if(!Objects.equals(modeloUsuarios.getUid(), fUser.getUid())){
                    //Para filtrar la busqueda
                        if(modeloUsuarios.getName() != null &&
                                modeloUsuarios.getName().contains(query.toLowerCase())
                                || modeloUsuarios.getEmail() != null
                                && modeloUsuarios.getEmail().contains(query.toLowerCase()))
                        {

                            usuariosList.add(modeloUsuarios);
                        }
                    }
                    //-->> Adaptador
                    adaptadorUsuarios = new AdaptadorUsuarios(getActivity(),usuariosList);
                    //Actualizar el adaptador:
                    adaptadorUsuarios.notifyDataSetChanged();

                    recyclerView.setAdapter(adaptadorUsuarios);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        inflater.inflate(R.menu.menu_main, menu);

        //--> Para mostrar el buscador en la vista
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                //-->> esta es la llamada de cuando el usuario presiona en el boton de buscar

                //--> Si la consulta no está vacía entonces busca:

                if (!TextUtils.isEmpty(s.trim())) {
                    searchUsers(s);

                } else {

                    //getTodosLosUsuarios();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                //-->> y esta es la llamada de cuando se presiona cualquier letra
                if (!TextUtils.isEmpty(s.trim())) {
                    searchUsers(s);

                } else {

                  //  getTodosLosUsuarios();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //-- On click del menu
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