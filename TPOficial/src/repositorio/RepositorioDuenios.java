package repositorio;

import java.util.List;

import modelo.Duenio;

public interface RepositorioDuenios {
    void guardar(Duenio duenio);
    Duenio buscarPorDni(String dni);
    List<Duenio> listarTodos();
}
