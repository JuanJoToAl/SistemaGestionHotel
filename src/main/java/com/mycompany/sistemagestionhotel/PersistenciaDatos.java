package com.mycompany.sistemagestionhotel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PersistenciaDatos {
    private static final Path DATA_DIR = Paths.get("datos");
    private static final Path HOTEL_FILE = DATA_DIR.resolve("hotel.csv");
    private static final Path HABITACIONES_FILE = DATA_DIR.resolve("habitaciones.csv");
    private static final Path CLIENTES_FILE = DATA_DIR.resolve("clientes.csv");
    private static final Path RESERVAS_FILE = DATA_DIR.resolve("reservas.csv");
    private static final Path FACTURAS_FILE = DATA_DIR.resolve("facturas.csv");

    public DatosHotel cargarDatos() {
        DatosHotel datos = new DatosHotel();
        
        try {
            if (!Files.exists(DATA_DIR)) {
                return null; // No hay datos previos
            }

            // Cargar hotel
            datos.setHotel(cargarHotel());
            
            // Cargar habitaciones
            datos.setHabitaciones(cargarHabitaciones());
            
            // Cargar clientes
            datos.setClientes(cargarClientes());
            datos.setNextClienteId(calcularNextId(datos.getClientes().stream().map(Cliente::getId).collect(Collectors.toList())));
            
            // Cargar reservas (necesita habitaciones ya cargadas)
            datos.setReservas(cargarReservas(datos.getHabitaciones()));
            datos.setNextReservaId(calcularNextId(datos.getReservas().stream().map(Reserva::getId).collect(Collectors.toList())));
            
            // Cargar facturas (necesita reservas ya cargadas)
            datos.setFacturas(cargarFacturas(datos.getReservas()));
            datos.setNextFacturaId(calcularNextId(datos.getFacturas().stream().map(Factura::getId).collect(Collectors.toList())));

            return datos;

        } catch (IOException e) {
            System.err.println("Error cargando datos: " + e.getMessage());
            return null;
        }
    }

    private Hotel cargarHotel() throws IOException {
        if (!Files.exists(HOTEL_FILE)) {
            return new Hotel("Hotel Paradise", "Calle Principal 123", "+57 1 2345678");
        }

        List<String> lines = Files.readAllLines(HOTEL_FILE, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return new Hotel("Hotel Paradise", "Calle Principal 123", "+57 1 2345678");
        }

        String[] partes = lines.get(0).split(";", -1);
        String nombre = partes.length > 0 ? partes[0] : "Hotel Paradise";
        String direccion = partes.length > 1 ? partes[1] : "Calle Principal 123";
        String telefono = partes.length > 2 ? partes[2] : "+57 1 2345678";
        
        return new Hotel(nombre, direccion, telefono);
    }

    private List<Habitacion> cargarHabitaciones() throws IOException {
        List<Habitacion> habitaciones = new ArrayList<>();
        
        if (!Files.exists(HABITACIONES_FILE)) {
            return habitaciones;
        }

        List<String> lines = Files.readAllLines(HABITACIONES_FILE, StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try {
                Habitacion h = Habitacion.fromCsv(linea);
                habitaciones.add(h);
            } catch (Exception e) {
                System.err.println("Error leyendo habitación: " + linea + " -> " + e.getMessage());
            }
        }
        
        return habitaciones;
    }

    private List<Cliente> cargarClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        
        if (!Files.exists(CLIENTES_FILE)) {
            return clientes;
        }

        List<String> lines = Files.readAllLines(CLIENTES_FILE, StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try {
                Cliente c = Cliente.fromCsv(linea);
                clientes.add(c);
            } catch (Exception e) {
                System.err.println("Error leyendo cliente: " + linea + " -> " + e.getMessage());
            }
        }
        
        return clientes;
    }

    private List<Reserva> cargarReservas(List<Habitacion> habitaciones) throws IOException {
        List<Reserva> reservas = new ArrayList<>();
        
        if (!Files.exists(RESERVAS_FILE)) {
            return reservas;
        }

        List<String> lines = Files.readAllLines(RESERVAS_FILE, StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try {
                String[] partes = linea.split(";", -1);
                int numeroHab = Integer.parseInt(partes[4]);
                Habitacion hab = buscarHabitacionPorNumero(habitaciones, numeroHab);
                if (hab == null) {
                    System.err.println("Reserva referenciada a habitación inexistente: " + numeroHab);
                    continue;
                }
                Reserva r = Reserva.fromCsv(linea, hab);
                reservas.add(r);
            } catch (Exception e) {
                System.err.println("Error leyendo reserva: " + linea + " -> " + e.getMessage());
            }
        }
        
        return reservas;
    }

    private List<Factura> cargarFacturas(List<Reserva> reservas) throws IOException {
        List<Factura> facturas = new ArrayList<>();
        
        if (!Files.exists(FACTURAS_FILE)) {
            return facturas;
        }

        List<String> lines = Files.readAllLines(FACTURAS_FILE, StandardCharsets.UTF_8);
        for (String linea : lines) {
            if (linea.trim().isEmpty()) continue;
            try {
                String[] partes = linea.split(";", -1);
                int idReserva = Integer.parseInt(partes[6]);
                Reserva reserva = buscarReservaPorId(reservas, idReserva);
                if (reserva == null) {
                    System.err.println("Factura referenciada a reserva inexistente: " + idReserva);
                    continue;
                }
                Factura f = Factura.fromCsv(linea, reserva);
                facturas.add(f);
            } catch (Exception e) {
                System.err.println("Error leyendo factura: " + linea + " -> " + e.getMessage());
            }
        }
        
        return facturas;
    }

    // Métodos de guardado
    public void guardarHotel(Hotel hotel) throws IOException {
        asegurarDirectorio();
        String linea = hotel.getNombre() + ";" + hotel.getDireccion() + ";" + hotel.getTelefono();
        Files.write(HOTEL_FILE, Collections.singletonList(linea), StandardCharsets.UTF_8, 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarHabitaciones(List<Habitacion> habitaciones) throws IOException {
        asegurarDirectorio();
        List<String> lines = habitaciones.stream()
                .map(Habitacion::toCsv)
                .collect(Collectors.toList());
        Files.write(HABITACIONES_FILE, lines, StandardCharsets.UTF_8, 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarClientes(List<Cliente> clientes) throws IOException {
        asegurarDirectorio();
        List<String> lines = clientes.stream()
                .map(Cliente::toCsv)
                .collect(Collectors.toList());
        Files.write(CLIENTES_FILE, lines, StandardCharsets.UTF_8, 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarReservas(List<Reserva> reservas) throws IOException {
        asegurarDirectorio();
        List<String> lines = reservas.stream()
                .map(Reserva::toCsv)
                .collect(Collectors.toList());
        Files.write(RESERVAS_FILE, lines, StandardCharsets.UTF_8, 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void guardarFacturas(List<Factura> facturas) throws IOException {
        asegurarDirectorio();
        List<String> lines = facturas.stream()
                .map(Factura::toCsv)
                .collect(Collectors.toList());
        Files.write(FACTURAS_FILE, lines, StandardCharsets.UTF_8, 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Métodos auxiliares
    private void asegurarDirectorio() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
    }

    private Habitacion buscarHabitacionPorNumero(List<Habitacion> habitaciones, int numero) {
        for (Habitacion h : habitaciones) {
            if (h.getNumero() == numero) return h;
        }
        return null;
    }

    private Reserva buscarReservaPorId(List<Reserva> reservas, int id) {
        for (Reserva r : reservas) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    private int calcularNextId(List<Integer> ids) {
        return ids.stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }
}