package vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;

import controlador.GestorVacunas;
import modelo.Especie;
import modelo.Vacuna;
import modelo.VacunaAplicada;

/** Pestaña de vacunas: refuerzos próximos a vencer, recordatorios a los dueños y catálogo. */
public class VistaVacunas extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorVacunas gestorVacunas;

    private final ModeloTabla<VacunaAplicada> modeloProximas = new ModeloTabla<>(
            new String[] {"Vacuna", "Lote", "Aplicada", "Próximo refuerzo"},
            v -> v.getNombreVacuna(),
            v -> v.getLote().getNumeroLote(),
            v -> formatear(v.getFechaAplicacion()),
            v -> formatear(v.getFechaProximoRefuerzo()));
    private final ModeloTabla<Vacuna> modeloCatalogo = new ModeloTabla<>(
            new String[] {"Vacuna", "Edad mínima", "Refuerzo por especie", "Producto de stock", "Stock"},
            v -> v.getNombre(),
            v -> v.getEdadMinimaMeses() + " meses",
            v -> intervalos(v),
            v -> v.getInsumo().getNombre(),
            v -> v.getInsumo().getStockActual());
    private final JTable tablaProximas = crearTabla(modeloProximas);
    private final JTable tablaCatalogo = crearTabla(modeloCatalogo);

    public VistaVacunas(SesionUsuario sesion, GestorVacunas gestorVacunas) {
        super(sesion);
        this.gestorVacunas = gestorVacunas;

        add(crearBarraBotones(
                crearBoton("Ver próximas a vencer", this::onVerProximas),
                crearBoton("Emitir recordatorios", this::onEmitirRecordatorios)), BorderLayout.NORTH);

        JPanel pnlTablas = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTablas.add(conTitulo("Refuerzos que vencen en los próximos 7 días", tablaProximas));
        pnlTablas.add(conTitulo("Catálogo de vacunas", tablaCatalogo));
        add(pnlTablas, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        modeloProximas.setFilas(gestorVacunas.vacunasProximasAVencer());
        modeloCatalogo.setFilas(gestorVacunas.listarCatalogo());
    }

    private void onVerProximas() {
        refrescar();
        int cantidad = modeloProximas.getRowCount();
        mostrarMensaje(cantidad == 0 ? "No hay refuerzos próximos a vencer."
                : "Hay " + cantidad + " refuerzo(s) próximos a vencer.");
    }

    private void onEmitirRecordatorios() {
        ejecutar(() -> {
            int enviados = gestorVacunas.emitirRecordatorios();
            mostrarMensaje(enviados == 0 ? "No hay recordatorios nuevos para enviar."
                    : "Se enviaron " + enviados + " recordatorio(s) a los dueños.");
        });
    }

    private static String intervalos(Vacuna vacuna) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Especie, Integer> e : vacuna.getIntervalosPorEspecie().entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(" meses");
        }
        return sb.toString();
    }
}
