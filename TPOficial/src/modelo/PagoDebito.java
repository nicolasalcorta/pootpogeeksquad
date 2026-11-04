package modelo;

public class PagoDebito implements MedioPago {
    @Override
    public double getPorcentajeAjuste() {
        return 0.0;
    }

    @Override
    public String getDescripcion() {
        return "Débito";
    }
}
