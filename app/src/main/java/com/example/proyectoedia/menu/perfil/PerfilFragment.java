package com.example.proyectoedia.menu.perfil;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoedia.login.MainActivity;
import com.example.proyectoedia.R;
import com.example.proyectoedia.publicacion.AdaptadorPublicacion;
import com.example.proyectoedia.publicacion.ModeloPublicacion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class PerfilFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    //ruta donde se almacenarán las imágenes del perfil de usuario y la portada
    String storagePath = "imagenes_portada_perfil";

    ImageView avatarIv, portadaIv;
    TextView nombreTv, descripcionTv,lugarTv;
    FloatingActionButton boton_flotante;
    RecyclerView postsRecyclerView;

    ProgressDialog pd;

    String descripcion;
    String lugar;

    //Permisos para camara y galeria.
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    List<ModeloPublicacion> publicacionList;
    AdaptadorPublicacion adaptadorPublicacion;
    String uid;

    Uri image_uri;

    //Para comprobar el perfil o la portada.
    String foto_perfil_o_portada;

    public PerfilFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        avatarIv = view.findViewById(R.id.avatar);
        portadaIv = view.findViewById(R.id.portadaIv);
        descripcionTv = view.findViewById(R.id.descripcion);
        nombreTv = view.findViewById(R.id.nombre);
        lugarTv = view.findViewById(R.id.lugar);
        boton_flotante = view.findViewById(R.id.boton_flotante);
        postsRecyclerView = view.findViewById(R.id.recyclerView_posts);


        pd = new ProgressDialog(getActivity());

        //Optenemos la informacion del usuario actualmente registrado.
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Verificamos hasta que obtenemos los datos que necesitamos.
                for(DataSnapshot ds: snapshot.getChildren()){
                    String nombre = "" + ds.child("name").getValue();
                    descripcion = "" + ds.child("descripcion").getValue();
                    String imagen = "" + ds.child("imagen").getValue();
                    String portada = "" + ds.child("portada").getValue();
                    lugar = "" + ds.child("lugar").getValue();

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

        //Boton flotante.
        boton_flotante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpcionesEditarUsuario();
            }
        });

        publicacionList = new ArrayList<>();

        verificarUsuarios();
        cargarPosts();

        return view;
    }

    private void cargarPosts() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);

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

                    adaptadorPublicacion = new AdaptadorPublicacion(getActivity(), publicacionList);
                    postsRecyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void buscarPosts(final String buscarQuery) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

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

                    adaptadorPublicacion = new AdaptadorPublicacion(getActivity(), publicacionList);
                    postsRecyclerView.setAdapter(adaptadorPublicacion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean comprobarPermisoAlmacenamiento(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void solicitarPermisoAlmacenamiento(){
        //Permiso de almacenamiento con tiempo de ejecucion
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }


    private boolean comprobarPermisoCamara(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void solicitarPermisoCamara(){
        //Permiso de almacenamiento con tiempo de ejecucion
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }


    private void mostrarOpcionesEditarUsuario() {

        //Diferentes opciones.
        String opciones[] = {"Editar imagen", "Editar portada", "Editar nombre", "Editar descripcion", "Editar Ubicación", "Cambiar Contraseña"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Opciones");

        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    //Modificar la foto.
                    pd.setMessage("Actualizando la foto de perfil");
                    foto_perfil_o_portada = "imagen";
                    mostrarOpcionesParaImagen();

                }else if (which == 1){
                    //Modificar el fondo
                    pd.setMessage("Actualizando la imagen de portada");
                    foto_perfil_o_portada = "portada";
                    mostrarOpcionesParaImagen();

                }else if (which == 2){
                    //Modificar el nombre
                    pd.setMessage("Actualizando el nombre");
                    mostrarActualizacionDeOpciones("name");

                }else if (which == 3){
                    //Modificar la descripcion
                    pd.setMessage("Actualizando la descripción");
                    mostrarActualizacionDeOpciones("descripcion");

                }else if (which == 4){
                    //Modificar la ubicacion
                    pd.setMessage("Actualizando la ubicación");
                    mostrarActualizacionDeOpciones("lugar");

                }else if (which == 5){
                    //Modificar la contraseña
                    pd.setMessage("Cambiando Contraseña");
                    mostrarActualizacionDeContrasena();
                }
            }
        });

        builder.create().show();
    }

    private void mostrarActualizacionDeContrasena() {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.opciones_cambiar_contrasena, null);

        final EditText editTextContrasenaActual = view.findViewById(R.id.editTextContrasenaActual);
        final EditText editTextContrasenaNueva = view.findViewById(R.id.editTextContrasenaNueva);
        Button botonCambiarContrasena = view.findViewById(R.id.cambiarContrasena);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        botonCambiarContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String contrasenaAntigua = editTextContrasenaActual.getText().toString().trim();
                String contrasenaNueva = editTextContrasenaNueva.getText().toString().trim();

                if(TextUtils.isEmpty(contrasenaAntigua)){
                    Toast.makeText(getActivity(), "Introduzca la contraseña actual", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(contrasenaNueva.length() < 6){
                    Toast.makeText(getActivity(), "La contraseña debe tener almenos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                actualizarContrasena(contrasenaAntigua, contrasenaNueva);
            }
        });
    }

    private void actualizarContrasena(String contrasenaAntigua, final String contrasenaNueva) {
        pd.show();

        final FirebaseUser user = firebaseAuth.getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), contrasenaAntigua);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(contrasenaNueva)
                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       pd.dismiss();
                                       Toast.makeText(getActivity(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                   }
                               })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarActualizacionDeOpciones(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Editar " + key);//Actualizar el nombre o la descripcion.

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Editar " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Agregar un boton para actualizar.
        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String value = editText.getText().toString().trim();

                //Validamos si el usuario a ingresado algo o no.
                if(!TextUtils.isEmpty(value)){

                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Actualizando...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    //Si el usuario cambia el nombre, que se cambie tambien en los mensajes históricos.
                    if(key.equals("name")){

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for(DataSnapshot ds: dataSnapshot.getChildren()){

                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Comentarios")){
                                        String child1 = ""+ snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comentarios").orderByChild("uid").equalTo(uid);
                                        child2.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }else {
                    Toast.makeText(getActivity(), "Por favor introduzca: " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Agregar un boton para cancelar.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void mostrarOpcionesParaImagen() {

        String opciones[] = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Elegir imagen de");

        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    //Camara
                    if(!comprobarPermisoCamara()){
                        solicitarPermisoCamara();
                    }else{
                        cogerFotoDeCamara();
                    }

                }else if (which == 1){
                    //Galeria
                    if(!comprobarPermisoAlmacenamiento()){
                        solicitarPermisoAlmacenamiento();
                    }else{
                        cogerFotoDeGaleria();
                    }

                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Permisos para aceptar o denegar la camara.
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted){

                        cogerFotoDeCamara();
                    }else{
                        Toast.makeText(getActivity(), "Por favor, acepte el permiso de cámara y almacenamiento.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccepted){

                        cogerFotoDeGaleria();
                    }else{
                        Toast.makeText(getActivity(), "Por favor, acepte el permiso de almacenamiento", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Llamaremos a este método una vez hayamos elegido la opción de camara o galeria.

        if(resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){

                image_uri = data.getData();
                subirFotoPortada(image_uri);

            }

            if(requestCode == IMAGE_PICK_CAMERA_CODE){

                subirFotoPortada(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void subirFotoPortada(final Uri uri) {
        //Usaremos el UID del usuario actualmente registrado como nombre de la imagen, por lo que solo habrá un perfil de imagen y una imagen para la portad de cada usuario.

        pd.show();

        String rutaArchivoYNombre = storagePath + "" + foto_perfil_o_portada + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(rutaArchivoYNombre);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //la imagen se carga en el almacenamiento, ahora obtenga su URL y almacénela en la base de datos del usuario

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();

                        //Comprobamos si la imagen esta o no descargada y se recibe la Url.
                        if(uriTask.isSuccessful()){

                            HashMap<String, Object> results = new HashMap<>();

                            results.put(foto_perfil_o_portada, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL en la base de datos del usuario se agregó correctamente
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Imagen actualizada", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error al agregar URL en la base de datos del usuario
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error al actualizar la imagen", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            //Si el usuario cambia el nombre, que se cambie tambien en los mensajes históricos.
                            if(foto_perfil_o_portada.equals("image")){

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for(DataSnapshot ds: dataSnapshot.getChildren()){

                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //Actualizar la foto en los comentarios

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Comentarios")){
                                                String child1 = ""+ snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comentarios").orderByChild("uid").equalTo(uid);
                                                child2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot ds: snapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }else{
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Ha ocurrido algún error", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cogerFotoDeCamara() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Foto temporal");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripción temporal");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Iniciar con la camara.
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void cogerFotoDeGaleria() {

        Intent galeriaIntent = new Intent(Intent.ACTION_PICK);
        galeriaIntent.setType("image/*");
        startActivityForResult(galeriaIntent, IMAGE_PICK_GALLERY_CODE);
    }

    ////----- VERIFICAR QUE EL USUARIO EXISTE-----///
    private void verificarUsuarios(){

        //--Obtener el usuario con fireBase:
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){ //-- Si el usuario está en la bbdd de FireBase:
            //mPerfilTv.setText(user.getEmail());
            uid = user.getUid();

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

        MenuItem item = menu.findItem(R.id.action_search);
        //Para buscar publicaciones de usuarios especificos.
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Cuando se pulsa el boton de busqueda.
                if(!TextUtils.isEmpty(s)){

                    buscarPosts(s);

                }else{
                    cargarPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Cuando se escribe cualquier letra en la busqueda.
                if(!TextUtils.isEmpty(s)){

                    buscarPosts(s);

                }else{
                    cargarPosts();
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

        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            verificarUsuarios();
        }

        return super.onOptionsItemSelected(item);
    }

}