package vista;

import java.awt.BorderLayout;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import controlador.GestorPacientes;
import controlador.GestorPersonal;
import controlador.GestorTurnos;
import modelo.Factura;
import modelo.Mascota;
import modelo.MedioPago;
import modelo.TipoAtencion;
import modelo.Turno;
import modelo.Veterinario;

/** Pestaña de turnos: asignar, urgencias, reprogramar, cancelar, seña y ausencias. */
public class VistaTurnos extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorTurnos gestorTurnos;
    private final GestorPacientes gestorPacientes;
    private final GestorPersonal gestorPersonal;
    private final TurnoTableModel modeloTabla = new TurnoTableModel();
    private final JTable tabla = crearTabla(modeloTabla);

    public VistaTurnos(SesionUsuario sesion, GestorTurnos gestorTurnos, GestorPacientes gestorPacientes,
                       GestorPersonal gestorPersonal) {
        super(sesion);
        this.gestorTurnos = gestorTurnos;
        this.gestorPacientes = gestorPacientes;
        this.gestorPersonal = gestorPersonal;

        add(crearBarraBotones(
                crearBoton("Asignar turno", this::onAsignarTurno),
                crearBoton("Asignar urgencia", this::onAsignarUrgencia),
                crearBoton("Reprogramar", this::onReprogramar),
                crearBoton("Cancelar turno", this::onCancelar),
                crearBoton("Registrar seña", this::onRegistrarSenia),
                crearBoton("Marcar ausente", this::onMarcarAusente)), BorderLayout.NORTH);
        add(conTitulo("Turnos", tabla), BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        modeloTabla.setTurnos(gestorTurnos.listarTurnos());
    }

    private void onAsignarTurno() {
        JComboBox<Mascota> cmbMascota = comboMascotas();
        JComboBox<Veterinario> cmbVeterinario = crearCombo(gestorPersonal.listarVeterinarios(),
                v -> v.getNombreCompleto() + " - " + v.getEspecialidad());
        JTextField txtFechaHora = new JTextField(16);
        JComboBox<TipoAtencion> cmbTipo = crearCombo(Arrays.asList(TipoAtencion.values()), TipoAtencion::name);
        Formulario frm = new Formulario()
                .agregar("Mascota:", cmbMascota)
                .agregar("Veterinario:", cmbVeterinario)
                .agregar("Fecha y hora (dd/MM/aaaa HH:mm):", txtFechaHora)
                .agregar("Tipo de atención:", cmbTipo);
        if (!frm.mostrar(this, "Asignar turno")) {
            return;
        }
        ejecutar(() -> {
            Turno turno = gestorTurnos.asignarTurno(elegido(cmbMascota, "mascotas"), elegido(cmbVeterinario, "veterinarios"),
                    leerFechaHora(txtFechaHora, "Fecha y hora"), elegido(cmbTipo, "tipos de atención"));
            String aviso = turno.isSeniaPagada() ? ""
                    : "\nEl dueño tiene ausencias reiteradas: debe pagar la seña antes de la atención.";
            mostrarMensaje("Turno asignado para el " + formatear(turno.getFechaHora()) + "." + aviso);
        });
    }

    private void onAsignarUrgencia() {
        JComboBox<Mascota> cmbMascota = comboMascotas();
        JComboBox<TipoAtencion> cmbTipo = crearCombo(Arrays.asList(TipoAtencion.values()), TipoAtencion::name);
        Formulario frm = new Formulario()
                .agregar("Mascota:", cmbMascota)
                .agregar("Tipo de atención:", cmbTipo);
        if (!frm.mostrar(this, "Asignar urgencia")) {
            return;
        }
        ejecutar(() -> {
            Turno turno = gestorTurnos.asignarUrgencia(elegido(cmbMascota, "mascotas"), elegido(cmbTipo, "tipos de atención"));
            mostrarMensaje("Urgencia asignada a " + turno.getVeterinario().getNombreCompleto() + " para el "
                    + formatear(turno.getFechaHora()) + ".");
        });
    }

    private void onReprogramar() {
        Turno turno = seleccionado(tabla, modeloTabla, "un turno");
        if (turno == null) {
            return;
        }
        JTextField txtFechaHora = new JTextField(formatear(turno.getFechaHora()), 16);
        Formulario frm = new Formulario().agregar("Nueva fecha y hora (dd/MM/aaaa HH:mm):", txtFechaHora);
        if (!frm.mostrar(this, "Reprogramar turno")) {
            return;
        }
        ejecutar(() -> {
            LocalDateTime nueva = leerFechaHora(txtFechaHora, "Nueva fecha y hora");
            gestorTurnos.reprogramarTurno(turno, nueva);
            mostrarMensaje("Turno reprogramado para el " + formatear(nueva) + ".");
        });
    }

    private void onCancelar() {
        Turno turno = seleccionado(tabla, modeloTabla, "un turno");
        if (turno == null) {
            return;
        }
        MedioPago medio = pedirMedioPago("Medio de pago para un posible cargo por cancelación tardía");
        if (medio == null) {
            return;
        }
        ejecutar(() -> {
            Factura cargo = gestorTurnos.cancelarTurno(turno, medio);
            if (cargo == null) {
                mostrarMensaje("Turno cancelado sin cargo.");
            } else {
                mostrarMensaje("Turno cancelado con poca anticipación. Se emitió la factura " + cargo.getNumeroFactura()
                        + " por " + moneda(cargo.getTotal()) + ".");
            }
        });
    }

    private void onRegistrarSenia() {
        Turno turno = seleccionado(tabla, modeloTabla, "un turno");
        if (turno == null) {
            return;
        }
        MedioPago medio = pedirMedioPago("Pago de la seña");
        if (medio == null) {
            return;
        }
        ejecutar(() -> {
            Factura factura = gestorTurnos.registrarSenia(turno, medio);
            mostrarMensaje("Seña registrada. Factura " + factura.getNumeroFactura() + " por "
                    + moneda(factura.getTotal()) + ".");
        });
    }

    private void onMarcarAusente() {
        Turno turno = seleccionado(tabla, modeloTabla, "un turno");
        if (turno == null) {
            return;
        }
        ejecutar(() -> {
            gestorTurnos.marcarAusente(turno);
            mostrarMensaje("Se registró la ausencia de " + turno.getMascota().getNombre() + ".");
        });
    }

    private JComboBox<Mascota> comboMascotas() {
        return crearCombo(gestorPacientes.listarMascotas(), m -> m.getNombre() + " (" + m.getEspecie() + ") - "
                + (m.getDuenio() == null ? "sin dueño" : m.getDuenio().getNombreCompleto()));
    }
}
