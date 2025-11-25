package com.mycompany.sistemagestionhotel;

import java.util.ArrayList;
import java.util.List;

public class DatosHotel {
    private Hotel hotel;
    private List<Habitacion> habitaciones;
    private List<Cliente> clientes;
    private List<Reserva> reservas;
    private List<Factura> facturas;
    private int nextClienteId = 1;
    private int nextReservaId = 1;
    private int nextFacturaId = 1;

    public DatosHotel() {
        this.habitaciones = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.facturas = new ArrayList<>();
    }

    // Getters y Setters
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public List<Habitacion> getHabitaciones() { return new ArrayList<>(habitaciones); }
    public void setHabitaciones(List<Habitacion> habitaciones) { this.habitaciones = new ArrayList<>(habitaciones); }

    public List<Cliente> getClientes() { return new ArrayList<>(clientes); }
    public void setClientes(List<Cliente> clientes) { this.clientes = new ArrayList<>(clientes); }

    public List<Reserva> getReservas() { return new ArrayList<>(reservas); }
    public void setReservas(List<Reserva> reservas) { this.reservas = new ArrayList<>(reservas); }

    public List<Factura> getFacturas() { return new ArrayList<>(facturas); }
    public void setFacturas(List<Factura> facturas) { this.facturas = new ArrayList<>(facturas); }

    public int getNextClienteId() { return nextClienteId; }
    public void setNextClienteId(int nextClienteId) { this.nextClienteId = nextClienteId; }

    public int getNextReservaId() { return nextReservaId; }
    public void setNextReservaId(int nextReservaId) { this.nextReservaId = nextReservaId; }

    public int getNextFacturaId() { return nextFacturaId; }
    public void setNextFacturaId(int nextFacturaId) { this.nextFacturaId = nextFacturaId; }
}