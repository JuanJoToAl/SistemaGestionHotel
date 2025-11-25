package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

public class SistemaGestionHotel {
    private DatosHotel datos;
    private Administrador administrador;
    private GestionHabitaciones gestionHabitaciones;
    private GestionReservas gestionReservas;
    private PersistenciaDatos persistencia;

    public SistemaGestionHotel() {
        this.persistencia = new PersistenciaDatos();
        this.administrador = new Administrador("admin", "admin123");
        
        // Cargar datos existentes o inicializar sistema nuevo
        this.datos = persistencia.cargarDatos();
        if (this.datos == null) {
            inicializarSistema();
            guardarTodosLosDatos();
        }
        
        // Inicializar módulos de gestión
        this.gestionHabitaciones = new GestionHabitaciones(datos.getHotel(), 
                datos.getReservas());
        this.gestionReservas = new GestionReservas(datos.getReservas(), 
                gestionHabitaciones, datos.getNextReservaId(), this);
    }

    public static void main(String[] args) {
        SistemaGestionHotel sistema = new SistemaGestionHotel();
        sistema.mostrarMenuConsola();
    }

    private void inicializarSistema() {
        this.datos = new DatosHotel();
        this.datos.setHotel(new Hotel("Hotel Paradise", "Calle Principal 123", "+57 1 2345678"));
        agregarHabitacionesPrueba();
        agregarClientesPrueba();
    }

