package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MenuConsola {
    private SistemaGestionHotel sistema;
    private Scanner scanner;

    public MenuConsola(SistemaGestionHotel sistema) {
        this.sistema = sistema;
        this.scanner = new Scanner(System.in);
    }

    // ==========================================
    // MÉTODO MAIN (PUNTO DE ENTRADA)
    // ==========================================
    public static void main(String[] args) {
        SistemaGestionHotel sistema = new SistemaGestionHotel();
        MenuConsola menu = new MenuConsola(sistema);
        menu.iniciar();
    }

    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerInt(); // Usamos el método auxiliar leerInt para evitar errores

            try {
                switch (opcion) {
                    case 1 -> manejarListarDisponibles();
                    case 2 -> manejarCrearReserva();
                    case 3 -> manejarCheckIn();
                    case 4 -> manejarCheckOut();
                    case 5 -> manejarCancelarReserva();
                    case 6 -> manejarGenerarFactura();
                    case 7 -> manejarPagarFactura();
                    case 8 -> manejarVerCliente();
                    case 9 -> manejarAdministrador();
                    case 10 -> {
                        System.out.println("Saliendo...");
                        salir = true;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.err.println("Error en la operación: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void mostrarMenu() {
        System.out.println("\n==== SISTEMA GESTIÓN HOTEL ====");
        System.out.println("1) Listar habitaciones disponibles");
        System.out.println("2) Crear reserva");
        System.out.println("3) Realizar check-in");
        System.out.println("4) Realizar check-out");
        System.out.println("5) Cancelar reserva");
        System.out.println("6) Generar factura");
        System.out.println("7) Pagar factura");
        System.out.println("8) Ver cliente");
        System.out.println("9) Administrador");
        System.out.println("10) Salir");
        System.out.print("Elige una opción: ");
    }

    // --- Métodos de Manejo del Menú Principal ---

    private void manejarListarDisponibles() {
        LocalDate fi = leerFecha("Fecha inicio (YYYY-MM-DD): ");
        LocalDate ff = leerFecha("Fecha fin (YYYY-MM-DD): ");
        if (fi == null || ff == null) return;

        List<Habitacion> disp = sistema.getGestionHabitaciones().buscarHabitacionesDisponibles(fi, ff);
        System.out.println("Habitaciones disponibles:");
        if(disp.isEmpty()) System.out.println("No hay habitaciones disponibles.");
        for (Habitacion h : disp) {
            System.out.printf(" - %d | %s | %.0f\n", h.getNumero(), h.getTipo(), h.getPrecio());
        }
    }

    private void manejarCrearReserva() {
        System.out.print("Cédula cliente (vacío para ID): ");
        String cedula = scanner.nextLine().trim();
        Cliente cliente = null;

        if (!cedula.isEmpty()) {
            cliente = sistema.getGestionClientes().buscarClientePorCedula(cedula);
        } else {
            System.out.print("ID cliente: ");
            int id = leerInt();
            if (id != -1) cliente = sistema.getGestionClientes().buscarClientePorId(id);
        }

        if (cliente == null) {
            System.out.println("Cliente no encontrado. Registrando nuevo...");
            cliente = registrarNuevoCliente(cedula.isEmpty() ? "SD" : cedula);
            if (cliente == null) return;
        } else {
            System.out.println("Cliente: " + cliente.getNombre());
        }

        System.out.print("Número habitación: ");
        int numHab = leerInt();
        Habitacion hab = sistema.getGestionHabitaciones().buscarHabitacionPorNumero(numHab);
        if (hab == null) { System.out.println("Habitación no existe."); return; }

        LocalDate fi = leerFecha("Fecha inicio (YYYY-MM-DD): ");
        LocalDate ff = leerFecha("Fecha fin (YYYY-MM-DD): ");
        if (fi == null || ff == null) return;
        
        System.out.print("Método de pago: ");
        String pago = scanner.nextLine();

        try {
            Reserva r = sistema.getGestionReservas().crearReserva(fi, ff, hab, pago, cliente.getCedula());
            System.out.println("✅ Reserva creada ID: " + r.getId());
        } catch (Exception e) {
            System.out.println("Error al reservar: " + e.getMessage());
        }
    }

    private Cliente registrarNuevoCliente(String cedulaSugerida) {
        String cedula = cedulaSugerida;
        if(cedula.equals("SD")) {
             System.out.print("Cédula: ");
             cedula = scanner.nextLine();
        }
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String tel = scanner.nextLine();
        
        return sistema.registrarCliente(new Cliente(0, nombre, cedula, email, tel));
    }

    private void manejarCheckIn() {
        System.out.print("ID Reserva: ");
        int id = leerInt();
        System.out.print("Cédula Cliente: ");
        String cedula = scanner.nextLine();
        try {
            sistema.getGestionReservas().realizarCheckIn(id, cedula);
            System.out.println("Check-in exitoso.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manejarCheckOut() {
        System.out.print("ID Reserva: ");
        int id = leerInt();
        try {
            sistema.getGestionReservas().realizarCheckOut(id);
            System.out.println("Check-out exitoso.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manejarCancelarReserva() {
        System.out.print("ID Reserva: ");
        int id = leerInt();
        try {
            sistema.getGestionReservas().cancelarReserva(id);
            System.out.println("Reserva cancelada.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manejarGenerarFactura() {
        System.out.print("ID Reserva: ");
        int id = leerInt();
        System.out.print("IVA (ej: 0.19): ");
        double iva = leerDouble();
        
        Reserva r = sistema.getGestionReservas().buscarReservaPorId(id);
        if (r == null) { System.out.println("Reserva no encontrada."); return; }
        
        try {
            Factura f = sistema.generarFactura(r, iva);
            System.out.println("Factura generada ID: " + f.getId() + " Total: " + f.getTotal());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manejarPagarFactura() {
        System.out.print("ID Reserva asociada a la factura: ");
        int id = leerInt();
        try {
            sistema.marcarFacturaComoPagada(id);
            System.out.println("Factura pagada correctamente.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manejarVerCliente() {
        System.out.print("Cédula: ");
        String cedula = scanner.nextLine();
        Cliente c = sistema.buscarClientePorCedula(cedula);
        if (c == null) { System.out.println("Cliente no encontrado."); return; }
        
        System.out.println("--- Cliente: " + c.getNombre() + " ---");
        System.out.println("Reservas:");
        sistema.getGestionReservas().getReservasDeClientePorCedula(cedula)
               .forEach(r -> System.out.println(" - ID:" + r.getId() + " Est:" + r.getEstado()));
        
        System.out.println("Facturas:");
        sistema.getFacturasDeClientePorCedula(cedula)
               .forEach(f -> System.out.println(" - ID:" + f.getId() + " Pagada:" + f.estaPagada() + " Total:" + f.getTotal()));
    }

    // --- Métodos de Admin ---

    private void manejarAdministrador() {
        System.out.print("Usuario: ");
        String u = scanner.nextLine();
        System.out.print("Clave: ");
        String p = scanner.nextLine();
        
        if (!sistema.getAdministrador().getUsuario().equals(u) || !sistema.getAdministrador().getContraseña().equals(p)) {
            System.out.println("Credenciales incorrectas.");
            return;
        }
        
        boolean salirAdmin = false;
        while (!salirAdmin) {
            System.out.println("\n--- ADMIN ---");
            System.out.println("1) Listar Todo (Hab/Res/Fact)");
            System.out.println("2) Agregar Habitación");
            System.out.println("3) Borrar Cliente");
            System.out.println("4) Borrar Reserva");
            System.out.println("0) Volver");
            System.out.print("Opción: ");
            
            int op = leerInt();
            switch (op) {
                case 1 -> listarTodoAdmin();
                case 2 -> agregarHabitacionAdmin();
                case 3 -> borrarClienteAdmin();
                case 4 -> borrarReservaAdmin();
                case 0 -> salirAdmin = true;
            }
        }
    }
    
    private void listarTodoAdmin() {
        System.out.println("--- Habitaciones ---");
        sistema.getGestionHabitaciones().getTodasHabitaciones().forEach(h -> System.out.println(h.getNumero() + " " + h.getEstado()));
        System.out.println("--- Reservas ---");
        sistema.getReservas().forEach(r -> System.out.println(r.getId() + " " + r.getEstado()));
        System.out.println("--- Facturas ---");
        sistema.getFacturas().forEach(f -> System.out.println(f.getId() + " Total: " + f.getTotal()));
    }

    private void agregarHabitacionAdmin() {
        System.out.print("Número: "); int num = leerInt();
        System.out.print("Tipo: "); String tipo = scanner.nextLine();
        System.out.print("Precio: "); double precio = leerDouble();
        sistema.agregarHabitacion(new Habitacion(num, tipo, precio, EstadoHabitacion.DISPONIBLE));
        System.out.println("Habitación agregada.");
    }

    private void borrarClienteAdmin() {
        System.out.print("Cédula a borrar: ");
        String cedula = scanner.nextLine();
        try {
            sistema.getGestionClientes().borrarCliente(cedula); // Asegúrate de haber agregado este método en GestionClientes
            System.out.println("Cliente borrado.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void borrarReservaAdmin() {
        System.out.print("ID Reserva a borrar: ");
        int id = leerInt();
        try {
            sistema.getGestionReservas().borrarReserva(id); // Asegúrate de haber agregado este método en GestionReservas
            System.out.println("Reserva borrada.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- Helpers ---

    private int leerInt() {
        try {
            int i = Integer.parseInt(scanner.nextLine());
            return i;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private double leerDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private LocalDate leerFecha(String msg) {
        System.out.print(msg);
        try {
            return LocalDate.parse(scanner.nextLine());
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida.");
            return null;
        }
    }
}