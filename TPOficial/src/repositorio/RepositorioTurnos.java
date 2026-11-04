package repositorio;

import java.util.List;

import modelo.Turno;
import modelo.Veterinario;

public interface RepositorioTurnos {
    void guardar(Turno turno);
    List<Turno> buscarPorVeterinario(Veterinario veterinario);
    List<Turno> listarTodos();
}
