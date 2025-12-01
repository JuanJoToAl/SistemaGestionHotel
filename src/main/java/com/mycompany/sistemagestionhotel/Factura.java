package com.mycompany.sistemagestionhotel;

import java.time.LocalDate;

public class Factura {
    private int id;
    private LocalDate fecha;
    private double subtotal;
    private double iva;
    private double total;
    private String metodoPago;
    private Reserva reserva;
    private boolean pagada;

    public Factura(int id,
                   LocalDate fecha,
                   double subtotal,
                   double iva,
                   double total,
                   String metodoPago,
                   Reserva reserva) {
        this.id = id;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.metodoPago = metodoPago;
        this.reserva = reserva;
        this.pagada = false;
    }

    // ===== Métodos del UML =====

    /**
     * Calcula el IVA y actualiza el total de la factura.
     * @param porcentajeIVA porcentaje en forma decimal 
     * (por ejemplo 0.19 para 19%).
     */
    public void calcularIVA(double porcentajeIVA) {
        this.iva = subtotal * porcentajeIVA;
        this.total = subtotal + iva;
    }

    /**
     * Genera/imprime la factura en consola. En una app real podría 
     * enviar a impresora o PDF.
     */
    public void generarFactura() {
        System.out.println("\n=== FACTURA N° " + id + " ===");
        System.out.println("Fecha: " + fecha);
        System.out.println("Reserva ID: " + reserva.getId());
        System.out.println("Habitación: " + reserva.getHabitacion().getNumero());
        System.out.println("Subtotal: " + subtotal);
        System.out.println("IVA: " + iva);
        System.out.println("Total: " + total);
        System.out.println("Método de pago: " + metodoPago);
        System.out.println("Estado de pago: " + (pagada ? "PAGADA" : "PENDIENTE"));
        System.out.println("========================\n");
    }

    public boolean estaPagada() {
        return pagada;
    }

    public void pagar() {
        this.pagada = true;
    }

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getIva() {
        return iva;
    }

    public double getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public Reserva getReserva() {
        return reserva;
    }

    // Persistencia sencilla a CSV
    // Formato: id;fecha;subtotal;iva;total;metodoPago;idReserva;pagada
    public String toCsv() {
        return id + ";" + fecha + ";" + subtotal + ";" + iva + ";" + 
                total + ";" + metodoPago + ";" + reserva.getId() + 
                ";" + pagada;
    }

    public static Factura fromCsv(String linea, Reserva reserva) {
        String[] p = linea.split(";");
        int id = Integer.parseInt(p[0]);
        LocalDate fecha = LocalDate.parse(p[1]);
        double subtotal = Double.parseDouble(p[2]);
        double iva = Double.parseDouble(p[3]);
        double total = Double.parseDouble(p[4]);
        String metodoPago = p[5];
        boolean pagada = Boolean.parseBoolean(p[7]);
        Factura f = new Factura(id, fecha, subtotal, iva, total, 
                metodoPago, reserva);
        if (pagada) {
            f.pagar();
        }
        return f;
    }
}
