package farmacia.model;

public class Producto {
    private int codigo;
    private String tipo;
    private String descripcion;
    private double precio;
    private Laboratorio laboratorio;

    public Producto(int codigo, String tipo, String descripcion, double precio, Laboratorio laboratorio) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.laboratorio = laboratorio;
    }
}
