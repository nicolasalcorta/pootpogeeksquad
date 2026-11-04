package notificacion;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import modelo.Duenio;

/**
 * Muestra cada notificación en una ventana emergente (Swing).
 * Los gestores no saben que existe: solo conocen la interfaz Notificador (DIP).
 */
public class NotificadorVentana implements Notificador {

    @Override
    public void notificar(Duenio duenio, String mensaje) {
        String destinatario = (duenio != null) ? duenio.getNombreCompleto() : "(sin destinatario)";
        String titulo = "Notificación para " + destinatario;
        // Las ventanas de Swing se deben crear en el hilo de eventos de Swing.
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE));
    }
}
