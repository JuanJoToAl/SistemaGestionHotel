package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva {
    private int id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoReserva estado;
    private Habitacion habitacion;
    private String cedulaCheckIn;
    private String metodoPago;

    public Reserva(int id,
                   LocalDate fechaInicio,
                   LocalDate fechaFin,
                   EstadoReserva estado,
                   Habitacion habitacion,
                   String cedulaCheckIn,
                   String metodoPago) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser "
                    + "anterior a la fecha de inicio");
        }
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.habitacion = habitacion;
        this.cedulaCheckIn = cedulaCheckIn;
        this.metodoPago = metodoPago;
    }

    public double calcularTotal() {
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (noches <= 0) {
            noches = 1;
        }
        return noches * habitacion.getPrecio();
    }

    public void realizarCheckIn(String cedula) {
        this.cedulaCheckIn = cedula;
        this.estado = EstadoReserva.CHECK_IN_REALIZADO;
    }

    /**
     * Marca la reserva como finalizada (check-out realizado).
     */
    public void realizarCheckOut() {
        this.estado = EstadoReserva.FINALIZADA;
    }

    public int getId() {
        return id;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public String getCedulaCheckIn() {
        return cedulaCheckIn;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    // Persistencia sencilla a CSV (sin incluir el objeto Habitacion completo)
    // Formato: id;fechaInicio;fechaFin;estado;numeroHabitacion;cedulaCheckIn;metodoPago
    public String toCsv() {
        return id + ";" + fechaInicio + ";" + fechaFin + ";" + estado.name() + 
                ";" + habitacion.getNumero() + ";" + cedulaCheckIn + ";" + 
                metodoPago;
    }

    public static Reserva fromCsv(String linea, Habitacion habitacion) {
        String[] p = linea.split(";");
        int id = Integer.parseInt(p[0]);
        LocalDate inicio = LocalDate.parse(p[1]);
        LocalDate fin = LocalDate.parse(p[2]);
        EstadoReserva estado = EstadoReserva.valueOf(p[3]);
        String cedulaCheckIn = p[5];
        String metodoPago = p[6];
        return new Reserva(id, inicio, fin, estado, habitacion, 
                cedulaCheckIn, metodoPago);
    }
}
