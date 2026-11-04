package repositorio;

import java.util.List;

import modelo.Producto;

public interface RepositorioProductos {
    void guardar(Producto producto);
    Producto buscarPorCodigo(String codigo);
    List<Producto> listarTodos();
}
