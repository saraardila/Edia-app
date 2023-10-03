package com.example.proyectoedia.notificaciones;

import android.content.Intent;

public class Data {

    private  String usuario, body, title, enviar;

    private Integer icon;

    public Data() {
    }

    public Data(String usuario, String body, String title, String enviar, Integer icon) {

        this.usuario = usuario;
        this.body = body;
        this.title = title;
        this.enviar = enviar;
        this.icon = icon;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnviar() {
        return enviar;
    }

    public void setEnviar(String enviar) {
        this.enviar = enviar;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
