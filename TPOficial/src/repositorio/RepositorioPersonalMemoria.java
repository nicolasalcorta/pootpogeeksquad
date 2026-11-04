package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Personal;
import modelo.Veterinario;

public class RepositorioPersonalMemoria implements RepositorioPersonal {
    private final Map<String, Personal> items = new LinkedHashMap<>();

    @Override
    public void guardar(Personal personal) {
        if (personal != null) {
            items.put(personal.getDni(), personal);
        }
    }

    @Override
    public Personal buscarPorDni(String dni) {
        return items.get(dni);
    }

    @Override
    public List<Personal> listarTodos() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Veterinario> listarVeterinarios() {
        List<Veterinario> veterinarios = new ArrayList<>();
        for (Personal p : items.values()) {
            if (p instanceof Veterinario) {
                veterinarios.add((Veterinario) p);
            }
        }
        return veterinarios;
    }
}
