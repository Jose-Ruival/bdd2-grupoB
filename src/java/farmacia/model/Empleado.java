package farmacia.model;

import java.util.List;

public class Empleado {
    private int idEmpleado;
    private String apellido;
    private String nombre;
    private int dni;
    private long cuil;
    private Domicilio domicilio;
    private List<AfiliacionEmpleado> afiliaciones;

    public Empleado(int idEmpleado, String apellido, String nombre, int dni, long cuil,
                    Domicilio domicilio, List<AfiliacionEmpleado> afiliaciones) {
        this.idEmpleado = idEmpleado;
        this.apellido = apellido;
        this.nombre = nombre;
        this.dni = dni;
        this.cuil = cuil;
        this.domicilio = domicilio;
        this.afiliaciones = afiliaciones;
    }
}