    private void agregarHabitacionesPrueba() {
        Hotel hotel = datos.getHotel();
        hotel.agregarHabitacion(new Habitacion(101, "Individual", 100000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(102, "Individual", 100000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(103, "Doble", 150000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(104, "Doble", 150000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(201, "Suite", 300000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(202, "Suite", 300000, EstadoHabitacion.DISPONIBLE));
        hotel.agregarHabitacion(new Habitacion(301, "Presidencial", 500000, EstadoHabitacion.DISPONIBLE));
    }

    private void agregarClientesPrueba() {
        registrarCliente(new Cliente(0, "Juan Pérez", "12345678", "juan@email.com", "3001234567"));
        registrarCliente(new Cliente(0, "María García", "87654321", "maria@email.com", "3007654321"));
        registrarCliente(new Cliente(0, "Carlos López", "11223344", "carlos@email.com", "3001122334"));
    }

    // ===== MÉTODOS DE PERSISTENCIA SIMPLIFICADOS =====
    
    private void guardarTodosLosDatos() {
        try {
            persistencia.guardarHotel(datos.getHotel());
            persistencia.guardarHabitaciones(datos.getHotel().getHabitaciones());
            persistencia.guardarClientes(datos.getClientes());
            persistencia.guardarReservas(datos.getReservas());
            persistencia.guardarFacturas(datos.getFacturas());
        } catch (IOException e) {
            System.err.println("Error guardando datos: " + e.getMessage());
        }
    }

    public void guardarHabitaciones() {
        try {
            persistencia.guardarHabitaciones(datos.getHotel().getHabitaciones());
        } catch (IOException e) {
            System.err.println("Error guardando habitaciones: " + e.getMessage());
        }
    }

    public void guardarReservas() {
        try {
            persistencia.guardarReservas(datos.getReservas());
        } catch (IOException e) {
            System.err.println("Error guardando reservas: " + e.getMessage());
        }
    }

    private void guardarClientes() {
        try {
            persistencia.guardarClientes(datos.getClientes());
        } catch (IOException e) {
            System.err.println("Error guardando clientes: " + e.getMessage());
        }
    }

    private void guardarFacturas() {
        try {
            persistencia.guardarFacturas(datos.getFacturas());
        } catch (IOException e) {
            System.err.println("Error guardando facturas: " + e.getMessage());
        }
    }

    // ===== OPERACIONES PRINCIPALES =====

    public Cliente registrarCliente(Cliente cliente) {
        if (cliente.getId() <= 0) {
            cliente.setId(datos.getNextClienteId());
            datos.setNextClienteId(datos.getNextClienteId() + 1);
        }
        datos.getClientes().add(cliente);
        guardarClientes();
        return cliente;
    }

    public Cliente buscarClientePorCedula(String cedula) {
        for (Cliente c : datos.getClientes()) {
            if (c.getCedula().equalsIgnoreCase(cedula)) return c;
        }
        return null;
    }

    public Cliente buscarClientePorId(int id) {
        for (Cliente c : datos.getClientes()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public Habitacion buscarHabitacionPorNumero(int numero) {
        return gestionHabitaciones.buscarHabitacionPorNumero(numero);
    }

    public Reserva buscarReservaPorId(int id) {
        return gestionReservas.buscarReservaPorId(id);
    }

    public List<Habitacion> buscarHabitacionesDisponibles(LocalDate fechaInicio, 
            LocalDate fechaFin) {
        return gestionHabitaciones.buscarHabitacionesDisponibles(fechaInicio, 
                fechaFin);
    }

    public Factura generarFactura(Reserva reserva, double porcentajeIVA) {
        double subtotal = reserva.calcularTotal();
        Factura f = new Factura(datos.getNextFacturaId(), LocalDate.now(), subtotal, 0.0, subtotal, 
                               reserva.getMetodoPago(), reserva);
        f.calcularIVA(porcentajeIVA);
        datos.getFacturas().add(f);
        datos.setNextFacturaId(datos.getNextFacturaId() + 1);
        guardarFacturas();
        return f;
    }

    // ===== MÉTODOS DE GESTIÓN =====

    private void mostrarResumenReportes() {
        System.out.println("Resumen: Clientes=" + datos.getClientes().size() +
                " Reservas=" + datos.getReservas().size() +
                " Facturas=" + datos.getFacturas().size());
    }

    private void actualizarPrecioHabitacion() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Número habitación: ");
        int num = Integer.parseInt(sc.nextLine().trim());

        Habitacion h = gestionHabitaciones.buscarHabitacionPorNumero(num);
        if (h == null) {
            System.out.println("La habitación no existe.");
            return;
        }

        System.out.print("Nuevo precio: ");
        double nuevoPrecio = Double.parseDouble(sc.nextLine().trim());

        gestionHabitaciones.actualizarPrecioHabitacion(num, nuevoPrecio);
        guardarHabitaciones();
        System.out.println("Precio actualizado y guardado.");
    }

    public void agregarHabitacion(Habitacion habitacion) {
        gestionHabitaciones.agregarHabitacion(habitacion);
        guardarHabitaciones();
    }

    private List<Factura> facturasDeClientePorCedula(String cedula) {
    Set<Integer> idsReservasCliente = datos.getReservas().stream()
            .filter(r -> cedula.equalsIgnoreCase(r.getCedulaCheckIn()))
            .map(Reserva::getId)
            .collect(Collectors.toSet());  
    
    return datos.getFacturas().stream()
            .filter(f -> idsReservasCliente.contains(f.getReserva().getId()))
            .collect(Collectors.toList());  
    }

    // ===== MODO ADMINISTRADOR =====

    private void modoAdministrador() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Usuario administrador: ");
        String user = sc.nextLine();
        System.out.print("Contraseña: ");
        String pass = sc.nextLine();

        if (!administrador.getUsuario().equals(user) || !administrador.getContraseña().equals(pass)) {
            System.out.println("Credenciales incorrectas.");
            return;
        }

        System.out.println("Acceso administrador concedido.");

        int opcion;
        do {
            System.out.println("\n--- MODO ADMINISTRADOR ---");
            System.out.println("1. Gestionar habitaciones (agregar)");
            System.out.println("2. Actualizar precio de una habitación");
            System.out.println("3. Ver reportes (resumen)");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = Integer.parseInt(sc.nextLine());

            switch (opcion) {
                case 1 -> gestionarHabitaciones();
                case 2 -> actualizarPrecioHabitacion();
                case 3 -> mostrarResumenReportes();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void gestionarHabitaciones() {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.print("Número habitación: ");
            int numero = Integer.parseInt(sc.nextLine());
            System.out.print("Tipo: ");
            String tipo = sc.nextLine();
            System.out.print("Precio: ");
            double precio = Double.parseDouble(sc.nextLine());

            Habitacion h = new Habitacion(numero, tipo, precio, EstadoHabitacion.DISPONIBLE);
            agregarHabitacion(h);
            System.out.println("Habitación agregada correctamente.");

        } catch (Exception e) {
            System.out.println("Error agregando habitación: " + e.getMessage());
        }
    }

    // ===== MENÚ PRINCIPAL (sin cambios en la lógica de presentación) =====

    private void mostrarMenuConsola() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n==== SISTEMA GESTIÓN HOTEL ====");
            System.out.println("1) Listar habitaciones disponibles");
            System.out.println("2) Crear reserva (registra cliente si es nuevo)");
            System.out.println("3) Realizar check-in");
            System.out.println("4) Realizar check-out");
            System.out.println("5) Cancelar reserva");
            System.out.println("6) Generar factura (para reserva)");
            System.out.println("7) Ver cliente (muestra cliente, sus reservas y sus facturas)");
            System.out.println("8) Administrador (requiere usuario/clave)");
            System.out.println("9) Salir");
            System.out.print("Elige una opción: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                    case "1" -> {
                        System.out.print("Fecha inicio (YYYY-MM-DD): ");
                        LocalDate fi = LocalDate.parse(sc.nextLine().trim());
                        System.out.print("Fecha fin (YYYY-MM-DD): ");
                        LocalDate ff = LocalDate.parse(sc.nextLine().trim());
                        
                        List<Habitacion> disp = gestionHabitaciones.buscarHabitacionesDisponibles(fi, ff);
                        System.out.println("Habitaciones disponibles:");
                        for (Habitacion h : disp) {
                            System.out.println(" - " + h.getNumero() 
                                    + " | " + h.getTipo() + " | " + h.getPrecio());
                        }
                    }
                    case "2" -> {
                        // Flujo de creación de reserva (sin cambios)
                        System.out.print("Ingrese Cédula del cliente (o vacío para buscar por ID): ");
                        String cedulaBuscar = sc.nextLine().trim();
                        Cliente clienteReserva = null;
                        
                        if (!cedulaBuscar.isEmpty()) {
                            clienteReserva = buscarClientePorCedula(cedulaBuscar);
                        } else {
                            System.out.print("Ingrese ID del cliente: ");
                            try {
                                int idCli = Integer.parseInt(sc.nextLine().trim());
                                clienteReserva = buscarClientePorId(idCli);
                            } catch (NumberFormatException nfe) {
                                System.out.println("ID inválido.");
                                break;
                            }
                        }
                        
                        if (clienteReserva == null) {
                            System.out.println("\n--- REGISTRO DE NUEVO CLIENTE ---");
                            String cedulaParaRegistro;
                            if (!cedulaBuscar.isEmpty()) {
                                cedulaParaRegistro = cedulaBuscar;
                                System.out.println("Cédula: " + 
                                        cedulaParaRegistro);
                            } else {
                                System.out.print("Cédula: ");
                                cedulaParaRegistro = sc.nextLine();
                            }
                            
                            System.out.print("Nombre: ");
                            String nNombre = sc.nextLine();
                            System.out.print("Email: ");
                            String nEmail = sc.nextLine();
                            System.out.print("Teléfono: ");
                            String nTel = sc.nextLine();
                            
                            clienteReserva = registrarCliente(new Cliente(0, 
                                    nNombre, cedulaParaRegistro, nEmail, nTel));
                            System.out.println("Cliente registrado con ID " 
                                    + clienteReserva.getId());
                        } else {
                            System.out.println("Cliente encontrado: " + clienteReserva.getNombre());
                        }
                        
                        System.out.print("Número habitación: ");
                        int num = Integer.parseInt(sc.nextLine().trim());
                        Habitacion hab = gestionHabitaciones.buscarHabitacionPorNumero(num);
                        
                        if (hab == null) {
                            System.out.println("Habitación no encontrada.");
                            break;
                        }
                        
                        System.out.print("Fecha inicio (YYYY-MM-DD): ");
                        LocalDate rfi = LocalDate.parse(sc.nextLine().trim());
                        System.out.print("Fecha fin (YYYY-MM-DD): ");
                        LocalDate rff = LocalDate.parse(sc.nextLine().trim());
                        System.out.print("Método de pago: ");
                        String mp = sc.nextLine();
                        
                        Reserva res = gestionReservas.crearReserva(rfi, rff, 
                                hab, mp, clienteReserva.getCedula());
                        System.out.println("✅ Reserva creada con ID " 
                                + res.getId() + " para cliente " + clienteReserva.getNombre());
                    }
                    case "3" -> {
                        System.out.print("ID reserva: ");
                        int idIn = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Cédula cliente (check-in): ");
                        String cIn = sc.nextLine();
                        gestionReservas.realizarCheckIn(idIn, cIn);
                        System.out.println("Check-in realizado.");
                    }
                    case "4" -> {
                        System.out.print("ID reserva: ");
                        int idOut = Integer.parseInt(sc.nextLine().trim());
                        gestionReservas.realizarCheckOut(idOut);
                        System.out.println("Check-out realizado.");
                    }
                    case "5" -> {
                        System.out.print("ID reserva a cancelar: ");
                        int idCanc = Integer.parseInt(sc.nextLine().trim());
                        gestionReservas.cancelarReserva(idCanc);
                        System.out.println("Reserva cancelada.");
                    }
                    case "6" -> {
                        System.out.print("ID reserva para facturar: ");
                        int idFact = Integer.parseInt(sc.nextLine().trim());
                        Reserva rr = gestionReservas.buscarReservaPorId(idFact);
                        if (rr == null) {
                            System.out.println("Reserva no encontrada.");
                            break;
                        }
                        System.out.print("Porcentaje IVA (ej. 0.19): ");
                        double iva = Double.parseDouble(sc.nextLine().trim());
                        Factura f = generarFactura(rr, iva);
                        f.generarFactura();
                        System.out.println("Factura generada con ID " + f.getId());
                    }
                    case "7" -> {
                        System.out.print("Ingrese Cédula del cliente: ");
                        String cedCliente = sc.nextLine().trim();
                        Cliente cli = buscarClientePorCedula(cedCliente);
                        if (cli == null) {
                            System.out.println("Cliente no encontrado.");
                            break;
                        }
                        System.out.println("\n--- CLIENTE ---");
                        System.out.println("ID: " + cli.getId());
                        System.out.println("Nombre: " + cli.getNombre());
                        System.out.println("Cédula: " + cli.getCedula());
                        System.out.println("Email: " + cli.getEmail());
                        System.out.println("Teléfono: " + cli.getTelefono());

                        List<Reserva> resCliente = gestionReservas.
                                getReservasDeClientePorCedula(cli.getCedula());
                        System.out.println("\nReservas del cliente:");
                        if (resCliente.isEmpty()) {
                            System.out.println("  (sin reservas)");
                        } else {
                            for (Reserva rc : resCliente) {
                                System.out.println(" - ID " + rc.getId() + 
                                        " | Hab " + rc.getHabitacion().getNumero() +
                                        " | " + rc.getFechaInicio() + 
                                        " a " + rc.getFechaFin() + " | " + rc.getEstado());
                            }
                        }

                        List<Factura> facCliente = facturasDeClientePorCedula(cli.getCedula());
                        System.out.println("\nFacturas relacionadas:");
                        if (facCliente.isEmpty()) {
                            System.out.println("  (sin facturas)");
                        } else {
                            for (Factura fx : facCliente) {
                                System.out.println(" - ID " + fx.getId() + " | Fecha: " + fx.getFecha() + 
                                        " | Total: " + fx.getTotal() + " | Pagada: " + fx.estaPagada());
                            }
                        }
                    }
                    case "8" -> modoAdministrador();
                    case "9" -> {
                        System.out.println("Saliendo...");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (DateTimeParseException dtpe) {
                System.out.println("Formato de fecha incorrecto: " + dtpe.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ===== GETTERS =====
    public Hotel getHotel() { return datos.getHotel(); }
    public List<Cliente> getClientes() { return datos.getClientes(); }
    public List<Reserva> getReservas() { return datos.getReservas(); }
    public List<Factura> getFacturas() { return datos.getFacturas(); }
    public Administrador getAdministrador() { return administrador; }
    public GestionHabitaciones getGestionHabitaciones() { return gestionHabitaciones; }
    public GestionReservas getGestionReservas() { return gestionReservas; }
}