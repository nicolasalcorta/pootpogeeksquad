package modelo;

import java.time.LocalDateTime;

public class NotaCredito {
    private final String numero;
    private final LocalDateTime fecha;
    private final double monto;

    public NotaCredito(String numero, double monto) {
        this.numero = numero;
        this.fecha = LocalDateTime.now();
        this.monto = monto;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }
}
