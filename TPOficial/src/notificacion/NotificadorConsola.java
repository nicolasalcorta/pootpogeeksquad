package notificacion;

import modelo.Dueño;

public class NotificadorConsola implements Notificador {

    @Override
    public void notificar(Dueño dueño, String mensaje) {
        String destinatario = (dueño != null) ? dueño.getNombreCompleto() : "(sin destinatario)";
        System.out.println("[NOTIFICACIÓN para " + destinatario + "] " + mensaje);
    }
}
