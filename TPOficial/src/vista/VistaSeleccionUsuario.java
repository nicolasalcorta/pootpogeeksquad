package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import modelo.Personal;

/** Primera ventana: el usuario elige quién es. Según su rol, VistaPrincipal muestra unas pestañas u otras. */
public class VistaSeleccionUsuario extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion contexto;
    private final SesionUsuario sesion = new SesionUsuario();
    private final JComboBox<Personal> cmbPersonal;

    public VistaSeleccionUsuario(ContextoAplicacion contexto) {
        super("Clínica Veterinaria - Ingreso");
        this.contexto = contexto;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cmbPersonal = VistaBase.crearCombo(contexto.getGestorPersonal().listarPersonal(),
                p -> p.getNombreCompleto() + " (" + p.getRol() + ")");
        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> confirmarSeleccion());

        JPanel pnlCentro = new JPanel(new GridLayout(2, 1, 8, 8));
        pnlCentro.add(new JLabel("¿Quién usa el sistema?"));
        pnlCentro.add(cmbPersonal);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.add(btnIngresar);

        JPanel pnlContenido = new JPanel(new BorderLayout(8, 8));
        pnlContenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlContenido.add(pnlCentro, BorderLayout.CENTER);
        pnlContenido.add(pnlBotones, BorderLayout.SOUTH);
        setContentPane(pnlContenido);

        getRootPane().setDefaultButton(btnIngresar);
        pack();
        setLocationRelativeTo(null);
    }

    private void confirmarSeleccion() {
        Personal elegido = (Personal) cmbPersonal.getSelectedItem();
        if (elegido == null) {
            JOptionPane.showMessageDialog(this, "No hay personal cargado.", "Ingreso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        sesion.iniciar(elegido);
        new VistaPrincipal(contexto, sesion).mostrar();
        dispose();
    }
}
