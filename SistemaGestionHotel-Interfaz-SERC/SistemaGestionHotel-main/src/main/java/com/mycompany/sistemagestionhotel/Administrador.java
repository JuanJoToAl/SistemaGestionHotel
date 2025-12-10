package com.mycompany.sistemagestionhotel;

public class Administrador {
    private String usuario;
    private String contraseña;

    public Administrador(String usuario, String contraseña) {
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    // Métodos del UML con una implementación sencilla para consola
    public void gestionarHabitaciones() {
        System.out.println("Use el modo administrador para agregar habitaciones, "
                + "cambiar estados y actualizar precios.");
    }

    public void verReportes() {
        System.out.println("Los reportes se pueden obtener listando reservas y "
                + "facturas desde el menú principal y el modo administrador.");
    }

    public void actualizarPrecios() {
        System.out.println("Actualice precios usando la opción 'Actualizar "
                + "precio de una habitación' en el modo administrador.");
    }
}
