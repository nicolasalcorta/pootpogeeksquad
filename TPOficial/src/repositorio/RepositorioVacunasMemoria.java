package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Vacuna;

public class RepositorioVacunasMemoria implements RepositorioVacunas {
    private final Map<String, Vacuna> items = new LinkedHashMap<>();

    @Override
    public void guardar(Vacuna vacuna) {
        if (vacuna != null) {
            items.put(vacuna.getNombre(), vacuna);
        }
    }

    @Override
    public Vacuna buscarPorNombre(String nombre) {
        return items.get(nombre);
    }

    @Override
    public List<Vacuna> listarTodas() {
        return new ArrayList<>(items.values());
    }
}
