package notificacion;

import modelo.Dueño;

public interface Notificador {
    void notificar(Dueño dueño, String mensaje);
}
