package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class GestionReservas {
    private List<Reserva> reservas;
    private GestionHabitaciones gestionHabitaciones;
    private DatosHotel datos;
    private SistemaGestionHotel sistema;

    public GestionReservas(List<Reserva> reservas, GestionHabitaciones 
            gestionHabitaciones, DatosHotel datos, SistemaGestionHotel sistema) {
        this.reservas = reservas;
        this.gestionHabitaciones = gestionHabitaciones;
        this.datos = datos;
        this.sistema = sistema;
    }

    public Reserva crearReserva(LocalDate fechaInicio, LocalDate fechaFin, 
                                Habitacion habitacion, String metodoPago, 
                                String cedulaCliente) {
        
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede "
                    + "ser anterior a la de inicio.");
        }

        // Verificar disponibilidad
        List<Habitacion> disponibles = gestionHabitaciones.
                buscarHabitacionesDisponibles(fechaInicio, fechaFin);
        boolean estaDisponible = disponibles.stream().anyMatch(h -> h.getNumero() 
                == habitacion.getNumero());

        if (!estaDisponible) {
            throw new IllegalStateException("La habitación " 
                    + habitacion.getNumero() + " no está disponible en esas fechas.");
        }

        // Crear la reserva
        Reserva nuevaReserva = new Reserva(
            datos.getNextReservaId(),
            fechaInicio,
            fechaFin,
            EstadoReserva.CONFIRMADA,
            habitacion,
            cedulaCliente,
            metodoPago
        );

        // Marcar habitación como RESERVADA
        habitacion.setEstado(EstadoHabitacion.RESERVADA);

        // Actualizar IDs y listas
        datos.setNextReservaId(datos.getNextReservaId() + 1);
        this.reservas.add(nuevaReserva);

        // Guardar cambios
        sistema.guardarReservas();
        sistema.guardarHabitaciones();
        
        return nuevaReserva;
    }

    public void realizarCheckIn(int reservaId, String cedulaCliente) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada con ID: " + reservaId);
        
        // Solo se permite check-in sobre reservas CONFIRMADAS
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede hacer check-in de una reserva CONFIRMADA.");
        }
        
        // Validar que la reserva pertenezca al cliente (opcional)
        if (!r.getCedulaCheckIn().equals(cedulaCliente)) {
             // En este diseño simple, permitimos el check-in si coincide el ID,
             // pero podrías lanzar error si la cédula no coincide.
        }

        r.realizarCheckIn(cedulaCliente); // Cambia estado a CHECK_IN_REALIZADO

        // Marcar habitación como OCUPADA
        gestionHabitaciones.cambiarEstadoHabitacion(r.getHabitacion().getNumero(),
                EstadoHabitacion.OCUPADA);

        sistema.guardarReservas();
        sistema.guardarHabitaciones();
    }

    public void realizarCheckOut(int reservaId) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada "
                + "con ID: " + reservaId);

        // Solo se permite check-out si la reserva ya tiene check-in realizado
        if (r.getEstado() != EstadoReserva.CHECK_IN_REALIZADO) {
            throw new IllegalStateException("Solo se puede hacer check-out de una reserva con check-in realizado.");
        }

        // Verificar que exista una factura pagada asociada a la reserva
        boolean pagada = sistema.getFacturas().stream()
                .anyMatch(f -> f.getReserva().getId() == reservaId && f.estaPagada());
        if (!pagada) {
            throw new IllegalStateException("No se puede hacer check-out: la factura de la reserva aún no está pagada.");
        }

        // 1. Cambiar estado a FINALIZADA
        r.realizarCheckOut();
        
        // 2. Liberar habitación 
        gestionHabitaciones.cambiarEstadoHabitacion(r.getHabitacion().getNumero(), 
                EstadoHabitacion.DISPONIBLE);
        
        // 3. Guardar cambios
        sistema.guardarReservas();
        sistema.guardarHabitaciones();
    }

    public void cancelarReserva(int reservaId) {
        Reserva r = buscarReservaPorId(reservaId);
        if (r == null) throw new NoSuchElementException("Reserva no encontrada "
                + "con ID: " + reservaId);

        // Solo se puede cancelar si aún no se ha hecho check-in ni check-out
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede cancelar una reserva CONFIRMADA sin check-in.");
        }

        r.setEstado(EstadoReserva.CANCELADA);

        // Volver a marcar habitación como DISPONIBLE al cancelar
        gestionHabitaciones.cambiarEstadoHabitacion(r.getHabitacion().getNumero(),
                EstadoHabitacion.DISPONIBLE);

        sistema.guardarReservas();
        sistema.guardarHabitaciones();
    }
    
    // Método para borrar reserva físicamente (Admin)
    public void borrarReserva(int idReserva) {
        Reserva r = buscarReservaPorId(idReserva);
        if (r == null) throw new IllegalArgumentException("Reserva no "
                + "encontrada.");
        
        // Solo permitir borrar si no está activa
        if (r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() 
                == EstadoReserva.CHECK_IN_REALIZADO) {
            throw new IllegalStateException("No se puede borrar una reserva "
                    + "activa. Cancélela primero.");
        }
        
        reservas.remove(r);
        sistema.guardarReservas();
    }

    public Reserva buscarReservaPorId(int id) {
        return reservas.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Reserva> getReservasDeClientePorCedula(String cedula) {
        return reservas.stream()
                .filter(r -> r.getCedulaCheckIn().equalsIgnoreCase(cedula))
                .collect(Collectors.toList());
    }
}