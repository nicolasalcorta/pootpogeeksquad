package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Mascota;

public class RepositorioMascotasMemoria implements RepositorioMascotas {
    private final Map<String, Mascota> items = new LinkedHashMap<>();

    @Override
    public void guardar(Mascota elemento) {
        if (elemento != null) {
            items.put(elemento.getIdMascota(), elemento);
        }
    }

    @Override
    public Mascota buscarPorId(String clave) {
        return items.get(clave);
    }

    @Override
    public List<Mascota> listarTodas() {
        return new ArrayList<>(items.values());
    }
}
