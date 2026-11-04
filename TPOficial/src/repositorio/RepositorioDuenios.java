package repositorio;

import java.util.List;

import modelo.Dueño;

public interface RepositorioDuenios {
    void guardar(Dueño dueño);
    Dueño buscarPorDni(String dni);
    List<Dueño> listarTodos();
}
