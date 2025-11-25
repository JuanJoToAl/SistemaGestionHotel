package com.mycompany.sistemagestionhotel;

public class Habitacion {
    private int numero;
    private String tipo;
    private double precio;
    private EstadoHabitacion estado; // DISPONIBLE, RESERVADA, OCUPADA, etc.

    public Habitacion(int numero, String tipo, double precio, 
            EstadoHabitacion estado) {
        this.numero = numero;
        this.tipo = tipo;
        this.precio = precio;
        this.estado = estado;
    }

    public boolean estaDisponible() {
        return estado == EstadoHabitacion.DISPONIBLE;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getNumero() {
        return numero;
    }

    public EstadoHabitacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoHabitacion estado) {
        this.estado = estado;
    }

    public String getTipo() {
        return tipo;
    }

    // Persistencia sencilla a CSV
    public String toCsv() {
        return numero + ";" + tipo + ";" + precio + ";" + estado.name();
    }

    public static Habitacion fromCsv(String linea) {
        String[] p = linea.split(";");
        int numero = Integer.parseInt(p[0]);
        String tipo = p[1];
        double precio = Double.parseDouble(p[2]);
        EstadoHabitacion estado = EstadoHabitacion.valueOf(p[3]);
        return new Habitacion(numero, tipo, precio, estado);
    }
}
