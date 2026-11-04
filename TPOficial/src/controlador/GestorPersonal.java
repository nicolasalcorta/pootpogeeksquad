package controlador;

import java.util.List;

import modelo.Personal;
import modelo.Veterinario;
import repositorio.RepositorioPersonal;

public class GestorPersonal {
    private final RepositorioPersonal repositorioPersonal;

    public GestorPersonal(RepositorioPersonal repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio.");
        }
        this.repositorioPersonal = repositorio;
    }

    public void registrarPersonal(Personal personal) {
        if (personal == null) {
            throw new IllegalArgumentException("El personal no puede ser nulo.");
        }
        if (repositorioPersonal.buscarPorDni(personal.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe personal registrado con el DNI " + personal.getDni() + ".");
        }
        repositorioPersonal.guardar(personal);
    }

    public List<Personal> listarPersonal() {
        return repositorioPersonal.listarTodos();
    }

    public List<Veterinario> listarVeterinarios() {
        return repositorioPersonal.listarVeterinarios();
    }
}
