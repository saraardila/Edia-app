package com.example.proyectoedia.publicacion;

public class ModeloComentarios {

    String cId, pComentario, horaDia, uid, uEmail, uDp, uNombre;

    public ModeloComentarios() {
    }

    public ModeloComentarios(String cId, String pComentario, String horaDia, String uid, String uEmail, String uDp, String uNombre) {
        this.cId = cId;
        this.pComentario = pComentario;
        this.horaDia = horaDia;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uNombre = uNombre;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getpComentario() {
        return pComentario;
    }

    public void setpComentario(String pComentario) {
        this.pComentario = pComentario;
    }

    public String getHoraDia() {
        return horaDia;
    }

    public void setHoraDia(String horaDia) {
        this.horaDia = horaDia;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuNombre() {
        return uNombre;
    }

    public void setuNombre(String uNombre) {
        this.uNombre = uNombre;
    }
}