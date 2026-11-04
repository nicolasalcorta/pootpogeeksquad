package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import excepciones.FacturaAnuladaException;

public class Factura {
    private static final double DESCUENTO_PLAN_SALUD = 0.15;
    private static final AtomicInteger CONTADOR = new AtomicInteger();

    private final String numeroFactura;
    private final LocalDateTime fecha;
    private final ConceptoFactura concepto;
    private final Consulta consulta; // solo para concepto CONSULTA; null en seña o cargo por cancelación
    private final Duenio duenio;
    private final MedioPago medioPago;
    private final List<LineaFactura> lineas;
    private double total;
    private boolean emitida;
    private boolean anulada;
    private NotaCredito notaCredito;

    /** Factura de una consulta atendida. */
    public Factura(Consulta consulta, Duenio duenio, MedioPago medioPago) {
        this(ConceptoFactura.CONSULTA, requerirConsulta(consulta), duenio, medioPago);
    }

    /** Factura de un cargo que no sale de una consulta (seña o cancelación tardía). */
    public Factura(ConceptoFactura concepto, Duenio duenio, MedioPago medioPago) {
        this(conceptoSinConsulta(concepto), null, duenio, medioPago);
    }

    private Factura(ConceptoFactura concepto, Consulta consulta, Duenio duenio, MedioPago medioPago) {
        if (concepto == null || duenio == null || medioPago == null) {
            throw new IllegalArgumentException("El concepto, el dueño y el medio de pago son obligatorios.");
        }
        this.numeroFactura = String.format("F-%06d", CONTADOR.incrementAndGet());
        this.fecha = LocalDateTime.now();
        this.concepto = concepto;
        this.consulta = consulta;
        this.duenio = duenio;
        this.medioPago = medioPago;
        this.lineas = new ArrayList<>();
    }

    // Se validan antes de llamar al constructor privado para no consumir un número de factura.
    private static Consulta requerirConsulta(Consulta consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta es obligatoria para facturar una consulta.");
        }
        return consulta;
    }

    private static ConceptoFactura conceptoSinConsulta(ConceptoFactura concepto) {
        if (concepto == ConceptoFactura.CONSULTA) {
            throw new IllegalArgumentException("Para facturar una consulta hay que indicar la consulta.");
        }
        return concepto;
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
        double descuentoPlan = (duenio.isTienePlanSalud() ? DESCUENTO_PLAN_SALUD : 0.0);
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

    public NotaCredito anularConNotaCredito() throws FacturaAnuladaException {
        if (anulada) {
            throw new FacturaAnuladaException("La factura " + numeroFactura + " ya fue anulada.");
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

    public ConceptoFactura getConcepto() {
        return concepto;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public Duenio getDuenio() {
        return duenio;
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
