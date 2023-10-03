package com.example.proyectoedia.menu.Chat;

import com.google.firebase.database.PropertyName;

public class ModeloChat {

    String mensaje, recibido, enviado, horadia;
    boolean isVisto;

    public ModeloChat() {
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRecibido() {
        return recibido;
    }

    public void setRecibido(String recibido) {
        this.recibido = recibido;
    }

    public String getEnviado() {
        return enviado;
    }

    public void setEnviado(String enviado) {
        this.enviado = enviado;
    }

    public String getHoradia() {
        return horadia;
    }

    public void setHoradia(String horadia) {
        this.horadia = horadia;
    }

    @PropertyName("isVisto")
    public boolean isVisto() {
        return isVisto;
    }
    @PropertyName("isVisto")
    public void setVisto(boolean visto) {
        this.isVisto = visto;
    }
}
