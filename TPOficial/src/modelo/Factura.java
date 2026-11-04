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
        // Java evalúa los argumentos de izquierda a derecha: el número se pide recién después de validar.
        this(ConceptoFactura.CONSULTA, requerirConsulta(consulta), requerirDuenio(duenio), requerirMedio(medioPago),
                siguienteNumero(), LocalDateTime.now());
    }

    /** Factura de un cargo que no sale de una consulta (seña o cancelación tardía). */
    public Factura(ConceptoFactura concepto, Duenio duenio, MedioPago medioPago) {
        this(conceptoSinConsulta(concepto), null, requerirDuenio(duenio), requerirMedio(medioPago),
                siguienteNumero(), LocalDateTime.now());
    }

    private Factura(ConceptoFactura concepto, Consulta consulta, Duenio duenio, MedioPago medioPago,
                    String numeroFactura, LocalDateTime fecha) {
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.concepto = concepto;
        this.consulta = consulta;
        this.duenio = duenio;
        this.medioPago = medioPago;
        this.lineas = new ArrayList<>();
    }

    /**
     * Recrea una factura ya emitida, con su número, fecha y total originales (por ejemplo, al leerla de un archivo).
     * El contador avanza hasta ese número para que las facturas nuevas no lo repitan.
     */
    public static Factura restaurar(String numeroFactura, LocalDateTime fecha, ConceptoFactura concepto, Consulta consulta,
                                    Duenio duenio, MedioPago medioPago, List<LineaFactura> lineas, double total,
                                    NotaCredito notaCredito) {
        if (numeroFactura == null || fecha == null || concepto == null || lineas == null) {
            throw new IllegalArgumentException("El número, la fecha, el concepto y las líneas son obligatorios.");
        }
        if (concepto == ConceptoFactura.CONSULTA) {
            requerirConsulta(consulta);
        }
        Factura f = new Factura(concepto, consulta, requerirDuenio(duenio), requerirMedio(medioPago), numeroFactura, fecha);
        f.lineas.addAll(lineas);
        f.total = total;
        f.emitida = true;
        f.anulada = (notaCredito != null);
        f.notaCredito = notaCredito;
        CONTADOR.accumulateAndGet(numeroDe(numeroFactura), Math::max);
        return f;
    }

    private static String siguienteNumero() {
        return String.format("F-%06d", CONTADOR.incrementAndGet());
    }

    private static int numeroDe(String numeroFactura) {
        try {
            return Integer.parseInt(numeroFactura.substring(numeroFactura.indexOf('-') + 1));
        } catch (RuntimeException e) {
            return 0; // un número con otro formato no afecta al contador
        }
    }

    private static Consulta requerirConsulta(Consulta consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta es obligatoria para facturar una consulta.");
        }
        return consulta;
    }

    private static Duenio requerirDuenio(Duenio duenio) {
        if (duenio == null) {
            throw new IllegalArgumentException("El dueño es obligatorio.");
        }
        return duenio;
    }

    private static MedioPago requerirMedio(MedioPago medioPago) {
        if (medioPago == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        return medioPago;
    }

    private static ConceptoFactura conceptoSinConsulta(ConceptoFactura concepto) {
        if (concepto == null) {
            throw new IllegalArgumentException("El concepto es obligatorio.");
        }
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
