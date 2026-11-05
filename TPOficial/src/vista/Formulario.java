package vista;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Formulario simple de dos columnas (etiqueta | campo) que se muestra en un diálogo con Aceptar y Cancelar.
 * Lo usan todas las vistas para pedir datos al usuario.
 */
public class Formulario extends JPanel {
    private static final long serialVersionUID = 1L;

    public Formulario() {
        super(new GridLayout(0, 2, 8, 8));
    }

    public Formulario agregar(String etiqueta, JComponent campo) {
        add(new JLabel(etiqueta));
        add(campo);
        return this;
    }

    /** Muestra el formulario en un diálogo modal. Devuelve true si el usuario confirmó con Aceptar. */
    public boolean mostrar(Component padre, String titulo) {
        int opcion = JOptionPane.showConfirmDialog(padre, this, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION;
    }
}
