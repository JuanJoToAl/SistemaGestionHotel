package com.mycompany.sistemagestionhotel;

import java.util.List;
import java.util.Optional;

public class GestionClientes {
    private List<Cliente> clientes;
    private DatosHotel datos; // Necesitamos el acceso a DatosHotel para gestionar el ID.

    public GestionClientes(List<Cliente> clientes, DatosHotel datos) {
        this.clientes = clientes;
        this.datos = datos;
    }
    
    // En la clase com.mycompany.sistemagestionhotel.GestionClientes

public void borrarCliente(String cedula) throws IllegalArgumentException {
    Cliente clienteABorrar = null;
    
    // Buscamos el cliente por cédula (asumiendo que tienes un método buscarClientePorCedula)
    for (Cliente c : clientes) {
        if (c.getCedula().equals(cedula)) {
            clienteABorrar = c;
            break;
        }
    }

    if (clienteABorrar == null) {
        throw new IllegalArgumentException("❌ Error: Cliente con cédula " + cedula + " no encontrado.");
    }

    // Nota: Aquí, en un sistema real, deberías verificar que el cliente 
    // no tiene reservas activas. Por ahora, solo lo borra.

    if (clientes.remove(clienteABorrar)) {
        // En un sistema persistente (base de datos o archivo), 
        // aquí se llamaría al método para guardar los cambios.
        // Si tienes un método de guardado en SistemaGestionHotel, deberás llamarlo desde allí.
    } else {
        throw new IllegalArgumentException("❌ Error interno: No se pudo eliminar el cliente de la lista.");
    }
}

    /**
     * Registra un nuevo cliente, asignándole el siguiente ID disponible.
     * @param nuevoCliente El cliente sin ID.
     * @return El cliente ya registrado con su ID asignado.
     * @throws IllegalArgumentException si la cédula ya existe.
     */
    public Cliente registrarCliente(Cliente nuevoCliente) throws IllegalArgumentException {
        if (buscarClientePorCedula(nuevoCliente.getCedula()) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con la cédula " + nuevoCliente.getCedula());
        }
        
        // Asignar y actualizar el ID.
        nuevoCliente.setId(datos.getNextClienteId());
        datos.setNextClienteId(datos.getNextClienteId() + 1);
        
        clientes.add(nuevoCliente);
        return nuevoCliente;
    }

    /**
     * Busca un cliente por su ID.
     * @param id El ID del cliente.
     * @return El cliente encontrado o null si no existe.
     */
    public Cliente buscarClientePorId(int id) {
        Optional<Cliente> clienteEncontrado = clientes.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
        return clienteEncontrado.orElse(null);
    }

    /**
     * Busca un cliente por su número de cédula.
     * @param cedula La cédula del cliente.
     * @return El cliente encontrado o null si no existe.
     */
    public Cliente buscarClientePorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        Optional<Cliente> clienteEncontrado = clientes.stream()
                .filter(c -> c.getCedula().equalsIgnoreCase(cedula.trim()))
                .findFirst();
        return clienteEncontrado.orElse(null);
    }
    
    // Método para cargar clientes desde la persistencia si es necesario
    public List<Cliente> getTodosLosClientes() {
        return clientes;
    }
}