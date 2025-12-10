package com.mycompany.sistemagestionhotel;

public class Cliente {
    private int id;
    private String nombre;
    private String cedula;
    private String email;
    private String telefono;

    public Cliente(int id, String nombre, String cedula, 
            String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.email = email;
        this.telefono = telefono;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Persistencia sencilla a CSV
    public String toCsv() {
        return id + ";" + nombre + ";" + cedula + ";" + email + ";" + telefono;
    }

    public static Cliente fromCsv(String linea) {
        String[] p = linea.split(";");
        int id = Integer.parseInt(p[0]);
        return new Cliente(id, p[1], p[2], p[3], p[4]);
    }
}
