package com.example.proyectoedia.publicacion;

public class ModeloPublicacion {

    String pId, pTitulo, pDescripcion, pLikes, pComentarios, pImagen, pTime, uid, uEmail, uDp, uName;

    public ModeloPublicacion() {
    }

    public ModeloPublicacion(String pId, String pTitulo, String pDescripcion, String pLikes, String pComentarios, String pImagen, String pTime, String uid, String uEmail, String uDp, String uName) {
        this.pId = pId;
        this.pTitulo = pTitulo;
        this.pDescripcion = pDescripcion;
        this.pLikes = pLikes;
        this.pComentarios = pComentarios;
        this.pImagen = pImagen;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitulo() {
        return pTitulo;
    }

    public void setpTitulo(String pTitulo) {
        this.pTitulo = pTitulo;
    }

    public String getpDescripcion() {
        return pDescripcion;
    }

    public void setpDescripcion(String pDescripcion) {
        this.pDescripcion = pDescripcion;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComentarios() {
        return pComentarios;
    }

    public void setpComentarios(String pComentarios) {
        this.pComentarios = pComentarios;
    }

    public String getpImagen() {
        return pImagen;
    }

    public void setpImagen(String pImagen) {
        this.pImagen = pImagen;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
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

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
