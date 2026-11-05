package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import modelo.RolPersonal;
import modelo.Veterinario;

/**
 * Ventana principal con una pestaña por área. Muestra solo las pestañas que corresponden al rol del usuario
 * y a cada vista le pasa por constructor únicamente los gestores que usa.
 */
public class VistaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion contexto;
    private final SesionUsuario sesion;
    private final JTabbedPane pestanas = new JTabbedPane();

    public VistaPrincipal(ContextoAplicacion contexto, SesionUsuario sesion) {
        super("Clínica Veterinaria");
        this.contexto = contexto;
        this.sesion = sesion;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblUsuario = new JLabel("Usuario: " + sesion.getPersonal().getNombreCompleto()
                + " - " + sesion.getPersonal().getRol());
        JButton btnCambiarUsuario = new JButton("Cambiar usuario");
        btnCambiarUsuario.addActionListener(e -> cambiarUsuario());

        JPanel pnlSuperior = new JPanel(new BorderLayout());
        pnlSuperior.setBorder(BorderFactory.createEmptyBorder(6, 10, 0, 10));
        pnlSuperior.add(lblUsuario, BorderLayout.WEST);
        JPanel pnlDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlDerecha.add(btnCambiarUsuario);
        pnlSuperior.add(pnlDerecha, BorderLayout.EAST);

        armarPestanasSegunRol();
        // Al cambiar de pestaña se recargan sus datos, porque otra pestaña pudo haberlos modificado.
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() instanceof VistaBase) {
                ((VistaBase) pestanas.getSelectedComponent()).refrescar();
            }
        });

        setLayout(new BorderLayout());
        add(pnlSuperior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    public void mostrar() {
        setVisible(true);
    }

    /**
     * ADMINISTRADOR: todas las pestañas.
     * RECEPCIONISTA: turnos, pacientes, vacunas y facturación.
     * VETERINARIO: turnos, pacientes, consultas y vacunas.
     */
    private void armarPestanasSegunRol() {
        boolean admin = sesion.tieneRol(RolPersonal.ADMINISTRADOR);
        boolean recepcion = sesion.tieneRol(RolPersonal.RECEPCIONISTA);
        boolean veterinario = sesion.getPersonal() instanceof Veterinario;

        agregar("Turnos", new VistaTurnos(sesion, contexto.getGestorTurnos(), contexto.getGestorPacientes(),
                contexto.getGestorPersonal()));
        agregar("Pacientes", new VistaPacientes(sesion, contexto.getGestorPacientes()));
        if (veterinario) {
            agregar("Consultas", new VistaConsultas(sesion, contexto.getGestorConsultas(), contexto.getGestorVacunas(),
                    contexto.getGestorStock()));
        }
        agregar("Vacunas", new VistaVacunas(sesion, contexto.getGestorVacunas()));
        if (admin || recepcion) {
            agregar("Facturación", new VistaFacturacion(sesion, contexto.getGestorFacturacion(),
                    contexto.getGestorConsultas()));
        }
        if (admin) {
            agregar("Stock", new VistaStock(sesion, contexto.getGestorStock()));
        }
    }

    private void agregar(String titulo, VistaBase vista) {
        pestanas.addTab(titulo, vista);
        vista.refrescar();
    }

    private void cambiarUsuario() {
        new VistaSeleccionUsuario(contexto).setVisible(true);
        dispose();
    }
}
