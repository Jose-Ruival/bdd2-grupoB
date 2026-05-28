package farmacia.model;

import java.util.List;

public class Venta {
    private int idVenta;
    private String fecha;
    private int nroTicket;
    private double total;
    private String formaPago;
    private Cliente cliente;
    private Sucursal sucursal;
    private Empleado empleadoAtencion;
    private Empleado empleadoCobro;
    private List<DetalleVenta> detalle;

    public Venta(int idVenta, String fecha, int nroTicket, double total, String formaPago,
                 Cliente cliente, Sucursal sucursal, Empleado empleadoAtencion,
                 Empleado empleadoCobro, List<DetalleVenta> detalle) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.nroTicket = nroTicket;
        this.total = total;
        this.formaPago = formaPago;
        this.cliente = cliente;
        this.sucursal = sucursal;
        this.empleadoAtencion = empleadoAtencion;
        this.empleadoCobro = empleadoCobro;
        this.detalle = detalle;
    }
}
