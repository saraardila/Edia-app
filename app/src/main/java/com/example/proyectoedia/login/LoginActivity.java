package com.example.proyectoedia.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    //Vistas
    EditText mEmailEt, mContrasenaEt;
    TextView mnoestar_registradoTv,molvidar_contrasenaTv;
    Button mLoginBtn;

    //Declarar fireBase
    private FirebaseAuth mAuth;

    //Texto de progreso
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //---->>ACCIONES DE MENÚ + TITULO<<---//

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //Botón volver

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        //Inicializarlo
        mEmailEt = findViewById(R.id.emailEt);
        mContrasenaEt = findViewById(R.id.contrasenaEt);
        mnoestar_registradoTv = findViewById(R.id.noestar_registradoTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        molvidar_contrasenaTv = findViewById(R.id.olvidar_contrasenaTv);


        //--Inicializar el dialogo de progreso

        progressDialog = new ProgressDialog(this);

        //Inicializar la variable de fireBase
        mAuth = FirebaseAuth.getInstance();

        // -- ON CLICK DEL LOGIN
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmailEt.getText().toString();
                String contrasena = mContrasenaEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){ //-- Si el correo no coincide con la estructura de un email:

                    //--Lanzamos el error
                    mEmailEt.setError("El correo no es válido");
                    mEmailEt.setFocusable(true);
                }else{

                    //-- Si el correo es valido:
                    loginUser(email,contrasena);
                }

            }
        });

        //No tener cuenta, onClick
        mnoestar_registradoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
                finish();
            }
        });

        //--Recuperar contrasena ONClick
        molvidar_contrasenaTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                olvidarContrasena();
            }
        });


    }

    // --Crear cuadrode dialogo para recuperar la contrasena
    private void olvidarContrasena() {

        //Alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");

        //Creamos el layout
        LinearLayout linearLayout = new LinearLayout(this);

        //Metemos el texto dentro
        final EditText emalEt = new EditText(this);
        emalEt.setHint("Email");
        emalEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emalEt.setMinEms(16);
        linearLayout.addView(emalEt); //-->> se lo añadimos
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //--Boton recuperar
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String email = emalEt.getText().toString().trim();
                recuperar(email);
            }
        });
        //---Boton cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss(); //--> para que desaparezca el cuadro de dialogo cuando se clicke
            }
        });

        //--Para que aparezca
        builder.create().show();


    }

    private void recuperar(String email) {

        //--Vista del dialogo de progreso
        progressDialog.setMessage("Enviando email...");
        progressDialog.show();

        //--Recuperar ocntraseña con FireBase
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                progressDialog.dismiss();

                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email enviado!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this, "Algo ha fallado", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //--Crear un mensaje de error
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loginUser(String email, String contrasena) {

        //--Vista del dialogo de progreso
        progressDialog.setMessage("Iniciando...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Para que acabe el progressDialog
                            progressDialog.dismiss();

                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Si el usuario se ha logueado, entra en la pagina de perfil
                            startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                            finish();
                        } else {
                            //Para que acabe el progressDialog
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "oh oh, algo salió mal.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Para que acabe el progressDialog
                progressDialog.dismiss();
                //Toast para que aparezca el mensaje
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed(); //-->> Para ir al activity anterior
        return super.onSupportNavigateUp();
    }
}