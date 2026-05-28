package farmacia.model;

public class Sucursal {
    private int idSucursal;
    private String calle;
    private int numero;
    private String localidad;
    private String provincia;
    private int puntoDeVenta;
    private Empleado encargado;

    public Sucursal(int idSucursal, String calle, int numero, String localidad,
                    String provincia, int puntoDeVenta, Empleado encargado) {
        this.idSucursal = idSucursal;
        this.calle = calle;
        this.numero = numero;
        this.localidad = localidad;
        this.provincia = provincia;
        this.puntoDeVenta = puntoDeVenta;
        this.encargado = encargado;
    }
}
