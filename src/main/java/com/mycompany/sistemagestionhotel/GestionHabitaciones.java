package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GestionHabitaciones {
    private Hotel hotel;
    private List<Reserva> reservas;

    public GestionHabitaciones(Hotel hotel, List<Reserva> reservas) {
        this.hotel = hotel;
        this.reservas = reservas;
    }

    /**
     * Devuelve lista de habitaciones disponibles entre las fechas (fechaInicio 
     * inclusive, fechaFin exclusive).
     */
    public List<Habitacion> buscarHabitacionesDisponibles(LocalDate fechaInicio, 
            LocalDate fechaFin) {
        List<Habitacion> todas = hotel.getHabitaciones();
        List<Habitacion> disponibles = new ArrayList<>();
        
        for (Habitacion h : todas) {
            boolean ocupada = false;
            for (Reserva r : reservas) {
                if (r.getHabitacion().getNumero() != h.getNumero()) continue;
                // Si reserva está CANCELADA o FINALIZADA no bloquea
                if (r.getEstado() == EstadoReserva.CANCELADA || r.getEstado() 
                        == EstadoReserva.FINALIZADA) continue;
                if (fechasSeSolapan(fechaInicio, fechaFin, 
                        r.getFechaInicio(), r.getFechaFin())) {
                    ocupada = true;
                    break;
                }
            }
            if (!ocupada && h.estaDisponible()) {
                disponibles.add(h);
            }
        }
        return disponibles;
    }

    private boolean fechasSeSolapan(LocalDate aInicio, LocalDate aFin, 
            LocalDate bInicio, LocalDate bFin) {
        // Solapan si aInicio < bFin && bInicio < aFin
        return (aInicio.isBefore(bFin) && bInicio.isBefore(aFin));
    }

    public Habitacion buscarHabitacionPorNumero(int numero) {
        for (Habitacion h : hotel.getHabitaciones()) {
            if (h.getNumero() == numero) return h;
        }
        return null;
    }

    public void agregarHabitacion(Habitacion habitacion) {
        hotel.agregarHabitacion(habitacion);
    }

    public void actualizarPrecioHabitacion(int numeroHabitacion, 
            double nuevoPrecio) {
        Habitacion habitacion = buscarHabitacionPorNumero(numeroHabitacion);
        if (habitacion != null) {
            habitacion.setPrecio(nuevoPrecio);
        }
    }

    public List<Habitacion> getTodasHabitaciones() {
        return hotel.getHabitaciones();
    }

    public List<Habitacion> getHabitacionesDisponibles() {
        return hotel.getHabitaciones().stream()
                .filter(Habitacion::estaDisponible)
                .collect(Collectors.toList());
    }

    public boolean existeHabitacion(int numero) {
        return buscarHabitacionPorNumero(numero) != null;
    }

    public void cambiarEstadoHabitacion(int numeroHabitacion, 
            EstadoHabitacion nuevoEstado) {
        Habitacion habitacion = buscarHabitacionPorNumero(numeroHabitacion);
        if (habitacion != null) {
            habitacion.setEstado(nuevoEstado);
        }
    }
}