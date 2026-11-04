package modelo;

import java.time.LocalDateTime;

public class NotaCredito {
    private final String numero;
    private final LocalDateTime fecha;
    private final double monto;

    public NotaCredito(String numero, double monto) {
        this(numero, LocalDateTime.now(), monto);
    }

    /** Recrea una nota de crédito ya emitida (por ejemplo, al leerla de un archivo). */
    public NotaCredito(String numero, LocalDateTime fecha, double monto) {
        this.numero = numero;
        this.fecha = fecha;
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
