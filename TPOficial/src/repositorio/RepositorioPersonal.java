package repositorio;

import java.util.List;

import modelo.Personal;
import modelo.Veterinario;

public interface RepositorioPersonal {
    void guardar(Personal personal);
    Personal buscarPorDni(String dni);
    List<Personal> listarTodos();
    List<Veterinario> listarVeterinarios();
}
