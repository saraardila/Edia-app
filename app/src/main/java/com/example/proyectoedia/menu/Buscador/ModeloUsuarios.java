package com.example.proyectoedia.menu.Buscador;

public class ModeloUsuarios {

    //--> Se tiene que poner igual que está en firebase
    String name, email, search, imagen, portada, uid, descripcion,predeterminada,telefono,nombre,contraseña,estado,escribiendoA,lugar;

    public ModeloUsuarios() {
    }

    public ModeloUsuarios(String name, String email, String search, String imagen, String portada, String uid, String descripcion, String predeterminada, String telefono, String nombre, String contraseña, String estado, String escribiendoA, String lugar) {
        this.name = name;
        this.email = email;
        this.search = search;
        this.imagen = imagen;
        this.portada = portada;
        this.uid = uid;
        this.descripcion = descripcion;
        this.predeterminada = predeterminada;
        this.telefono = telefono;
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.estado = estado;
        this.escribiendoA = escribiendoA;
        this.lugar = lugar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPredeterminada() {
        return predeterminada;
    }

    public void setPredeterminada(String predeterminada) {
        this.predeterminada = predeterminada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEscribiendoA() {
        return escribiendoA;
    }

    public void setEscribiendoA(String escribiendoA) {
        this.escribiendoA = escribiendoA;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}

