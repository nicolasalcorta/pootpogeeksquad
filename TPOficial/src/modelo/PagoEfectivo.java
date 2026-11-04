package modelo;

public class PagoEfectivo implements MedioPago {
    @Override
    public double getPorcentajeAjuste() {
        return -0.10;
    }

    @Override
    public String getDescripcion() {
        return "Efectivo";
    }
}
