package modelo;

public class PagoCreditoCuotas implements MedioPago {
    private final int cantidadCuotas;

    public PagoCreditoCuotas(int cantidadCuotas) {
        if (cantidadCuotas < 1) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser al menos 1.");
        }
        this.cantidadCuotas = cantidadCuotas;
    }

    @Override
    public double getPorcentajeAjuste() {
        return 0.08;
    }

    @Override
    public String getDescripcion() {
        return "Crédito en " + cantidadCuotas + " cuotas";
    }

    public int getCantidadCuotas() {
        return cantidadCuotas;
    }
}
