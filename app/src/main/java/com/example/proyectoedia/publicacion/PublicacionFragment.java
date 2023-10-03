package com.example.proyectoedia.publicacion;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.widget.Toast;

import com.example.proyectoedia.R;
import com.example.proyectoedia.login.InicioActivity;
import com.example.proyectoedia.login.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class PublicacionFragment extends Fragment {

    ActionBar actionBar;

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbReference;

    //Permisos.
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;

    String[] permisosCamara;
    String[] permisosGaleria;

    private static final int IMAGEN_CAMARA_CODE = 300;
    private static final int IMAGEN_GALERIA_CODE = 400;


    EditText tituloEt, descripcionEt;
    ImageView imagenIv;
    Button publicarBtn;

    //Informacion del usuario.
    String name, email, uid, dp;

    String editTitulo, editDescripcion, editImagen;

    Uri image_rui;

    ProgressDialog progressDialog;

    public PublicacionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_publicacion, container, false);

        // actionBar = getSupportActionBar();
        // actionBar.setTitle("Nueva publicacion");
        // actionBar.setDisplayShowHomeEnabled(true);
        // actionBar.setDisplayHomeAsUpEnabled(true);

        //Permisos.
        permisosCamara = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        permisosGaleria = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        comprobarEstadoUsuario();

        tituloEt = view.findViewById(R.id.pTitulo);
        descripcionEt = view.findViewById(R.id.pDescripcionEt);
        imagenIv = view.findViewById(R.id.pImagenIv);
        publicarBtn = view.findViewById(R.id.pPublicarBtn);


        //obtener datos a través del adaptador de la actividad anterior
        Intent intent = getActivity().getIntent();
        final String claveActualizacion = "" + intent.getStringExtra("key");
        final String editarPostId = "" + intent.getStringExtra("EditarPostId");

        if(claveActualizacion.equals("EditarPost")){
            //actionBar.setTitle("Actualizar post");
            publicarBtn.setText("Actualizar");
            cargarDatosDelPost(editarPostId);
        }else{
            //actionBar.setTitle("Añadir nuevo post");
            publicarBtn.setText("Publicar");
        }

        // actionBar.setSubtitle(email);

        //obtener información del usuario actual para incluirla en el post
        userDbReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){

                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" +ds.child("imagen").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //Para obtener la imagen de la camara o galeria.
        imagenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSeleccionImagen();
            }
        });

        publicarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = tituloEt.getText().toString().trim();
                String descripcion = descripcionEt.getText().toString().trim();

                if(TextUtils.isEmpty(titulo)){
                    Toast.makeText(getActivity(), "Ponga un título", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(descripcion)){
                    Toast.makeText(getActivity(), "Ponga una descripción", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(claveActualizacion.equals("EditarPost")){
                    comenzarActualizar(titulo, descripcion, editarPostId);
                }else{
                    cargarDatos(titulo, descripcion);

                    startActivity(new Intent(getActivity(), InicioActivity.class));
                }


               /* if(image_rui == null){
                    cargarDatos(titulo, descripcion, "noImagen");
                }else{
                    cargarDatos(titulo, descripcion, String.valueOf(image_rui));
                }*/

            }
        });
        return view;
    }

    private void comenzarActualizar(String titulo, String descripcion, String editarPostId) {

        progressDialog.setMessage("Actualizando el Post");
        progressDialog.show();

        if(!editImagen.equals("noImagen")){
            actualizacionConImagen(titulo, descripcion, editarPostId);

        }else if (imagenIv.getDrawable() != null){
            actualizacionNuevaImagen(titulo, descripcion, editarPostId);

        }else {
            actualizacionSinImagen(titulo, descripcion, editarPostId);
        }
    }

    private void actualizacionSinImagen(String titulo, String descripcion, String editarPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pLikes","0");
        hashMap.put("pTitulo", titulo);
        hashMap.put("pDescripcion", descripcion);
        hashMap.put("pImagen", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editarPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizacionNuevaImagen(final String titulo, final String descripcion, final String editarPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String rutaYNombreArchivo = "Posts/" + "post_" + timeStamp;

        //Obtenemos la imagen del imageView.
        Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //Imagen comprimida.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(rutaYNombreArchivo);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Obtenemos la url de la imagen.
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitulo", titulo);
                            hashMap.put("pDescripcion", descripcion);
                            hashMap.put("pImagen", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editarPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizacionConImagen(final String titulo, final String descripcion, final String editarPostId) {

        StorageReference mImagenRef =  FirebaseStorage.getInstance().getReferenceFromUrl(editImagen);
        mImagenRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String rutaYNombreArchivo = "Posts/" + "post_" + timeStamp;

                        //Obtenemos la imagen del imageView.
                        Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //Imagen comprimida.
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(rutaYNombreArchivo);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //Obtenemos la url de la imagen.
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if(uriTask.isSuccessful()){
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);
                                            hashMap.put("uEmail", email);
                                            hashMap.put("uDp", dp);
                                            hashMap.put("pTitulo", titulo);
                                            hashMap.put("pDescripcion", descripcion);
                                            hashMap.put("pImagen", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editarPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarDatosDelPost(String editarPostId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = reference.orderByChild("pId").equalTo(editarPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    editTitulo = "" + ds.child("pTitulo").getValue();
                    editDescripcion = "" + ds.child("pDescripcion").getValue();
                    editImagen = "" + ds.child("pImagen").getValue();

                    tituloEt.setText(editTitulo);
                    descripcionEt.setText(editDescripcion);

                    if(!editImagen.equals("noImagen")){
                        try {
                            Picasso.get().load(editImagen).into(imagenIv);
                        }catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cargarDatos(final String titulo, final String descripcion) {

        progressDialog.setMessage("Publicando post..");
        progressDialog.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String rutaYNombreArchivo = "Posts/" + "post_" + timeStamp;

        if(imagenIv.getDrawable() != null){
            //Obtenemos la imagen del imageView.
            Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Imagen comprimida.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(rutaYNombreArchivo);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String descargarUri = uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("pLikes","0");
                                hashMap.put("uDp", dp);
                                hashMap.put("pComentarios","0");
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitulo", titulo);
                                hashMap.put("pDescripcion", descripcion);
                                hashMap.put("pImagen", descargarUri);
                                hashMap.put("pTime", timeStamp);

                                //ruta para almacenar los datos.
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(), "Post publicado", Toast.LENGTH_SHORT).show();
                                                //Reseteamos la vista.
                                                tituloEt.setText("");
                                                descripcionEt.setText("");
                                                imagenIv.setImageURI(null);
                                                image_rui = null;

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Si hay un error añadiendo el post en la bbdd.
                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Fallo al cargar la imagen.
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitulo", titulo);
            hashMap.put("pDescripcion", descripcion);
            hashMap.put("pImagen", "noImagen");
            hashMap.put("pTime", timeStamp);

            //ruta para almacenar los datos.
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Post publicado", Toast.LENGTH_SHORT).show();
                            //Reseteamos la vista.
                            tituloEt.setText("");
                            descripcionEt.setText("");
                            imagenIv.setImageURI(null);
                            image_rui = null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Si hay un error añadiendo el post en la bbdd.
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }

    private void mostrarSeleccionImagen() {

        String[] opciones = {"Camara", "Galeria"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Elegir imagen de");

        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    //Si pulsas en la camara
                    if(!permisoCamara()){
                        solicitarPermisoCamara();
                    }else{
                        cogerDeLaCamara();
                    }
                }

                if(which == 1){
                    //Si pulsas en la galeria.
                    if(!permisoAlmacenamiento()){
                        solicitarPermisoAlmacenamiento();
                    }else{
                        cogerDeLaGaleria();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void cogerDeLaGaleria() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGEN_GALERIA_CODE);
    }

    private void cogerDeLaCamara() {

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Selección temporal");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Descripción temporal");
        image_rui = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGEN_CAMARA_CODE);

    }

    private boolean permisoAlmacenamiento(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void solicitarPermisoAlmacenamiento(){
        ActivityCompat.requestPermissions(getActivity(), permisosGaleria, GALLERY_REQUEST_CODE);
    }


    private boolean permisoCamara(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void solicitarPermisoCamara(){
        ActivityCompat.requestPermissions(getActivity(), permisosCamara, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        comprobarEstadoUsuario();
    }

    @Override
    public void onResume() {
        super.onResume();
        comprobarEstadoUsuario();
    }

    private void comprobarEstadoUsuario(){
        //Optenemos el usuario actual.
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){

            email = user.getEmail();
            uid = user.getUid();

        }else{
            //startActivity(new Intent(PublicacionFragment.class, MainActivity.class));
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.nav_publicacion).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //Resultado del permiso.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean camaraAceptada = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean galeriaAceptada = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(camaraAceptada && galeriaAceptada){
                        cogerDeLaCamara();
                    }else{
                        Toast.makeText(getActivity(), "Los permisos de la camara y galeria son necesarios", Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }
            break;

            case GALLERY_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean galeriaAceptada = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(galeriaAceptada){
                        cogerDeLaGaleria();
                    }else{
                        Toast.makeText(getActivity(), "Es necesario el permiso de la galeria", Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == Activity.RESULT_OK){

            if(requestCode == IMAGEN_GALERIA_CODE){

                image_rui = data.getData();
                imagenIv.setImageURI(image_rui);

            } else if(requestCode == IMAGEN_CAMARA_CODE){

                imagenIv.setImageURI(image_rui);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}