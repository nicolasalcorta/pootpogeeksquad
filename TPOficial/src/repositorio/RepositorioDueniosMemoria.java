package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Dueño;

public class RepositorioDueniosMemoria implements RepositorioDuenios {
    private final Map<String, Dueño> items = new LinkedHashMap<>();

    @Override
    public void guardar(Dueño elemento) {
        if (elemento != null) {
            items.put(elemento.getDni(), elemento);
        }
    }

    @Override
    public Dueño buscarPorDni(String clave) {
        return items.get(clave);
    }

    @Override
    public List<Dueño> listarTodos() {
        return new ArrayList<>(items.values());
    }
}
