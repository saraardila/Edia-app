package com.example.proyectoedia.notificaciones;

public class Token {

    //--> token para el registro, para conectar el servidor al cliente y poder recibir los mensajes
    // Con un id normal da problemas.

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
