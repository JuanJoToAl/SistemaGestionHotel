package com.mycompany.sistemagestionhotel.ui;

import com.mycompany.sistemagestionhotel.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class HotelGUI extends JFrame {

    private final SistemaGestionHotel sistema;

    // Componentes principales
    private JTabbedPane tabs;
    private JPanel panelClientes;
    private JPanel panelHabitaciones;
    private JPanel panelMisReservas;
    private JPanel panelAdmin;

    // Cliente actualmente autenticado (por cédula)
    private Cliente clienteActual;

    // Modelos de tablas para poder refrescar desde cualquier parte
    private DefaultTableModel clientesModel;
    private JTable clientesTable;

    private DefaultTableModel habitacionesModel;
    private JTable habitacionesTable;

    private DefaultTableModel reservasModel;
    private JTable reservasTable;

    public HotelGUI(SistemaGestionHotel sistema) {
        this.sistema = sistema;

        setTitle("Sistema de Gestión Hotelera");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabs = new JTabbedPane();

        // Crear paneles funcionales
        panelClientes = crearPanelClientes(); // solo para uso interno (admin)
        panelAdmin = crearPanelAdmin();

        // Panel principal de inicio de sesión de cliente / acceso admin
        JPanel panelInicio = crearPanelInicio();

        // Paneles específicos para cliente
        panelHabitaciones = crearPanelHabitaciones();
        panelMisReservas = crearPanelMisReservas();

        tabs.add("Inicio", panelInicio);
        tabs.add("Habitaciones", panelHabitaciones);
        tabs.add("Mis Reservas", panelMisReservas);
        tabs.add("Administrador", panelAdmin);

        add(tabs);

        // Cargar datos iniciales en las tablas
        refrescarTablaClientes();
        refrescarTablaHabitaciones();
        refrescarTablaReservas();

        setVisible(true);
    }

    // =============================
    // PANEL INICIO (LOGIN CLIENTE + ACCESO ADMIN)
    // =============================
    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout());

        // Barra superior con título y botón de acceso admin a la derecha
        JPanel top = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Sistema de Gestión Hotelera", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        top.add(titulo, BorderLayout.CENTER);

        JButton btnAdmin = new JButton("Acceso administrador");
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        adminPanel.add(btnAdmin);
        top.add(adminPanel, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);

        // Panel central: login de cliente por cédula
        JPanel loginPanel = new JPanel();
        loginPanel.setBorder(BorderFactory.createTitledBorder("Acceso de clientes"));
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel lblInfo = new JLabel("Ingrese su cédula para ver sus reservas y realizar nuevas.");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(lblInfo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        loginPanel.add(new JLabel("Cédula:"), gbc);

        JTextField cedulaField = new JTextField(12);
        gbc.gridx = 1;
        loginPanel.add(cedulaField, gbc);

        JButton btnAcceder = new JButton("Acceder");
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        loginPanel.add(btnAcceder, gbc);

        panel.add(loginPanel, BorderLayout.CENTER);

        // Acción de acceso cliente
        btnAcceder.addActionListener(e -> {
            String cedula = cedulaField.getText().trim();
            if (cedula.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar una cédula", "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente c = sistema.buscarClientePorCedula(cedula);
            if (c == null) {
                int resp = JOptionPane.showConfirmDialog(this,
                        "No se encontró un cliente con esa cédula.\n" +
                        "¿Desea registrarse?", "Registro de cliente",
                        JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) {
                    return;
                }

                String nombre = JOptionPane.showInputDialog(this, "Nombre completo:");
                if (nombre == null || nombre.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Registro cancelado", "Aviso",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String email = JOptionPane.showInputDialog(this, "Email (opcional):");
                String tel = JOptionPane.showInputDialog(this, "Teléfono (opcional):");

                c = sistema.registrarCliente(new Cliente(
                        0,
                        nombre.trim(),
                        cedula,
                        email == null ? "" : email.trim(),
                        tel == null ? "" : tel.trim()
                ));
            }

            clienteActual = c;
            JOptionPane.showMessageDialog(this,
                    "Bienvenido, " + c.getNombre(), "Acceso concedido",
                    JOptionPane.INFORMATION_MESSAGE);

            refrescarTablaHabitaciones();
            refrescarTablaReservas();
            tabs.setSelectedComponent(panelHabitaciones);
        });

        // Acción de acceso admin: lleva a la pestaña Administrador, donde se hace el login real
        btnAdmin.addActionListener(e -> tabs.setSelectedComponent(panelAdmin));

        return panel;
    }

    // =============================
    // PANEL CLIENTES
    // =============================
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout());

        clientesModel = new DefaultTableModel(
                new String[]{"Cédula", "Nombre", "Email", "Teléfono"}, 0
        );
        clientesTable = new JTable(clientesModel);
        JScrollPane scroll = new JScrollPane(clientesTable);

        // ----- Alta de cliente -----
        JPanel altaPanel = new JPanel();
        JTextField ced = new JTextField(8);
        JTextField nom = new JTextField(10);
        JTextField email = new JTextField(12);
        JTextField tel = new JTextField(8);
        JButton add = new JButton("Registrar");

        altaPanel.add(new JLabel("Cédula:")); altaPanel.add(ced);
        altaPanel.add(new JLabel("Nombre:")); altaPanel.add(nom);
        altaPanel.add(new JLabel("Email:")); altaPanel.add(email);
        altaPanel.add(new JLabel("Teléfono:")); altaPanel.add(tel);
        altaPanel.add(add);

        add.addActionListener(e -> {
            try {
                String cedula = ced.getText().trim();
                String nombre = nom.getText().trim();
                String correo = email.getText().trim();
                String telefono = tel.getText().trim();

                if (cedula.isEmpty() || nombre.isEmpty()) {
                    throw new IllegalArgumentException("Cédula y nombre son obligatorios");
                }

                Cliente nuevo = new Cliente(0, nombre, cedula, correo, telefono);
                sistema.registrarCliente(nuevo);
                refrescarTablaClientes();

                ced.setText("");
                nom.setText("");
                email.setText("");
                tel.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ----- Ver cliente (equivalente a manejarVerCliente) -----
        JPanel verPanel = new JPanel(new BorderLayout());
        JPanel verTop = new JPanel();
        JTextField cedBuscar = new JTextField(10);
        JButton btnVer = new JButton("Ver cliente");
        verTop.add(new JLabel("Cédula:"));
        verTop.add(cedBuscar);
        verTop.add(btnVer);

        JTextArea infoClienteArea = new JTextArea(6, 60);
        infoClienteArea.setEditable(false);
        JScrollPane infoScroll = new JScrollPane(infoClienteArea);

        btnVer.addActionListener(e -> {
            String cedula = cedBuscar.getText().trim();
            if (cedula.isEmpty()) return;

            Cliente c = sistema.buscarClientePorCedula(cedula);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Cliente no encontrado", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Cliente: ").append(c.getNombre()).append(" (ID ")
                    .append(c.getId()).append(")\n");
            sb.append("Cédula: ").append(c.getCedula()).append("\n");
            sb.append("Email: ").append(c.getEmail()).append("\n");
            sb.append("Teléfono: ").append(c.getTelefono()).append("\n\n");

            sb.append("Reservas:\n");
            for (Reserva r : sistema.getGestionReservas()
                    .getReservasDeClientePorCedula(cedula)) {
                sb.append(" - ID ").append(r.getId())
                        .append(" Estado: ").append(r.getEstado())
                        .append(" Hab: ").append(r.getHabitacion().getNumero())
                        .append(" Inicio: ").append(r.getFechaInicio())
                        .append(" Fin: ").append(r.getFechaFin())
                        .append("\n");
            }

            sb.append("\nFacturas:\n");
            for (Factura f : sistema.getFacturasDeClientePorCedula(cedula)) {
                sb.append(" - ID ").append(f.getId())
                        .append(" Total: ").append(f.getTotal())
                        .append(" Pagada: ").append(f.estaPagada())
                        .append("\n");
            }

            infoClienteArea.setText(sb.toString());
        });

        verPanel.add(verTop, BorderLayout.NORTH);
        verPanel.add(infoScroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(altaPanel, BorderLayout.NORTH);
        sur.add(verPanel, BorderLayout.CENTER);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    private void refrescarTablaClientes() {
        clientesModel.setRowCount(0);
        for (Cliente c : sistema.getClientes()) {
            clientesModel.addRow(new Object[]{
                    c.getCedula(), c.getNombre(), c.getEmail(), c.getTelefono()
            });
        }
    }

    // =============================
    // PANEL HABITACIONES (lista + creación de reservas)
    // =============================
    private JPanel crearPanelHabitaciones() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tabla de habitaciones
        habitacionesModel = new DefaultTableModel(
                new String[]{"Número", "Tipo", "Precio", "Estado"}, 0
        );
        habitacionesTable = new JTable(habitacionesModel);
        JScrollPane scrollHab = new JScrollPane(habitacionesTable);

        // Controles organizados en columnas
        JPanel controles = new JPanel();
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));

        // --- Crear reserva ---
        JPanel crearPanel = new JPanel();
        crearPanel.setBorder(BorderFactory.createTitledBorder("Crear reserva"));
        JLabel lblCliente = new JLabel("Cliente: (inicie sesión)");
        JTextField hab = new JTextField(4);
        JTextField inicio = new JTextField(8);
        JTextField fin = new JTextField(8);
        JTextField pago = new JTextField(8);
        JButton btnCrear = new JButton("Crear");

        crearPanel.add(lblCliente);
        crearPanel.add(new JLabel("Hab:")); crearPanel.add(hab);
        crearPanel.add(new JLabel("Inicio (YYYY-MM-DD):")); crearPanel.add(inicio);
        crearPanel.add(new JLabel("Fin:")); crearPanel.add(fin);
        crearPanel.add(new JLabel("Pago:")); crearPanel.add(pago);
        crearPanel.add(btnCrear);

        btnCrear.addActionListener(e -> {
            try {
                if (clienteActual == null) {
                    throw new IllegalStateException("Primero debe iniciar sesión en la pestaña Inicio.");
                }
                lblCliente.setText("Cliente: " + clienteActual.getNombre() + " (" + clienteActual.getCedula() + ")");

                int numeroHab = Integer.parseInt(hab.getText().trim());
                LocalDate fi = LocalDate.parse(inicio.getText().trim());
                LocalDate ff = LocalDate.parse(fin.getText().trim());
                
                if (fi.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a hoy.");
                }

                String metodoPago = pago.getText().trim();

                Cliente cliente = clienteActual;

                Habitacion habitacion = sistema.getGestionHabitaciones()
                        .buscarHabitacionPorNumero(numeroHab);
                if (habitacion == null) {
                    throw new IllegalArgumentException("Habitación no encontrada");
                }

                Reserva r = sistema.getGestionReservas().crearReserva(
                        fi, ff, habitacion, metodoPago, cliente.getCedula());

                refrescarTablaReservas();

                hab.setText("");
                inicio.setText("");
                fin.setText("");
                pago.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número de habitación inválido",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido (use YYYY-MM-DD)",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Listar habitaciones disponibles (todas) ---
        JPanel dispPanel = new JPanel();
        dispPanel.setBorder(BorderFactory.createTitledBorder("Habitaciones disponibles"));
        JButton btnDisp = new JButton("Refrescar lista de habitaciones");
        dispPanel.add(btnDisp);

        btnDisp.addActionListener(e -> {
            try {
                refrescarTablaHabitaciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controles.add(crearPanelFila(crearPanel, dispPanel));

        panel.add(scrollHab, BorderLayout.CENTER);
        panel.add(controles, BorderLayout.SOUTH);
        return panel;
    }

    // =============================
    // PANEL MIS RESERVAS (operaciones sobre reservas del cliente)
    // =============================
    private JPanel crearPanelMisReservas() {
        JPanel panel = new JPanel(new BorderLayout());

        reservasModel = new DefaultTableModel(
                new String[]{"ID", "Cliente", "Habitación", "Inicio", "Fin", "Estado", "Total"}, 0
        );
        reservasTable = new JTable(reservasModel);
        JScrollPane scrollRes = new JScrollPane(reservasTable);

        JPanel controles = new JPanel();
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));

        // --- Check-in ---
        JPanel checkInPanel = new JPanel();
        checkInPanel.setBorder(BorderFactory.createTitledBorder("Check-in"));
        JTextField idCheckIn = new JTextField(5);
        JTextField cedCheckIn = new JTextField(8);
        JButton btnCheckIn = new JButton("Check-in");
        checkInPanel.add(new JLabel("ID Reserva:")); checkInPanel.add(idCheckIn);
        checkInPanel.add(new JLabel("Cédula:")); checkInPanel.add(cedCheckIn);
        checkInPanel.add(btnCheckIn);

        btnCheckIn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idCheckIn.getText().trim());
                String cedula = cedCheckIn.getText().trim();
                sistema.getGestionReservas().realizarCheckIn(id, cedula);
                refrescarTablaReservas();
                JOptionPane.showMessageDialog(this, "Check-in exitoso");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Check-out ---
        JPanel checkOutPanel = new JPanel();
        checkOutPanel.setBorder(BorderFactory.createTitledBorder("Check-out"));
        JTextField idCheckOut = new JTextField(5);
        JButton btnCheckOut = new JButton("Check-out");
        checkOutPanel.add(new JLabel("ID Reserva:")); checkOutPanel.add(idCheckOut);
        checkOutPanel.add(btnCheckOut);

        btnCheckOut.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idCheckOut.getText().trim());
                sistema.getGestionReservas().realizarCheckOut(id);
                refrescarTablaReservas();
                JOptionPane.showMessageDialog(this, "Check-out exitoso");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Cancelar reserva ---
        JPanel cancelarPanel = new JPanel();
        cancelarPanel.setBorder(BorderFactory.createTitledBorder("Cancelar reserva"));
        JTextField idCancelar = new JTextField(5);
        JButton btnCancelar = new JButton("Cancelar");
        cancelarPanel.add(new JLabel("ID Reserva:")); cancelarPanel.add(idCancelar);
        cancelarPanel.add(btnCancelar);

        btnCancelar.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idCancelar.getText().trim());
                Reserva r = sistema.getGestionReservas().buscarReservaPorId(id);
                if (r == null) throw new IllegalArgumentException("Reserva no encontrada");
                if (r.getEstado() != EstadoReserva.CONFIRMADA) {
                    throw new IllegalStateException("Solo se puede cancelar una reserva CONFIRMADA sin check-in.");
                }
                sistema.getGestionReservas().cancelarReserva(id);
                refrescarTablaReservas();
                JOptionPane.showMessageDialog(this, "Reserva cancelada");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Pagar y ver factura ---
        JPanel pagarPanel = new JPanel();
        pagarPanel.setBorder(BorderFactory.createTitledBorder("Pagar reserva y ver factura"));
        JTextField idPagar = new JTextField(5);
        JButton btnPagar = new JButton("Pagar");
        pagarPanel.add(new JLabel("ID Reserva:")); pagarPanel.add(idPagar);
        pagarPanel.add(btnPagar);

        btnPagar.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idPagar.getText().trim());
                double iva = 0.19; // IVA fijo
                Reserva r = sistema.getGestionReservas().buscarReservaPorId(id);
                if (r == null) {
                    throw new IllegalArgumentException("Reserva no encontrada");
                }

                // Para pagar, la reserva debe tener al menos check-in realizado
                if (r.getEstado() != EstadoReserva.CHECK_IN_REALIZADO) {
                    throw new IllegalStateException("Para pagar, la reserva debe tener check-in realizado.");
                }

                Factura f = sistema.generarFactura(r, iva);
                sistema.marcarFacturaComoPagada(id);

                // Actualizar tabla para reflejar estado PENDIENTE PAGO / PAGADA
                refrescarTablaReservas();

                StringBuilder sb = new StringBuilder();
                sb.append("Factura ID: ").append(f.getId()).append("\n");
                sb.append("Fecha: ").append(f.getFecha()).append("\n");
                sb.append("Reserva ID: ").append(r.getId()).append("\n");
                sb.append("Habitación: ").append(r.getHabitacion().getNumero()).append("\n");
                sb.append("Subtotal: ").append(f.getSubtotal()).append("\n");
                sb.append("IVA: ").append(f.getIva()).append("\n");
                sb.append("Total: ").append(f.getTotal()).append("\n");
                sb.append("Método de pago: ").append(f.getMetodoPago()).append("\n");
                sb.append("Estado de pago: PAGADA\n");

                JOptionPane.showMessageDialog(this, sb.toString(),
                        "Factura", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controles.add(crearPanelFila(checkInPanel, checkOutPanel));
        controles.add(crearPanelFila(cancelarPanel, pagarPanel));

        panel.add(scrollRes, BorderLayout.CENTER);
        panel.add(controles, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFila(JPanel p1, JPanel p2) {
        JPanel fila = new JPanel(new GridLayout(1, 2));
        fila.add(p1);
        fila.add(p2);
        return fila;
    }

    private void refrescarTablaReservas() {
        if (reservasModel == null) return;
        reservasModel.setRowCount(0);
        if (clienteActual == null) {
            return; // No hay cliente autenticado todavía
        }
        for (Reserva r : sistema.getGestionReservas()
                .getReservasDeClientePorCedula(clienteActual.getCedula())) {
            // Determinar texto de estado considerando pago
            String estadoTexto = r.getEstado().toString();
            boolean pagada = sistema.getFacturas().stream()
                    .anyMatch(f -> f.getReserva().getId() == r.getId() && f.estaPagada());
            if (!pagada && r.getEstado() == EstadoReserva.CHECK_IN_REALIZADO) {
                estadoTexto += " (PENDIENTE PAGO)";
            } else if (pagada && r.getEstado() == EstadoReserva.FINALIZADA) {
                estadoTexto += " (PAGADA)";
            }

            reservasModel.addRow(new Object[]{
                    r.getId(),
                    clienteActual.getNombre(),
                    r.getHabitacion().getNumero(),
                    r.getFechaInicio(),
                    r.getFechaFin(),
                    estadoTexto,
                    r.calcularTotal()
            });
        }
    }

    private void refrescarTablaHabitaciones() {
        if (habitacionesModel == null) return;
        habitacionesModel.setRowCount(0);
        for (Habitacion h : sistema.getGestionHabitaciones().getTodasHabitaciones()) {
            habitacionesModel.addRow(new Object[]{
                    h.getNumero(), h.getTipo(), h.getPrecio(), h.getEstado()
            });
        }
    }

    // =============================
    // PANEL ADMINISTRADOR
    // =============================
    private JPanel crearPanelAdmin() {
        JPanel panel = new JPanel(new BorderLayout());

        // Login admin
        JPanel loginPanel = new JPanel();
        JTextField usuarioField = new JTextField(8);
        JPasswordField passField = new JPasswordField(8);
        JButton btnLogin = new JButton("Entrar");
        loginPanel.add(new JLabel("Usuario:")); loginPanel.add(usuarioField);
        loginPanel.add(new JLabel("Clave:")); loginPanel.add(passField);
        loginPanel.add(btnLogin);

        // Área de acciones y resultados
        JPanel accionesPanel = new JPanel();
        accionesPanel.setLayout(new BoxLayout(accionesPanel, BoxLayout.Y_AXIS));

        JTextArea salidaArea = new JTextArea(12, 70);
        salidaArea.setEditable(false);
        JScrollPane salidaScroll = new JScrollPane(salidaArea);

        // Botón listar todo
        JButton btnListarTodo = new JButton("Listar todo (Hab/Res/Fact)");
        btnListarTodo.setEnabled(false);
        btnListarTodo.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("--- Clientes ---\n");
            for (Cliente c : sistema.getClientes()) {
                sb.append(c.getCedula()).append(" - ").append(c.getNombre()).append("\n");
            }
            sb.append("\n--- Habitaciones ---\n");
            for (Habitacion h : sistema.getGestionHabitaciones().getTodasHabitaciones()) {
                sb.append(h.getNumero()).append(" ").append(h.getEstado())
                        .append(" ").append(h.getTipo()).append(" $")
                        .append(h.getPrecio()).append("\n");
            }
            sb.append("\n--- Reservas ---\n");
            for (Reserva r : sistema.getReservas()) {
                sb.append("ID ").append(r.getId())
                        .append(" Estado: ").append(r.getEstado())
                        .append(" Hab: ").append(r.getHabitacion().getNumero())
                        .append("\n");
            }
            sb.append("\n--- Facturas ---\n");
            for (Factura f : sistema.getFacturas()) {
                sb.append("ID ").append(f.getId())
                        .append(" Total: ").append(f.getTotal())
                        .append(" Pagada: ").append(f.estaPagada())
                        .append("\n");
            }
            salidaArea.setText(sb.toString());
        });

        // Borrar cliente
        JPanel borrarClientePanel = new JPanel();
        borrarClientePanel.setBorder(BorderFactory.createTitledBorder("Borrar cliente"));
        JTextField cedBorrar = new JTextField(10);
        JButton btnBorrarCliente = new JButton("Borrar");
        btnBorrarCliente.setEnabled(false);
        borrarClientePanel.add(new JLabel("Cédula:")); borrarClientePanel.add(cedBorrar);
        borrarClientePanel.add(btnBorrarCliente);

        btnBorrarCliente.addActionListener(e -> {
            try {
                String cedula = cedBorrar.getText().trim();
                sistema.getGestionClientes().borrarCliente(cedula);
                refrescarTablaClientes();
                JOptionPane.showMessageDialog(this, "Cliente borrado");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Editar cliente
        JPanel editarClientePanel = new JPanel();
        editarClientePanel.setBorder(BorderFactory.createTitledBorder("Editar cliente"));
        JTextField cedEditar = new JTextField(8);
        JTextField nomEditar = new JTextField(10);
        JTextField emailEditar = new JTextField(12);
        JTextField telEditar = new JTextField(8);
        JButton btnEditarCliente = new JButton("Guardar cambios");
        btnEditarCliente.setEnabled(false);
        editarClientePanel.add(new JLabel("Cédula:")); editarClientePanel.add(cedEditar);
        editarClientePanel.add(new JLabel("Nombre:")); editarClientePanel.add(nomEditar);
        editarClientePanel.add(new JLabel("Email:")); editarClientePanel.add(emailEditar);
        editarClientePanel.add(new JLabel("Teléfono:")); editarClientePanel.add(telEditar);
        editarClientePanel.add(btnEditarCliente);

        btnEditarCliente.addActionListener(e -> {
            try {
                String cedula = cedEditar.getText().trim();
                Cliente c = sistema.buscarClientePorCedula(cedula);
                if (c == null) throw new IllegalArgumentException("Cliente no encontrado");
                if (!nomEditar.getText().trim().isEmpty()) c.setNombre(nomEditar.getText().trim());
                if (!emailEditar.getText().trim().isEmpty()) c.setEmail(emailEditar.getText().trim());
                if (!telEditar.getText().trim().isEmpty()) c.setTelefono(telEditar.getText().trim());
                sistema.guardarClientes();
                refrescarTablaClientes();
                JOptionPane.showMessageDialog(this, "Cliente actualizado");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Borrar reserva
        JPanel borrarReservaPanel = new JPanel();
        borrarReservaPanel.setBorder(BorderFactory.createTitledBorder("Borrar reserva"));
        JTextField idBorrarReserva = new JTextField(5);
        JButton btnBorrarReserva = new JButton("Borrar");
        btnBorrarReserva.setEnabled(false);
        borrarReservaPanel.add(new JLabel("ID Reserva:"));
        borrarReservaPanel.add(idBorrarReserva);
        borrarReservaPanel.add(btnBorrarReserva);

        btnBorrarReserva.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idBorrarReserva.getText().trim());
                sistema.getGestionReservas().borrarReserva(id);
                refrescarTablaReservas();
                JOptionPane.showMessageDialog(this, "Reserva borrada");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Gestión de habitaciones (solo admin): agregar nueva habitación básica
        JPanel habAdminPanel = new JPanel();
        habAdminPanel.setBorder(BorderFactory.createTitledBorder("Agregar habitación"));
        JTextField numHabAdmin = new JTextField(5);
        JTextField tipoHabAdmin = new JTextField(10);
        JTextField precioHabAdmin = new JTextField(7);
        JButton btnAgregarHab = new JButton("Agregar");
        btnAgregarHab.setEnabled(false);
        habAdminPanel.add(new JLabel("Número:")); habAdminPanel.add(numHabAdmin);
        habAdminPanel.add(new JLabel("Tipo:")); habAdminPanel.add(tipoHabAdmin);
        habAdminPanel.add(new JLabel("Precio:")); habAdminPanel.add(precioHabAdmin);
        habAdminPanel.add(btnAgregarHab);

        // Editar habitación (precio y estado)
        JPanel editarHabPanel = new JPanel();
        editarHabPanel.setBorder(BorderFactory.createTitledBorder("Editar habitación"));
        JTextField numHabEdit = new JTextField(5);
        JTextField precioHabEdit = new JTextField(7);
        JComboBox<EstadoHabitacion> estadoHabEdit = new JComboBox<>(EstadoHabitacion.values());
        JButton btnEditarHab = new JButton("Guardar cambios");
        btnEditarHab.setEnabled(false);
        editarHabPanel.add(new JLabel("Número:")); editarHabPanel.add(numHabEdit);
        editarHabPanel.add(new JLabel("Nuevo precio:")); editarHabPanel.add(precioHabEdit);
        editarHabPanel.add(new JLabel("Estado:")); editarHabPanel.add(estadoHabEdit);
        editarHabPanel.add(btnEditarHab);

        btnEditarHab.addActionListener(e -> {
            try {
                int num = Integer.parseInt(numHabEdit.getText().trim());
                Habitacion h = sistema.getGestionHabitaciones().buscarHabitacionPorNumero(num);
                if (h == null) throw new IllegalArgumentException("Habitación no encontrada");
                if (!precioHabEdit.getText().trim().isEmpty()) {
                    double precio = Double.parseDouble(precioHabEdit.getText().trim());
                    h.setPrecio(precio);
                }
                if (estadoHabEdit.getSelectedItem() != null) {
                    h.setEstado((EstadoHabitacion) estadoHabEdit.getSelectedItem());
                }
                sistema.guardarHabitaciones();
                refrescarTablaHabitaciones();
                JOptionPane.showMessageDialog(this, "Habitación actualizada");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAgregarHab.addActionListener(e -> {
            try {
                int num = Integer.parseInt(numHabAdmin.getText().trim());
                double precio = Double.parseDouble(precioHabAdmin.getText().trim());
                String tipo = tipoHabAdmin.getText().trim();
                Habitacion h = new Habitacion(num, tipo, precio, EstadoHabitacion.DISPONIBLE);
                sistema.agregarHabitacion(h);
                JOptionPane.showMessageDialog(this, "Habitación agregada");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Editar factura (solo admin)
        JPanel editarFacturaPanel = new JPanel();
        editarFacturaPanel.setBorder(BorderFactory.createTitledBorder("Editar factura"));
        JTextField idFactura = new JTextField(5);
        JTextField metodoPagoField = new JTextField(8);
        JComboBox<String> estadoPagoCombo = new JComboBox<>(new String[]{
                "Sin cambios", "PENDIENTE", "PAGADA"
        });
        JButton btnEditarFactura = new JButton("Guardar cambios");
        btnEditarFactura.setEnabled(false);
        editarFacturaPanel.add(new JLabel("ID Factura:")); editarFacturaPanel.add(idFactura);
        editarFacturaPanel.add(new JLabel("Nuevo método pago:")); editarFacturaPanel.add(metodoPagoField);
        editarFacturaPanel.add(new JLabel("Estado pago:")); editarFacturaPanel.add(estadoPagoCombo);
        editarFacturaPanel.add(btnEditarFactura);

        btnEditarFactura.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idFactura.getText().trim());

                String nuevoMetodo = metodoPagoField.getText().trim();
                int sel = estadoPagoCombo.getSelectedIndex();
                Boolean pagada = null;
                if (sel == 1) { // PENDIENTE
                    pagada = false;
                } else if (sel == 2) { // PAGADA
                    pagada = true;
                }

                sistema.editarFactura(id, nuevoMetodo, pagada);
                JOptionPane.showMessageDialog(this, "Factura actualizada correctamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Borrar factura (solo admin)
        JPanel borrarFacturaPanel = new JPanel();
        borrarFacturaPanel.setBorder(BorderFactory.createTitledBorder("Borrar factura"));
        JTextField idBorrarFactura = new JTextField(5);
        JButton btnBorrarFactura = new JButton("Borrar");
        btnBorrarFactura.setEnabled(false);
        borrarFacturaPanel.add(new JLabel("ID Factura:")); borrarFacturaPanel.add(idBorrarFactura);
        borrarFacturaPanel.add(btnBorrarFactura);

        btnBorrarFactura.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idBorrarFactura.getText().trim());
                sistema.borrarFactura(id);
                JOptionPane.showMessageDialog(this, "Factura borrada correctamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Pagar factura (solo admin)
        JPanel pagarPanel = new JPanel();
        pagarPanel.setBorder(BorderFactory.createTitledBorder("Pagar factura por reserva"));
        JTextField idPagar = new JTextField(5);
        JButton btnPagar = new JButton("Pagar");
        btnPagar.setEnabled(false);
        pagarPanel.add(new JLabel("ID Reserva:")); pagarPanel.add(idPagar);
        pagarPanel.add(btnPagar);

        btnPagar.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idPagar.getText().trim());
                sistema.marcarFacturaComoPagada(id);
                JOptionPane.showMessageDialog(this, "Factura pagada correctamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        accionesPanel.add(btnListarTodo);
        accionesPanel.add(borrarClientePanel);
        accionesPanel.add(editarClientePanel);
        accionesPanel.add(borrarReservaPanel);
        accionesPanel.add(habAdminPanel);
        accionesPanel.add(editarHabPanel);
        accionesPanel.add(editarFacturaPanel);
        accionesPanel.add(borrarFacturaPanel);
        accionesPanel.add(pagarPanel);

        // Login habilita las acciones
        btnLogin.addActionListener(e -> {
            String u = usuarioField.getText().trim();
            String p = new String(passField.getPassword());
            if (!sistema.getAdministrador().getUsuario().equals(u)
                    || !sistema.getAdministrador().getContraseña().equals(p)) {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            btnListarTodo.setEnabled(true);
            btnBorrarCliente.setEnabled(true);
            btnEditarCliente.setEnabled(true);
            btnBorrarReserva.setEnabled(true);
            btnAgregarHab.setEnabled(true);
            btnEditarHab.setEnabled(true);
            btnEditarFactura.setEnabled(true);
            btnBorrarFactura.setEnabled(true);
            btnPagar.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Acceso de administrador concedido");
        });

        panel.add(loginPanel, BorderLayout.NORTH);
        panel.add(accionesPanel, BorderLayout.WEST);
        panel.add(salidaScroll, BorderLayout.CENTER);

        return panel;
    }

    // =============================
    // MÉTODO MAIN PARA LANZAR LA GUI
    // =============================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SistemaGestionHotel sistema = new SistemaGestionHotel();
            new HotelGUI(sistema);
        });
    }
}
