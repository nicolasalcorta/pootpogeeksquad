package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Producto;

public class RepositorioProductosMemoria implements RepositorioProductos {
    private final Map<String, Producto> items = new LinkedHashMap<>();

    @Override
    public void guardar(Producto elemento) {
        if (elemento != null) {
            items.put(elemento.getCodigo(), elemento);
        }
    }

    @Override
    public Producto buscarPorCodigo(String clave) {
        return items.get(clave);
    }

    @Override
    public List<Producto> listarTodos() {
        return new ArrayList<>(items.values());
    }
}
