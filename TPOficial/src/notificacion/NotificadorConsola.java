package notificacion;

import modelo.Duenio;

public class NotificadorConsola implements Notificador {

    @Override
    public void notificar(Duenio duenio, String mensaje) {
        String destinatario = (duenio != null) ? duenio.getNombreCompleto() : "(sin destinatario)";
        System.out.println("[NOTIFICACIÓN para " + destinatario + "] " + mensaje);
    }
}
