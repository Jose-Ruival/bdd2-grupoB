package farmacia.model;

public class AfiliacionCliente {
    private ObraSocial obraSocial;
    private int nroAfiliado;

    public AfiliacionCliente(ObraSocial obraSocial, int nroAfiliado) {
        this.obraSocial = obraSocial;
        this.nroAfiliado = nroAfiliado;
    }
}
