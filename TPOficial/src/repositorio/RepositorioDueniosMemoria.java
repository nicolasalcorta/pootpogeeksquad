package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Duenio;

public class RepositorioDueniosMemoria implements RepositorioDuenios {
    private final Map<String, Duenio> items = new LinkedHashMap<>();

    @Override
    public void guardar(Duenio elemento) {
        if (elemento != null) {
            items.put(elemento.getDni(), elemento);
        }
    }

    @Override
    public Duenio buscarPorDni(String clave) {
        return items.get(clave);
    }

    @Override
    public List<Duenio> listarTodos() {
        return new ArrayList<>(items.values());
    }
}
