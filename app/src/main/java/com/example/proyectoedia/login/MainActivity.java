package com.example.proyectoedia.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.proyectoedia.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    //VISTAS//
    Button mRegistroBtn,mLoginBtn; //--> la M es para que sepamos que es del main

    FirebaseAuth mAuth;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //VISTAS de los botones //
        mRegistroBtn = findViewById(R.id.registro_btn);
        mLoginBtn = findViewById(R.id.login_btn);

        //--El onclick para ir a RegistroActivity

        mRegistroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
            }
        });

        //--OnClick para ir a LoginActivity

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });


    }
}