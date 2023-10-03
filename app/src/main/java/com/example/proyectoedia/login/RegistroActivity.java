package com.example.proyectoedia.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistroActivity extends AppCompatActivity {

    //Vistas
    EditText mEmailEt, mContrasenaET, mNickEt, mLugar, mDescripcion;
    Button mRegistroBtn;
    TextView mEstarRegistradoTv;

    //Progress bar para el registro
    ProgressDialog progressDialog;

    //Conxion con FireBase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //---->>ACCIONES DE MENÚ + TITULO<<---//

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crear cuenta");

        //Botón volver

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //Inicializar el contenido
        mEmailEt = findViewById(R.id.emailEt);
        mNickEt =findViewById(R.id.nikEt);
        mLugar = findViewById(R.id.lugarEt);
        mDescripcion = findViewById(R.id.descripcionEt);
        mContrasenaET = findViewById(R.id.contrasenaEt);
        mRegistroBtn = findViewById(R.id.registro_btn);
        mEstarRegistradoTv = findViewById(R.id.estar_registradoTv);



        //Inicializar la variable de Firebase
        mAuth = FirebaseAuth.getInstance();

        //Vista ProgressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario, espere...");


        //ON CLICK

        mRegistroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Guardamos los datos en un String
                String email = mEmailEt.getText().toString().trim();
                String lugar = mLugar.getText().toString().trim();
                String descripcion = mDescripcion.getText().toString().trim();
                String contrasena = mContrasenaET.getText().toString().trim();
                String nick = mNickEt.getText().toString().trim();
                //Validacion

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { //-- Si el email no es igual

                    //Alertamos del error
                    mEmailEt.setError("El e-mail no es válido");
                    mEmailEt.setFocusable(true);

                }else if(contrasena.length()<6){ //-- Si la contrasena es más corta que 6 digitos

                    //Alertamos del error
                    mContrasenaET.setError("La contraseña es demasiado corta, debe contener al menos 6 caracteres");
                    mContrasenaET.setFocusable(true);

                }else if(nick.length()<3) { //-- Si el nick es mas corto que 3

                    //Alertamos del error
                    mNickEt.setError("Debe introducir un nombre más largo");
                    mNickEt.setFocusable(true);

                }else if(lugar.length()<3) { //-- Si la ubicacion es mas corto que 3
                    //Alertamos del error
                    mLugar.setError("Debe introducir un lugar más largo");
                    mLugar.setFocusable(true);

                }else if(descripcion.length()<10) { //-- Si la descripción es mas corto que 10 caracteres
                    //Alertamos del error
                    mDescripcion.setError("Debe introducir una descripción de al menos 10 caracteres");
                    mDescripcion.setFocusable(true);
                }
                else{
                    registrarUsuario(email,contrasena,nick,lugar,descripcion);
                }
            }
        });

        //---ON CLICK LOGIN

        mEstarRegistradoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void registrarUsuario(String email, final String contrasena, final String nick, final String lugar, final String descripcion) {
        //-- Si el email y la contrasena es valida, mostramos la barra de progreso y el usuario se registra

        progressDialog.show();

        //Este codigo nos lo da Firebase para la validacion de usuario
        mAuth.createUserWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();

                            //Obtener el correo electrónico del usuario y el uid de la autenticación
                            String email = user.getEmail();
                            String uid = user.getUid();

                            //Guardar también los datos en la base de datos a tiempo real.
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("name", nick);
                            hashMap.put("lugar", lugar);
                            hashMap.put("descripcion", descripcion);
                            hashMap.put("contraseña", contrasena);
                            hashMap.put("estado", "online");
                            hashMap.put("escribiendoA", "nadie");
                            hashMap.put("uid", uid);
                            hashMap.put("imagen", "");
                            hashMap.put("portada", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegistroActivity.this, "Registrado: "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistroActivity.this, InicioActivity.class));
                            finish();

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegistroActivity.this, "La autentificación ha fallado.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(RegistroActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed(); //-->> Para ir al activity anterior
        return super.onSupportNavigateUp();
    }
}