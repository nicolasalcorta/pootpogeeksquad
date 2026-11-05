package vista;

import modelo.Personal;
import modelo.RolPersonal;

/** Guarda quién está usando el sistema. Las vistas la consultan para saber qué mostrar. */
public class SesionUsuario {
    private Personal personalActual;

    public void iniciar(Personal personal) {
        if (personal == null) {
            throw new IllegalArgumentException("Hay que elegir un usuario para iniciar la sesión.");
        }
        this.personalActual = personal;
    }

    public Personal getPersonal() {
        return personalActual;
    }

    public boolean tieneRol(RolPersonal rol) {
        return personalActual != null && personalActual.tieneRol(rol);
    }
}
