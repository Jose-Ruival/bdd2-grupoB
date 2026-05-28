package farmacia.model;

public class Domicilio {
    private String calle;
    private int numero;
    private String localidad;
    private String provincia;

    public Domicilio(String calle, int numero, String localidad, String provincia) {
        this.calle = calle;
        this.numero = numero;
        this.localidad = localidad;
        this.provincia = provincia;
    }
}
