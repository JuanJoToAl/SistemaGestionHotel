package com.mycompany.sistemagestionhotel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PersistenciaDatos {
    private static final Path DATA_DIR = Paths.get("datos");
    private static final Path HOTEL_FILE = DATA_DIR.resolve("hotel.csv");
    private static final Path HABITACIONES_FILE = DATA_DIR.resolve("habitaciones.csv");
    private static final Path CLIENTES_FILE = DATA_DIR.resolve("clientes.csv");
    private static final Path CLIENTES_RESERVAS_FILE = DATA_DIR.resolve("clientes_reservas.csv");
    private static final Path FACTURAS_FILE = DATA_DIR.resolve("facturas.csv");

    // ================= CARGA DE DATOS (AHORA PÚBLICOS) =================

    public Hotel cargarHotel() throws IOException {
        if (!Files.exists(HOTEL_FILE)) return null;
        List<String> lines = Files.readAllLines(HOTEL_FILE, 
                StandardCharsets.UTF_8);
        if (lines.isEmpty()) return null;
        String[] partes = lines.get(0).split(";", -1);
        return new Hotel(partes[0], partes.length > 1 ? partes[1] : "", 
                partes.length > 2 ? partes[2] : "");
    }

    public List<Habitacion> cargarHabitaciones() throws IOException {
        List<Habitacion> habitaciones = new ArrayList<>();
        if (!Files.exists(HABITACIONES_FILE)) return habitaciones;
        List<String> lines = Files.readAllLines(HABITACIONES_FILE, 
                StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try { habitaciones.add(Habitacion.fromCsv(linea)); } 
            catch (Exception e) { System.err.println("Error hab: " + e.getMessage()); }
        }
        return habitaciones;
    }

    public List<Cliente> cargarClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        if (!Files.exists(CLIENTES_FILE)) return clientes;
        List<String> lines = Files.readAllLines(CLIENTES_FILE, 
                StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try { clientes.add(Cliente.fromCsv(linea)); } 
            catch (Exception e) { System.err.println("Error cliente: " + e.getMessage()); }
        }
        return clientes;
    }

    // RENOMBRADO para coincidir con la llamada en SistemaGestionHotel
    public List<Reserva> cargarReservas(List<Habitacion> habitaciones, 
            List<Cliente> clientes) throws IOException {
        List<Reserva> reservas = new ArrayList<>();
        if (!Files.exists(CLIENTES_RESERVAS_FILE)) return reservas;

        List<String> lines = Files.readAllLines(CLIENTES_RESERVAS_FILE, 
                StandardCharsets.UTF_8);
        int startIndex = (!lines.isEmpty() && lines.get(0).contains("Cliente_ID")) ? 1 : 0;

        for (int i = startIndex; i < lines.size(); i++) {
            String linea = lines.get(i);
            if (linea.trim().isEmpty()) continue;
            try {
                String[] partes = linea.split(";");
                if (partes.length < 13) continue;

                int numeroHabitacion = Integer.parseInt(partes[8].trim());
                String cedulaCliente = partes[2].trim();
                
                // Búsqueda manual simple para no depender de otras clases
                Habitacion habitacion = null;
                for(Habitacion h : habitaciones) if(h.getNumero() 
                        == numeroHabitacion) habitacion = h;
                
                Cliente cliente = null;
                for(Cliente c : clientes) if(c.getCedula().equalsIgnoreCase(cedulaCliente)) cliente = c;

                if (habitacion != null && cliente != null) {
                    Reserva r = new Reserva(
                        Integer.parseInt(partes[5].trim()),
                        LocalDate.parse(partes[6].trim()),
                        LocalDate.parse(partes[7].trim()),
                        EstadoReserva.valueOf(partes[10].trim()),
                        habitacion, cedulaCliente, partes[11].trim()
                    );
                    reservas.add(r);
                }
            } catch (Exception e) { System.err.println("Error reserva: " 
                    + e.getMessage()); }
        }
        return reservas;
    }

    public List<Factura> cargarFacturas(List<Reserva> reservas) throws IOException {
        List<Factura> facturas = new ArrayList<>();
        if (!Files.exists(FACTURAS_FILE)) return facturas;
        List<String> lines = Files.readAllLines(FACTURAS_FILE, 
                StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try {
                String[] partes = linea.split(";", -1);
                int idReserva = Integer.parseInt(partes[6]);
                Reserva reserva = null;
                for(Reserva r : reservas) if(r.getId() == idReserva) reserva = r;
                
                if (reserva != null) facturas.add(Factura.fromCsv(linea, reserva));
            } catch (Exception e) { System.err.println("Error factura: " 
                    + e.getMessage()); }
        }
        return facturas;
    }

    // ================= GUARDADO DE DATOS =================

    public void guardarHotel(Hotel hotel) throws IOException {
        asegurarDirectorio();
        String linea = hotel.getNombre() + ";" + hotel.getDireccion() 
                + ";" + hotel.getTelefono();
        Files.write(HOTEL_FILE, Collections.singletonList(linea), 
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarHabitaciones(List<Habitacion> habitaciones) throws IOException {
        asegurarDirectorio();
        List<String> lines = habitaciones.stream().map(Habitacion::toCsv).collect(Collectors.toList());
        Files.write(HABITACIONES_FILE, lines, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); 
    }
    
    // Sobrecarga para aceptar objeto Hotel si es necesario
    public void guardarHabitaciones(Hotel hotel) throws IOException {
        guardarHabitaciones(hotel.getHabitaciones());
    }

    public void guardarClientes(List<Cliente> clientes) throws IOException {
        asegurarDirectorio();
        List<String> lines = clientes.stream().map(Cliente::toCsv).collect(Collectors.toList());
        Files.write(CLIENTES_FILE, lines, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarReservas(List<Reserva> reservas) throws IOException {
        // Guardado simple si es necesario, pero el importante es el unificado
    }

    public void guardarReservasUnificado(List<Cliente> clientes, 
            List<Reserva> reservas) throws IOException {
        asegurarDirectorio();
        List<String> lines = new ArrayList<>();
        
        // Mantener cabecera o crear nueva
        boolean existe = Files.exists(CLIENTES_RESERVAS_FILE);
        if (existe) {
             List<String> existentes = Files.readAllLines(CLIENTES_RESERVAS_FILE, 
                     StandardCharsets.UTF_8);
             if(!existentes.isEmpty() && existentes.get(0).contains("Cliente_ID")) 
                 lines.add(existentes.get(0));
             else lines.add("Cliente_ID;Nombre_Cliente;Cedula_Cliente;Email_Cliente;Telefono_Cliente;Reserva_ID;Fecha_Inicio_Reserva;Fecha_Fin_Reserva;Numero_Habitacion;Tipo_Habitacion;Estado_Reserva;Metodo_Pago;Total_Reserva");
        } else {
             lines.add("Cliente_ID;Nombre_Cliente;Cedula_Cliente;Email_Cliente;Telefono_Cliente;Reserva_ID;Fecha_Inicio_Reserva;Fecha_Fin_Reserva;Numero_Habitacion;Tipo_Habitacion;Estado_Reserva;Metodo_Pago;Total_Reserva");
        }

        for (Reserva r : reservas) {
            Cliente c = null;
            for(Cliente cli : clientes) if(cli.getCedula().equals(r.getCedulaCheckIn())) c = cli;
            
            if (c != null) {
                // Verificar si ya existe en el archivo (simple check por ID reserva)
                boolean yaEsta = false;
                if(existe) {
                     // Lógica simple: reescribimos todo para evitar duplicados complejos en este ejemplo rápido
                }
                
                String linea = String.format("%d;%s;%s;%s;%s;%d;%s;%s;%d;%s;%s;%s;%.2f",
                    c.getId(), escapeCsv(c.getNombre()), escapeCsv(c.getCedula()), 
                    escapeCsv(c.getEmail()), escapeCsv(c.getTelefono()),
                    r.getId(), r.getFechaInicio(), r.getFechaFin(), 
                    r.getHabitacion().getNumero(), escapeCsv(r.getHabitacion().getTipo()),
                    r.getEstado().name(), escapeCsv(r.getMetodoPago()), 
                    r.calcularTotal());
                lines.add(linea);
            }
        }
        Files.write(CLIENTES_RESERVAS_FILE, lines, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarFacturas(List<Factura> facturas) throws IOException {
        asegurarDirectorio();
        List<String> lines = facturas.stream().map(Factura::toCsv).collect(Collectors.toList());
        Files.write(FACTURAS_FILE, lines, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String escapeCsv(String campo) {
        if (campo == null) return "";
        if (campo.contains(";") || campo.contains("\"")) return "\"" 
                + campo.replace("\"", "\"\"") + "\"";
        return campo;
    }

    private void asegurarDirectorio() throws IOException {
        if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
    }
}