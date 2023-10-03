package com.example.proyectoedia.notificaciones;

public class Sender {

    private Data data;
    private String enviarA;

    public Sender() {
    }

    public Sender(Data data, String enviarA) {
        this.data = data;
        this.enviarA = enviarA;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getEnviarA() {
        return enviarA;
    }

    public void setEnviarA(String enviarA) {
        this.enviarA = enviarA;
    }
}
