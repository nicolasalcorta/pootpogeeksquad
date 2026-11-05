package vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import controlador.GestorPacientes;
import modelo.Ave;
import modelo.Conejo;
import modelo.Duenio;
import modelo.Especie;
import modelo.Gato;
import modelo.Mascota;
import modelo.Perro;

/** Pestaña de pacientes: dueños y mascotas. */
public class VistaPacientes extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorPacientes gestorPacientes;

    private final ModeloTabla<Duenio> modeloDuenios = new ModeloTabla<>(
            new String[] {"DNI", "Nombre", "Teléfono", "Mail", "Plan de salud", "Ausencias", "Mascotas"},
            d -> d.getDni(),
            d -> d.getNombreCompleto(),
            d -> d.getTelefono(),
            d -> d.getMail(),
            d -> d.isTienePlanSalud() ? "Sí" : "No",
            d -> d.getAusenciasConsecutivas(),
            d -> d.getMascotas().size());
    private final ModeloTabla<Mascota> modeloMascotas = new ModeloTabla<>(
            new String[] {"Id", "Nombre", "Especie", "Raza", "Sexo", "Edad", "Peso (kg)", "Dueño", "Detalle"},
            m -> m.getIdMascota(),
            m -> m.getNombre(),
            m -> m.getEspecie(),
            m -> m.getRaza(),
            m -> m.getSexo(),
            m -> m.getEdad() + " años",
            m -> m.getPesoActual(),
            m -> m.getDuenio() == null ? "-" : m.getDuenio().getNombreCompleto(),
            m -> m.getInfoEspecie());
    private final JTable tablaDuenios = crearTabla(modeloDuenios);
    private final JTable tablaMascotas = crearTabla(modeloMascotas);

    public VistaPacientes(SesionUsuario sesion, GestorPacientes gestorPacientes) {
        super(sesion);
        this.gestorPacientes = gestorPacientes;

        add(crearBarraBotones(
                crearBoton("Registrar dueño", this::onRegistrarDuenio),
                crearBoton("Registrar mascota", this::onRegistrarMascota),
                crearBoton("Transferir mascota", this::onTransferirMascota)), BorderLayout.NORTH);

        JPanel pnlTablas = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTablas.add(conTitulo("Dueños", tablaDuenios));
        pnlTablas.add(conTitulo("Mascotas", tablaMascotas));
        add(pnlTablas, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        modeloDuenios.setFilas(gestorPacientes.listarDuenios());
        modeloMascotas.setFilas(gestorPacientes.listarMascotas());
    }

    private void onRegistrarDuenio() {
        JTextField txtDni = new JTextField(12);
        JTextField txtNombre = new JTextField(15);
        JTextField txtApellido = new JTextField(15);
        JTextField txtTelefono = new JTextField(12);
        JTextField txtMail = new JTextField(18);
        JCheckBox chkPlanSalud = new JCheckBox("Tiene plan de salud");
        Formulario frm = new Formulario()
                .agregar("DNI:", txtDni)
                .agregar("Nombre:", txtNombre)
                .agregar("Apellido:", txtApellido)
                .agregar("Teléfono:", txtTelefono)
                .agregar("Mail:", txtMail)
                .agregar("", chkPlanSalud);
        if (!frm.mostrar(this, "Registrar dueño")) {
            return;
        }
        ejecutar(() -> {
            String dni = texto(txtDni, "DNI");
            Duenio duenio = new Duenio("D-" + dni, texto(txtNombre, "Nombre"), texto(txtApellido, "Apellido"), dni,
                    txtTelefono.getText().trim());
            duenio.setMail(txtMail.getText().trim());
            duenio.setTienePlanSalud(chkPlanSalud.isSelected());
            gestorPacientes.registrarDuenio(duenio);
            mostrarMensaje("Dueño " + duenio.getNombreCompleto() + " registrado.");
        });
    }

    private void onRegistrarMascota() {
        JComboBox<Duenio> cmbDuenio = comboDuenios();
        JComboBox<Especie> cmbEspecie = crearCombo(Arrays.asList(Especie.values()), Especie::name);
        JTextField txtNombre = new JTextField(15);
        JTextField txtRaza = new JTextField(15);
        JComboBox<String> cmbSexo = new JComboBox<>(new String[] {"Macho", "Hembra"});
        JTextField txtNacimiento = new JTextField(10);
        JTextField txtPeso = new JTextField(6);
        JTextField txtChip = new JTextField(12);
        JTextField txtTamanio = new JTextField(10);
        JCheckBox chkOpcion = new JCheckBox("Gato: vive adentro / Ave: puede volar / Conejo: tiene jaula");
        Formulario frm = new Formulario()
                .agregar("Dueño:", cmbDuenio)
                .agregar("Especie:", cmbEspecie)
                .agregar("Nombre:", txtNombre)
                .agregar("Raza:", txtRaza)
                .agregar("Sexo:", cmbSexo)
                .agregar("Fecha de nacimiento (dd/MM/aaaa):", txtNacimiento)
                .agregar("Peso (kg):", txtPeso)
                .agregar("Perro: número de chip", txtChip)
                .agregar("Perro: tamaño", txtTamanio)
                .agregar("", chkOpcion);
        if (!frm.mostrar(this, "Registrar mascota")) {
            return;
        }
        ejecutar(() -> {
            Duenio duenio = elegido(cmbDuenio, "dueños registrados");
            String id = nuevoIdMascota();
            String nombre = texto(txtNombre, "Nombre");
            String raza = txtRaza.getText().trim();
            String sexo = (String) cmbSexo.getSelectedItem();
            LocalDate nacimiento = leerFecha(txtNacimiento, "Fecha de nacimiento");
            double peso = leerDecimal(txtPeso, "Peso");
            Mascota mascota;
            switch (elegido(cmbEspecie, "especies")) {
                case PERRO:
                    mascota = new Perro(id, nombre, raza, sexo, nacimiento, peso, null, txtChip.getText().trim(),
                            txtTamanio.getText().trim());
                    break;
                case GATO:
                    mascota = new Gato(id, nombre, raza, sexo, nacimiento, peso, null, chkOpcion.isSelected());
                    break;
                case AVE:
                    mascota = new Ave(id, nombre, raza, sexo, nacimiento, peso, null, chkOpcion.isSelected());
                    break;
                default:
                    mascota = new Conejo(id, nombre, raza, sexo, nacimiento, peso, null, chkOpcion.isSelected());
            }
            gestorPacientes.registrarMascota(duenio, mascota);
            mostrarMensaje(nombre + " quedó registrada con el id " + id + ".");
        });
    }

    private void onTransferirMascota() {
        Mascota mascota = seleccionado(tablaMascotas, modeloMascotas, "una mascota");
        if (mascota == null) {
            return;
        }
        JComboBox<Duenio> cmbDuenio = comboDuenios();
        Formulario frm = new Formulario().agregar("Nuevo dueño de " + mascota.getNombre() + ":", cmbDuenio);
        if (!frm.mostrar(this, "Transferir mascota")) {
            return;
        }
        ejecutar(() -> {
            Duenio nuevo = elegido(cmbDuenio, "dueños registrados");
            gestorPacientes.transferirMascota(mascota, nuevo);
            mostrarMensaje(mascota.getNombre() + " ahora pertenece a " + nuevo.getNombreCompleto() + ".");
        });
    }

    private JComboBox<Duenio> comboDuenios() {
        return crearCombo(gestorPacientes.listarDuenios(), d -> d.getNombreCompleto() + " (DNI " + d.getDni() + ")");
    }

    /** Genera el próximo id libre con el formato M-001, M-002, ... */
    private String nuevoIdMascota() {
        int numero = gestorPacientes.listarMascotas().size() + 1;
        String id;
        do {
            id = String.format("M-%03d", numero++);
        } while (existeMascota(id));
        return id;
    }

    private boolean existeMascota(String id) {
        for (Mascota m : gestorPacientes.listarMascotas()) {
            if (m.getIdMascota().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
