package farmacia.model;

public class DetalleVenta {
    private int idDetalleVenta;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double totalProducto;

    public DetalleVenta(int idDetalleVenta, Producto producto, int cantidad,
                        double precioUnitario, double totalProducto) {
        this.idDetalleVenta = idDetalleVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalProducto = totalProducto;
    }
}
