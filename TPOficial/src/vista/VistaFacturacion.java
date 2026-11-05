package vista;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import controlador.GestorConsultas;
import controlador.GestorFacturacion;
import modelo.Consulta;
import modelo.Factura;
import modelo.LineaFactura;
import modelo.MedioPago;
import modelo.NotaCredito;

/** Pestaña de facturación: facturar consultas confirmadas, ver el detalle y anular con nota de crédito. */
public class VistaFacturacion extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorFacturacion gestorFacturacion;
    private final GestorConsultas gestorConsultas;

    private final ModeloTabla<Factura> modeloFacturas = new ModeloTabla<>(
            new String[] {"Número", "Fecha", "Concepto", "Dueño", "Medio de pago", "Total", "Estado"},
            f -> f.getNumeroFactura(),
            f -> formatear(f.getFecha()),
            f -> f.getConcepto(),
            f -> f.getDuenio().getNombreCompleto(),
            f -> f.getMedioPago().getDescripcion(),
            f -> moneda(f.getTotal()),
            f -> f.isAnulada() ? "Anulada (" + f.getNotaCredito().getNumero() + ")" : "Vigente");
    private final JTable tablaFacturas = crearTabla(modeloFacturas);

    public VistaFacturacion(SesionUsuario sesion, GestorFacturacion gestorFacturacion, GestorConsultas gestorConsultas) {
        super(sesion);
        this.gestorFacturacion = gestorFacturacion;
        this.gestorConsultas = gestorConsultas;

        add(crearBarraBotones(
                crearBoton("Emitir factura de consulta", this::onEmitirFactura),
                crearBoton("Ver detalle", this::onVerDetalle),
                crearBoton("Anular factura", this::onAnularFactura)), BorderLayout.NORTH);
        add(conTitulo("Facturas", tablaFacturas), BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        modeloFacturas.setFilas(gestorFacturacion.listarFacturas());
    }

    private void onEmitirFactura() {
        JComboBox<Consulta> cmbConsulta = crearCombo(gestorConsultas.consultasPendientesDeFacturar(),
                c -> formatear(c.getFechaHora()) + " - " + c.getTurno().getMascota().getNombre() + " ("
                        + c.getTurno().getMascota().getDuenio().getNombreCompleto() + ") - " + c.getMotivo());
        if (cmbConsulta.getItemCount() == 0) {
            mostrarMensaje("No hay consultas confirmadas pendientes de facturar.");
            return;
        }
        Formulario frm = new Formulario().agregar("Consulta:", cmbConsulta);
        if (!frm.mostrar(this, "Emitir factura")) {
            return;
        }
        MedioPago medio = pedirMedioPago("Medio de pago");
        if (medio == null) {
            return;
        }
        ejecutar(() -> {
            Factura factura = gestorFacturacion.emitirFactura(elegido(cmbConsulta, "consultas"), medio);
            mostrarMensaje(detalle(factura));
        });
    }

    private void onVerDetalle() {
        Factura factura = seleccionado(tablaFacturas, modeloFacturas, "una factura");
        if (factura != null) {
            JOptionPane.showMessageDialog(this, detalle(factura), "Factura " + factura.getNumeroFactura(),
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void onAnularFactura() {
        Factura factura = seleccionado(tablaFacturas, modeloFacturas, "una factura");
        if (factura == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Anular la factura " + factura.getNumeroFactura()
                + " con una nota de crédito?", "Anular factura", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        // El gestor valida que el usuario sea ADMINISTRADOR (RolNoAutorizadoException si no lo es).
        ejecutar(() -> {
            NotaCredito nota = gestorFacturacion.anularFactura(factura, sesion.getPersonal());
            mostrarMensaje("Factura anulada. Nota de crédito " + nota.getNumero() + " por " + moneda(nota.getMonto()) + ".");
        });
    }

    private static String detalle(Factura f) {
        StringBuilder sb = new StringBuilder();
        sb.append("Factura ").append(f.getNumeroFactura()).append(" - ").append(formatear(f.getFecha())).append('\n');
        sb.append("Concepto: ").append(f.getConcepto()).append('\n');
        sb.append("Cliente: ").append(f.getDuenio().getNombreCompleto())
                .append(f.getDuenio().isTienePlanSalud() ? " (plan de salud)" : "").append("\n\n");
        for (LineaFactura l : f.getLineas()) {
            sb.append(l.getCantidad()).append(" x ").append(l.getConcepto()).append("  ")
                    .append(moneda(l.getPrecioUnitario())).append(" = ").append(moneda(l.calcularSubtotal())).append('\n');
        }
        sb.append("\nSubtotal: ").append(moneda(f.calcularSubtotal())).append('\n');
        sb.append("Medio de pago: ").append(f.getMedioPago().getDescripcion()).append('\n');
        sb.append("Total: ").append(moneda(f.getTotal()));
        if (f.isAnulada()) {
            sb.append("\n\nANULADA con nota de crédito ").append(f.getNotaCredito().getNumero());
        }
        return sb.toString();
    }
}
