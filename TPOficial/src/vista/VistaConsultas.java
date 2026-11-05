package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controlador.GestorConsultas;
import controlador.GestorStock;
import controlador.GestorVacunas;
import modelo.Consulta;
import modelo.HistoriaClinica;
import modelo.Lote;
import modelo.Mascota;
import modelo.NotaRectificatoria;
import modelo.Producto;
import modelo.Tratamiento;
import modelo.Turno;
import modelo.Vacuna;
import modelo.VacunaAplicada;
import modelo.Veterinario;

/**
 * Pestaña del veterinario: atiende sus turnos pendientes y completa sus consultas
 * (diagnóstico, tratamientos, vacunas), las confirma y agrega notas rectificatorias.
 */
public class VistaConsultas extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorConsultas gestorConsultas;
    private final GestorVacunas gestorVacunas;
    private final GestorStock gestorStock;

    private final TurnoTableModel modeloTurnos = new TurnoTableModel();
    private final ModeloTabla<Consulta> modeloConsultas = new ModeloTabla<>(
            new String[] {"Fecha", "Mascota", "Motivo", "Peso (kg)", "Diagnóstico", "Tratamientos", "Vacunas", "Estado"},
            c -> formatear(c.getFechaHora()),
            c -> c.getTurno() == null ? "-" : c.getTurno().getMascota().getNombre(),
            c -> c.getMotivo(),
            c -> c.getPesoDelDia(),
            c -> c.getDiagnostico(),
            c -> c.getTratamientos().size(),
            c -> c.getVacunas().size(),
            c -> c.isConfirmada() ? "Confirmada" : "Abierta");
    private final JTable tablaTurnos = crearTabla(modeloTurnos);
    private final JTable tablaConsultas = crearTabla(modeloConsultas);

    public VistaConsultas(SesionUsuario sesion, GestorConsultas gestorConsultas, GestorVacunas gestorVacunas,
                          GestorStock gestorStock) {
        super(sesion);
        if (!(sesion.getPersonal() instanceof Veterinario)) {
            throw new IllegalArgumentException("La pestaña de consultas es solo para veterinarios.");
        }
        this.gestorConsultas = gestorConsultas;
        this.gestorVacunas = gestorVacunas;
        this.gestorStock = gestorStock;

        JPanel pnlBotones = new JPanel(new GridLayout(2, 1));
        pnlBotones.add(crearBarraBotones(
                crearBoton("Registrar consulta del turno", this::onRegistrarConsulta),
                crearBoton("Ver historia clínica", this::onVerHistoria)));
        pnlBotones.add(crearBarraBotones(
                crearBoton("Diagnóstico", this::onDiagnostico),
                crearBoton("Agregar tratamiento", this::onAgregarTratamiento),
                crearBoton("Aplicar vacuna", this::onAplicarVacuna),
                crearBoton("Confirmar consulta", this::onConfirmarConsulta),
                crearBoton("Nota rectificatoria", this::onNotaRectificatoria)));
        add(pnlBotones, BorderLayout.NORTH);

        JPanel pnlTablas = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTablas.add(conTitulo("Mis turnos pendientes", tablaTurnos));
        pnlTablas.add(conTitulo("Mis consultas", tablaConsultas));
        add(pnlTablas, BorderLayout.CENTER);
    }

    private Veterinario veterinario() {
        return (Veterinario) sesion.getPersonal();
    }

    @Override
    public void refrescar() {
        modeloTurnos.setTurnos(gestorConsultas.turnosPendientes(veterinario()));
        modeloConsultas.setFilas(gestorConsultas.consultasDelVeterinario(veterinario()));
    }

    private void onRegistrarConsulta() {
        Turno turno = seleccionado(tablaTurnos, modeloTurnos, "un turno pendiente");
        if (turno == null) {
            return;
        }
        JTextField txtMotivo = new JTextField(20);
        JTextField txtPeso = new JTextField(6);
        Formulario frm = new Formulario()
                .agregar("Motivo:", txtMotivo)
                .agregar("Peso del día (kg, 0 si no se pesó):", txtPeso);
        if (!frm.mostrar(this, "Consulta de " + turno.getMascota().getNombre())) {
            return;
        }
        ejecutar(() -> {
            gestorConsultas.registrarConsulta(turno, veterinario(), texto(txtMotivo, "Motivo"), leerDecimal(txtPeso, "Peso"));
            mostrarMensaje("Consulta registrada. Completala en la tabla \"Mis consultas\".");
        });
    }

    private void onDiagnostico() {
        Consulta consulta = consultaSeleccionada();
        if (consulta == null) {
            return;
        }
        JTextField txtDiagnostico = new JTextField(consulta.getDiagnostico(), 25);
        JTextField txtObservaciones = new JTextField(consulta.getObservaciones(), 25);
        Formulario frm = new Formulario()
                .agregar("Diagnóstico:", txtDiagnostico)
                .agregar("Observaciones:", txtObservaciones);
        if (!frm.mostrar(this, "Diagnóstico")) {
            return;
        }
        ejecutar(() -> gestorConsultas.registrarDiagnostico(consulta, veterinario(), txtDiagnostico.getText(),
                txtObservaciones.getText()));
    }

    private void onAgregarTratamiento() {
        Consulta consulta = consultaSeleccionada();
        if (consulta == null) {
            return;
        }
        JComboBox<Producto> cmbMedicamento = crearCombo(gestorStock.listarProductos(),
                p -> p.getNombre() + " (stock " + p.getStockActual() + ")");
        JTextField txtCantidad = new JTextField("1", 5);
        JTextField txtDosis = new JTextField(12);
        JTextField txtFrecuencia = new JTextField(12);
        JTextField txtDias = new JTextField(5);
        Formulario frm = new Formulario()
                .agregar("Medicamento:", cmbMedicamento)
                .agregar("Unidades:", txtCantidad)
                .agregar("Dosis:", txtDosis)
                .agregar("Frecuencia:", txtFrecuencia)
                .agregar("Duración (días):", txtDias);
        if (!frm.mostrar(this, "Agregar tratamiento")) {
            return;
        }
        ejecutar(() -> {
            gestorConsultas.agregarTratamiento(consulta, veterinario(), elegido(cmbMedicamento, "productos"),
                    leerEntero(txtCantidad, "Unidades"), texto(txtDosis, "Dosis"), txtFrecuencia.getText().trim(),
                    leerEntero(txtDias, "Duración"));
            mostrarMensaje("Tratamiento agregado.");
        });
    }

    private void onAplicarVacuna() {
        Consulta consulta = consultaSeleccionada();
        if (consulta == null) {
            return;
        }
        JComboBox<Vacuna> cmbVacuna = crearCombo(gestorVacunas.listarCatalogo(), Vacuna::getNombre);
        JComboBox<Lote> cmbLote = crearCombo(List.of(),
                l -> l.getNumeroLote() + " (vence " + formatear(l.getFechaVencimiento()) + ", quedan " + l.getCantidad() + ")");
        // Al elegir otra vacuna se muestran los lotes disponibles de su producto.
        Runnable cargarLotes = () -> {
            cmbLote.removeAllItems();
            Vacuna vacuna = (Vacuna) cmbVacuna.getSelectedItem();
            if (vacuna != null) {
                for (Lote l : gestorStock.lotesDisponibles(vacuna.getInsumo())) {
                    cmbLote.addItem(l);
                }
            }
        };
        cmbVacuna.addActionListener(e -> cargarLotes.run());
        cargarLotes.run();
        Formulario frm = new Formulario()
                .agregar("Vacuna:", cmbVacuna)
                .agregar("Lote:", cmbLote);
        if (!frm.mostrar(this, "Aplicar vacuna")) {
            return;
        }
        ejecutar(() -> {
            VacunaAplicada aplicada = gestorConsultas.aplicarVacuna(consulta, veterinario(),
                    elegido(cmbVacuna, "vacunas en el catálogo"), elegido(cmbLote, "lotes disponibles"));
            mostrarMensaje("Vacuna aplicada. Próximo refuerzo: " + formatear(aplicada.getFechaProximoRefuerzo()) + ".");
        });
    }

    private void onConfirmarConsulta() {
        Consulta consulta = consultaSeleccionada();
        if (consulta == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Al confirmar se descuenta el stock y la consulta ya no se puede modificar. ¿Confirmar?",
                "Confirmar consulta", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar(() -> {
            gestorConsultas.confirmarConsulta(consulta, veterinario());
            mostrarMensaje("Consulta confirmada. Ya se puede facturar.");
        });
    }

    private void onNotaRectificatoria() {
        Consulta consulta = consultaSeleccionada();
        if (consulta == null) {
            return;
        }
        JTextField txtDescripcion = new JTextField(30);
        Formulario frm = new Formulario().agregar("Corrección:", txtDescripcion);
        if (!frm.mostrar(this, "Nota rectificatoria")) {
            return;
        }
        ejecutar(() -> {
            gestorConsultas.registrarNotaRectificatoria(consulta, veterinario(), texto(txtDescripcion, "Corrección"));
            mostrarMensaje("Nota agregada a la historia clínica.");
        });
    }

    /** Muestra la historia clínica completa de la mascota del turno o de la consulta seleccionada. */
    private void onVerHistoria() {
        Mascota mascota = null;
        if (tablaConsultas.getSelectedRow() >= 0) {
            Consulta c = modeloConsultas.getFila(tablaConsultas.getSelectedRow());
            mascota = c.getTurno() == null ? null : c.getTurno().getMascota();
        } else if (tablaTurnos.getSelectedRow() >= 0) {
            mascota = modeloTurnos.getFila(tablaTurnos.getSelectedRow()).getMascota();
        }
        if (mascota == null) {
            mostrarError("Seleccioná un turno o una consulta para ver la historia clínica de la mascota.");
            return;
        }
        JTextArea areaHistoria = new JTextArea(textoHistoria(mascota, gestorConsultas.consultarHistoria(mascota)));
        areaHistoria.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaHistoria);
        scroll.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scroll, "Historia clínica de " + mascota.getNombre(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private static String textoHistoria(Mascota mascota, HistoriaClinica historia) {
        StringBuilder sb = new StringBuilder();
        sb.append(historia.getNumeroHistoria()).append(" - ").append(mascota.getNombre()).append(" (")
                .append(mascota.getInfoEspecie()).append(")\n\n");
        if (historia.getConsultas().isEmpty()) {
            sb.append("Sin consultas registradas.\n");
        }
        for (Consulta c : historia.getConsultas()) {
            sb.append(formatear(c.getFechaHora())).append(" - ").append(c.getMotivo())
                    .append(c.isConfirmada() ? "" : " (abierta)").append('\n');
            if (c.getVeterinario() != null) {
                sb.append("  Atendió: ").append(c.getVeterinario().getNombreCompleto()).append('\n');
            }
            sb.append("  Peso: ").append(c.getPesoDelDia()).append(" kg\n");
            if (!c.getDiagnostico().isEmpty()) {
                sb.append("  Diagnóstico: ").append(c.getDiagnostico()).append('\n');
            }
            if (!c.getObservaciones().isEmpty()) {
                sb.append("  Observaciones: ").append(c.getObservaciones()).append('\n');
            }
            for (Tratamiento t : c.getTratamientos()) {
                sb.append("  Tratamiento: ").append(t.getNombreMedicamento()).append(" x").append(t.getCantidad())
                        .append(", ").append(t.getDosis()).append(", ").append(t.getFrecuencia())
                        .append(", ").append(t.getDuracionDias()).append(" días\n");
            }
            for (VacunaAplicada v : c.getVacunas()) {
                sb.append("  Vacuna: ").append(v.getNombreVacuna()).append(" (lote ").append(v.getLote().getNumeroLote())
                        .append("), refuerzo ").append(formatear(v.getFechaProximoRefuerzo())).append('\n');
            }
            for (NotaRectificatoria n : historia.getNotas()) {
                if (n.getConsulta() == c) {
                    sb.append("  Nota (").append(formatear(n.getFecha())).append(", ")
                            .append(n.getFirmadaPor().getNombreCompleto()).append("): ").append(n.getDescripcion()).append('\n');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private Consulta consultaSeleccionada() {
        return seleccionado(tablaConsultas, modeloConsultas, "una consulta");
    }
}
