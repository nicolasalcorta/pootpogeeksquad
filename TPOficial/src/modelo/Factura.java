package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Factura {
    private static final double DESCUENTO_PLAN_SALUD = 0.15;
    private static final AtomicInteger CONTADOR = new AtomicInteger();

    private final String numeroFactura;
    private final LocalDateTime fecha;
    private final Consulta consulta;
    private final Dueño dueño;
    private final MedioPago medioPago;
    private final List<LineaFactura> lineas;
    private double total;
    private boolean emitida;
    private boolean anulada;
    private NotaCredito notaCredito;

    public Factura(Consulta consulta, Dueño dueño, MedioPago medioPago) {
        if (consulta == null || dueño == null || medioPago == null) {
            throw new IllegalArgumentException("La consulta, el dueño y el medio de pago son obligatorios.");
        }
        this.numeroFactura = String.format("F-%06d", CONTADOR.incrementAndGet());
        this.fecha = LocalDateTime.now();
        this.consulta = consulta;
        this.dueño = dueño;
        this.medioPago = medioPago;
        this.lineas = new ArrayList<>();
    }

    public LineaFactura agregarLinea(String concepto, int cantidad, double precioUnitario) {
        if (emitida) {
            throw new IllegalStateException("Una factura emitida no se puede modificar.");
        }
        LineaFactura linea = new LineaFactura(concepto, cantidad, precioUnitario);
        lineas.add(linea);
        return linea;
    }

    public double calcularSubtotal() {
        double subtotal = 0;
        for (LineaFactura l : lineas) {
            subtotal += l.calcularSubtotal();
        }
        return subtotal;
    }

    public double calcularTotal() {
        double ajuste = medioPago.getPorcentajeAjuste();
        double descuentoPlan = (dueño.isTienePlanSalud() ? DESCUENTO_PLAN_SALUD : 0.0);
        double ajusteFinal;
        if (ajuste > 0) {
            ajusteFinal = ajuste - descuentoPlan;
        } else {
            ajusteFinal = -Math.max(-ajuste, descuentoPlan);
        }
        total = Math.round(calcularSubtotal() * (1 + ajusteFinal) * 100.0) / 100.0;
        emitida = true;
        return total;
    }

    public NotaCredito anularConNotaCredito() {
        if (anulada) {
            throw new IllegalStateException("La factura " + numeroFactura + " ya fue anulada.");
        }
        anulada = true;
        notaCredito = new NotaCredito("NC-" + numeroFactura, total);
        return notaCredito;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public Dueño getDueño() {
        return dueño;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public NotaCredito getNotaCredito() {
        return notaCredito;
    }

    public List<LineaFactura> getLineas() {
        return Collections.unmodifiableList(lineas);
    }
}
