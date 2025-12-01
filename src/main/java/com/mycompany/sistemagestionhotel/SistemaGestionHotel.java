package com.mycompany.sistemagestionhotel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SistemaGestionHotel {
    private DatosHotel datos;
    private Administrador administrador;
    private GestionHabitaciones gestionHabitaciones;
    private GestionReservas gestionReservas;
    private PersistenciaDatos persistencia;
    private GestionClientes gestionClientes; 

    public SistemaGestionHotel() {
        this.datos = new DatosHotel();
        this.administrador = new Administrador("admin", "1234");
        this.persistencia = new PersistenciaDatos();
        
        // 1. Cargar todos los datos desde los CSV
        cargarDatos();
        
        // 2. Inicialización de gestores (Usando los datos cargados)
        this.gestionClientes = new GestionClientes(datos.getClientes(), datos); 
        this.gestionHabitaciones = new GestionHabitaciones(datos.getHotel(), 
                datos.getReservas());
        
        // 3. Inicializar GestionReservas con todas sus dependencias
        this.gestionReservas = new GestionReservas(datos.getReservas(), 
                gestionHabitaciones, datos, this);
    }
    
    // ==========================================
    // MÉTODOS DELEGADOS Y LÓGICA DE NEGOCIO
    // ==========================================

    public Cliente registrarCliente(Cliente nuevoCliente) {
        Cliente registrado = gestionClientes.registrarCliente(nuevoCliente);
        guardarClientes(); 
        return registrado;
    }
    
    public Cliente buscarClientePorCedula(String cedula) { 
        return gestionClientes.buscarClientePorCedula(cedula); 
    }

    public Cliente buscarClientePorId(int id) { 
        return gestionClientes.buscarClientePorId(id); 
    }

    public Factura generarFactura(Reserva reserva, double porcentajeIVA) {
        if (reserva.getEstado() != EstadoReserva.FINALIZADA) {
            throw new IllegalArgumentException("Solo se puede facturar una "
                    + "reserva FINALIZADA.");
        }
        
        long noches = ChronoUnit.DAYS.between(reserva.getFechaInicio(), 
                reserva.getFechaFin());
        if (noches <= 0) noches = 1; // Mínimo una noche
        
        double subtotal = noches * reserva.getHabitacion().getPrecio();
        
        Factura nuevaFactura = new Factura(
            datos.getNextFacturaId(),
            LocalDate.now(),
            subtotal,
            0.0, // El IVA se calcula abajo
            subtotal,
            reserva.getMetodoPago(),
            reserva
        );
        nuevaFactura.calcularIVA(porcentajeIVA);
        
        datos.getFacturas().add(nuevaFactura);
        datos.setNextFacturaId(datos.getNextFacturaId() + 1);
        
        guardarFacturas(); 
        return nuevaFactura;
    }
    
    public void marcarFacturaComoPagada(int reservaId) {
        boolean encontrada = false;
        for(Factura f : datos.getFacturas()) {
            if(f.getReserva().getId() == reservaId) {
                f.pagar();
                encontrada = true;
                break; // Terminamos el bucle
            }
        }
        if (encontrada) {
            guardarFacturas();
        } else {
            throw new IllegalArgumentException("No se encontró factura para "
                    + "la reserva ID " + reservaId);
        }
    }
    
    public List<Factura> getFacturasDeClientePorCedula(String cedula) {
        return datos.getFacturas().stream()
            .filter(f -> f.getReserva().getCedulaCheckIn().equalsIgnoreCase(cedula))
            .collect(Collectors.toList());
    }

    // ==========================================
    // CARGA Y GUARDADO DE DATOS
    // ==========================================

    private void cargarDatos() {
        try {
            // 1. Cargar Hotel
            Hotel hotelCargado = persistencia.cargarHotel();
            if (hotelCargado == null) {
                hotelCargado = new Hotel("Hotel Paradise", "Calle Principal 123", 
                        "+57 1 2345678");
            }
            datos.setHotel(hotelCargado);

            // 2. Cargar Clientes
            datos.setClientes(persistencia.cargarClientes());
            
            // 3. Cargar Habitaciones y vincularlas al Hotel
            List<Habitacion> habitacionesCargadas = persistencia.cargarHabitaciones();
            // Limpiar lista actual del hotel para evitar duplicados si se recarga
            if (datos.getHotel().getHabitaciones() != null) {
                datos.getHotel().getHabitaciones().clear();
            }
            // Agregar las habitaciones cargadas al objeto hotel
            for (Habitacion h : habitacionesCargadas) {
                datos.getHotel().agregarHabitacion(h);
            }
            datos.setHabitaciones(habitacionesCargadas);
            
            // 4. Calcular IDs siguientes
            datos.setNextClienteId(datos.getClientes().stream().mapToInt(Cliente::getId).max().orElse(0) + 1);
            
            // 5. Cargar Reservas (Requiere Habitaciones y Clientes ya cargados)
            datos.setReservas(persistencia.cargarReservas(datos.getHabitaciones(), 
                    datos.getClientes()));
            datos.setNextReservaId(datos.getReservas().stream().mapToInt(Reserva::getId).max().orElse(0) + 1);

            // 6. Cargar Facturas (Requiere Reservas ya cargadas)
            datos.setFacturas(persistencia.cargarFacturas(datos.getReservas()));
            datos.setNextFacturaId(datos.getFacturas().stream().mapToInt(Factura::getId).max().orElse(0) + 1);
            
            System.out.println("Datos cargados exitosamente.");

        } catch (IOException e) {
            System.err.println("Advertencia: No se pudieron cargar todos los "
                    + "datos (posible primera ejecución): " + e.getMessage());
            inicializarDatosVacios();
        }
    }
    
    private void inicializarDatosVacios() {
        if (datos.getHotel() == null) datos.setHotel(new Hotel("Hotel Paradise", 
                "Dir", "Tel"));
        if (datos.getClientes() == null) datos.setClientes(new ArrayList<>());
        if (datos.getHabitaciones() == null) datos.setHabitaciones(new ArrayList<>());
        if (datos.getReservas() == null) datos.setReservas(new ArrayList<>());
        if (datos.getFacturas() == null) datos.setFacturas(new ArrayList<>());
    }

    public void guardarClientes() {
        try { persistencia.guardarClientes(datos.getClientes()); } 
        catch (IOException e) { System.err.println("Error guardar clientes: " + e.getMessage()); }
    }
    
    public void guardarHabitaciones() {
        try { persistencia.guardarHabitaciones(datos.getHotel()); } 
        catch (IOException e) { System.err.println("Error guardar habitaciones: " + e.getMessage()); }
    }
    
    public void guardarReservas() {
        try { persistencia.guardarReservasUnificado(datos.getClientes(), 
                datos.getReservas()); } catch (IOException e) { System.err.println("Error guardar reservas: " + e.getMessage()); }
    }
    
    public void guardarFacturas() {
        try { persistencia.guardarFacturas(datos.getFacturas()); } 
        catch (IOException e) { System.err.println("Error guardar facturas: " + e.getMessage()); }
    }
    
    // Método auxiliar para agregar habitación desde el menú admin
    public void agregarHabitacion(Habitacion h) {
        gestionHabitaciones.agregarHabitacion(h);
        guardarHabitaciones();
    }

    // ==========================================
    // GETTERS (Necesarios para MenuConsola)
    // ==========================================

    public GestionHabitaciones getGestionHabitaciones() { return gestionHabitaciones; }
    public GestionReservas getGestionReservas() { return gestionReservas; }
    public GestionClientes getGestionClientes() { return gestionClientes; }
    public Administrador getAdministrador() { return administrador; }
    
    // Estos getters de listas son usados por el reporte del Admin
    public List<Reserva> getReservas() { return datos.getReservas(); }
    public List<Factura> getFacturas() { return datos.getFacturas(); }
    public List<Cliente> getClientes() { return datos.getClientes(); }
}