
package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SistemaGestionHotel {
    private Hotel hotel;
    private List<Cliente> clientes;
    private Administrador administrador;
    private List<Reserva> reservas;
    private List<Factura> facturas;
    private int nextClienteId = 1;
    private int nextReservaId = 1;
    private int nextFacturaId = 1;

    public Sistema() {
        this.clientes = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.facturas = new ArrayList<>();
        this.administrador = new Administrador("admin", "admin123");
        inicializarSistema();
    }

    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        sistema.mostrarEstadoInicial();
    }

    private void inicializarSistema() {
        // Crear hotel con datos básicos
        this.hotel = new Hotel("Hotel Paradise", "Calle Principal 123", "+57 1 2345678");
        
        // Agregar habitaciones de diferentes tipos
        agregarHabitacionesPrueba();
        
        // Agregar algunos clientes de prueba
        agregarClientesPrueba();
    }

    private void agregarHabitacionesPrueba() {
        hotel.agregarHabitacion(new Habitacion(101, "Individual", 100000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(102, "Individual", 100000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(103, "Doble", 150000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(104, "Doble", 150000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(201, "Suite", 300000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(202, "Suite", 300000, "Disponible"));
        hotel.agregarHabitacion(new Habitacion(301, "Presidencial", 500000, "Disponible"));
    }

    private void agregarClientesPrueba() {
        registrarCliente(new Cliente(nextClienteId++, "Juan Pérez", "12345678", "juan@email.com", "3001234567"));
        registrarCliente(new Cliente(nextClienteId++, "María García", "87654321", "maria@email.com", "3007654321"));
        registrarCliente(new Cliente(nextClienteId++, "Carlos López", "11223344", "carlos@email.com", "3001122334"));
    }

    // ===== MÉTODOS PRINCIPALES DEL SISTEMA =====

    public List<Habitacion> buscarHabitacionesDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Habitacion> todasHabitaciones = hotel.getHabitacionesDisponibles();
        
        // Filtrar habitaciones que no tienen reservas conflictivas en las fechas solicitadas
        return todasHabitaciones.stream()
                .filter(habitacion -> estaDisponibleEnFechas(habitacion, fechaInicio, fechaFin))
                .collect(Collectors.toList());
    }

    private boolean estaDisponibleEnFechas(Habitacion habitacion, LocalDate fechaInicio, LocalDate fechaFin) {
        return reservas.stream()
                .noneMatch(reserva -> 
                    reserva.getHabitacion().equals(habitacion) &&
                    reservasSeSuperponen(
                        reserva.getFechaInicio(), reserva.getFechaFin(),
                        fechaInicio, fechaFin
                    )
                );
    }

    private boolean reservasSeSuperponen(LocalDate inicio1, LocalDate fin1, LocalDate inicio2, LocalDate fin2) {
        return !(fin1.isBefore(inicio2) || inicio1.isAfter(fin2));
    }

    public Reserva realizarReserva(Cliente cliente, Habitacion habitacion, LocalDate fechaInicio, LocalDate fechaFin) {
        // Verificar disponibilidad
        if (!estaDisponibleEnFechas(habitacion, fechaInicio, fechaFin)) {
            throw new IllegalStateException("La habitación no está disponible en las fechas seleccionadas");
        }

        // Crear reserva
        Reserva reserva = new Reserva(nextReservaId++, fechaInicio, fechaFin, "Confirmada", 
                                    habitacion, cliente.getCedula(), "Tarjeta");
        reservas.add(reserva);
        
        // Actualizar estado de la habitación
        habitacion.setEstado("Reservada");
        
        return reserva;
    }

    public Factura generarFactura(Reserva reserva) {
        double subtotal = reserva.calcularTotal();
        double iva = subtotal * 0.19; // 19% IVA
        double total = subtotal + iva;

        Factura factura = new Factura(nextFacturaId++, LocalDate.now(), subtotal, iva, total, 
                                    reserva.getMetodoPago(), reserva);
        facturas.add(factura);
        
        return factura;
    }

    public boolean realizarCheckIn(Reserva reserva, String cedula) {
        if (!reserva.getCedulaCheckIn().equals(cedula)) {
            return false;
        }
        
        reserva.realizarCheckIn(cedula);
        reserva.setEstado("Check-In Realizado");
        reserva.getHabitacion().setEstado("Ocupada");
        
        return true;
    }

    public boolean autenticarAdministrador(String usuario, String contraseña) {
        return administrador != null && 
               administrador.getUsuario().equals(usuario) && 
               administrador.getContraseña().equals(contraseña);
    }

    public void registrarCliente(Cliente cliente) {
        cliente.setId(nextClienteId++);
        clientes.add(cliente);
    }

    // ===== MÉTODOS DE CONSULTA =====

    public Cliente buscarClientePorCedula(String cedula) {
        return clientes.stream()
                .filter(cliente -> cliente.getCedula().equals(cedula))
                .findFirst()
                .orElse(null);
    }

    public Reserva buscarReservaPorId(int id) {
        return reservas.stream()
                .filter(reserva -> reserva.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Reserva> getReservasPorCliente(String cedulaCliente) {
        return reservas.stream()
                .filter(reserva -> reserva.getCedulaCheckIn().equals(cedulaCliente))
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS DE ADMINISTRACIÓN =====

    public void agregarHabitacion(Habitacion habitacion) {
        hotel.agregarHabitacion(habitacion);
    }

    public void actualizarPrecioHabitacion(int numeroHabitacion, double nuevoPrecio) {
        hotel.getHabitacionesDisponibles().stream()
                .filter(h -> h.getNumero() == numeroHabitacion)
                .findFirst()
                .ifPresent(h -> h.setPrecio(nuevoPrecio));
    }

    // ===== MÉTODOS DE UTILIDAD =====

    public void mostrarEstadoInicial() {
        System.out.println("=== SISTEMA DE GESTIÓN HOTELERA INICIALIZADO ===");
        System.out.println("Hotel: " + hotel.getNombre());
        System.out.println("Habitaciones disponibles: " + hotel.getHabitacionesDisponibles().size());
        System.out.println("Clientes registrados: " + clientes.size());
        System.out.println("Reservas activas: " + reservas.size());
        System.out.println("=============================================");
    }

    public void limpiarDatosPrueba() {
        reservas.clear();
        facturas.clear();
        hotel.getHabitacionesDisponibles().forEach(h -> h.setEstado("Disponible"));
        System.out.println("Datos de prueba limpiados. Sistema listo para uso real.");
    }

    // ===== GETTERS PARA LA INTERFAZ GRÁFICA =====

    public Hotel getHotel() { return hotel; }
    public List<Cliente> getClientes() { return new ArrayList<>(clientes); }
    public List<Reserva> getReservas() { return new ArrayList<>(reservas); }
    public List<Factura> getFacturas() { return new ArrayList<>(facturas); }
    public Administrador getAdministrador() { return administrador; }
}