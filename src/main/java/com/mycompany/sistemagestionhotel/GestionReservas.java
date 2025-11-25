package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public class GestionReservas {
    private List<Reserva> reservas;
    private GestionHabitaciones gestionHabitaciones;
    private int nextReservaId;
    private SistemaGestionHotel sistema;

    public GestionReservas(List<Reserva> reservas, 
            GestionHabitaciones gestionHabitaciones, int nextReservaId, 
            SistemaGestionHotel sistema) {
        this.reservas = reservas;
        this.gestionHabitaciones = gestionHabitaciones;
        this.nextReservaId = nextReservaId;
        this.sistema = sistema;
    }

    /**
     * Crear reserva -> REQUIERE asociar cliente (por cédula). Se guarda la 
     * cédula en cedulaCheckIn.
     */
    public Reserva crearReserva(LocalDate fechaInicio, LocalDate fechaFin, 
            Habitacion habitacion, String metodoPago, String cedulaCliente) {
        
        if (fechaFin.isBefore(fechaInicio)) throw new 
        IllegalArgumentException("Fecha fin anterior a fecha inicio");
        
        List<Habitacion> disp = 
                gestionHabitaciones.buscarHabitacionesDisponibles(fechaInicio, 
                        fechaFin);
        boolean disponible = disp.stream().anyMatch(h -> h.getNumero() 
                == habitacion.getNumero());
        if (!disponible) {
            throw new IllegalStateException("La habitación no está disponible "
                    + "para las fechas solicitadas");
        }
        
        Reserva r = new Reserva(nextReservaId++, fechaInicio, fechaFin, 
                EstadoReserva.CONFIRMADA, habitacion, cedulaCliente, metodoPago);
        this.reservas.add(r);
        
        gestionHabitaciones.cambiarEstadoHabitacion(habitacion.getNumero(), 
                EstadoHabitacion.RESERVADA);
        
        try {
            sistema.guardarReservas();
            sistema.guardarHabitaciones();
        } catch (Exception e) {
            System.err.println("Error guardando reserva/habitaciones: " + e.getMessage());
        }
        return r;
    }

    public void cancelarReserva(int reservaId) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada");
        r.setEstado(EstadoReserva.CANCELADA);
        Habitacion h = r.getHabitacion();
        
        gestionHabitaciones.cambiarEstadoHabitacion(h.getNumero(), 
                EstadoHabitacion.DISPONIBLE);
        
        try {
            sistema.guardarReservas();
            sistema.guardarHabitaciones();
        } catch (Exception e) {
            System.err.println("Error guardando cancelación: " + e.getMessage());
        }
    }

    public void realizarCheckIn(int reservaId, String cedulaCliente) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada");
        r.realizarCheckIn(cedulaCliente);
        r.setEstado(EstadoReserva.CHECK_IN_REALIZADO);
        Habitacion h = r.getHabitacion();
        
        gestionHabitaciones.cambiarEstadoHabitacion(h.getNumero(), 
                EstadoHabitacion.OCUPADA);
        
        try {
            sistema.guardarReservas();
            sistema.guardarHabitaciones();
        } catch (Exception e) {
            System.err.println("Error guardando check-in: " + e.getMessage());
        }
    }

    public void realizarCheckOut(int reservaId) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada");
        r.realizarCheckOut();
        r.setEstado(EstadoReserva.FINALIZADA);
        Habitacion h = r.getHabitacion();
        
        gestionHabitaciones.cambiarEstadoHabitacion(h.getNumero(), 
                EstadoHabitacion.DISPONIBLE);
        
        try {
            sistema.guardarReservas();
            sistema.guardarHabitaciones();
        } catch (Exception e) {
            System.err.println("Error guardando check-out: " + e.getMessage());
        }
    }

    public Reserva buscarReservaPorId(int id) {
        for (Reserva r : reservas) if (r.getId() == id) return r;
        return null;
    }

    public List<Reserva> getReservasDeClientePorCedula(String cedula) {
        return reservas.stream()
                .filter(r -> cedula.equalsIgnoreCase(r.getCedulaCheckIn()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Getter para nextReservaId, por si se necesita desde fuera
    public int getNextReservaId() {
        return nextReservaId;
    }
}