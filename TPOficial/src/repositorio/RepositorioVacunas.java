package repositorio;

import java.util.List;

import modelo.Vacuna;

public interface RepositorioVacunas {
    void guardar(Vacuna vacuna);
    Vacuna buscarPorNombre(String nombre);
    List<Vacuna> listarTodas();
}
