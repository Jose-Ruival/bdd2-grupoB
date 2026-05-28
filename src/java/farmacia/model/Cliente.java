package farmacia.model;

import java.util.List;

public class Cliente {
    private int idCliente;
    private String apellido;
    private String nombre;
    private int dni;
    private Domicilio domicilio;
    private List<AfiliacionCliente> afiliaciones;

    public Cliente(int idCliente, String apellido, String nombre, int dni,
                   Domicilio domicilio, List<AfiliacionCliente> afiliaciones) {
        this.idCliente = idCliente;
        this.apellido = apellido;
        this.nombre = nombre;
        this.dni = dni;
        this.domicilio = domicilio;
        this.afiliaciones = afiliaciones;
    }
}
