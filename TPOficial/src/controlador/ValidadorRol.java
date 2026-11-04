package controlador;

import excepciones.RolNoAutorizadoException;
import modelo.Personal;
import modelo.RolPersonal;

/** Validación de permisos compartida por los gestores que tienen operaciones restringidas por rol. */
final class ValidadorRol {

    private ValidadorRol() {
    }

    static void exigir(Personal usuario, RolPersonal rolRequerido, String operacion) throws RolNoAutorizadoException {
        if (usuario == null) {
            throw new IllegalArgumentException("Hay que indicar el usuario que realiza la operación.");
        }
        if (!usuario.tieneRol(rolRequerido)) {
            throw new RolNoAutorizadoException("Solo el rol " + rolRequerido + " puede " + operacion
                    + ". " + usuario.getNombreCompleto() + " tiene el rol " + usuario.getRol() + ".");
        }
    }
}
