package notificacion;

import modelo.Duenio;

public interface Notificador {
    void notificar(Duenio duenio, String mensaje);
}
