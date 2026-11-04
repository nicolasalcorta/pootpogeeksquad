package repositorio;

import java.util.ArrayList;
import java.util.List;

import modelo.Turno;
import modelo.Veterinario;

public class RepositorioTurnosMemoria implements RepositorioTurnos {
    private final List<Turno> turnos = new ArrayList<>();

    @Override
    public void guardar(Turno turno) {
        if (turno != null && !turnos.contains(turno)) {
            turnos.add(turno);
        }
    }

    @Override
    public List<Turno> buscarPorVeterinario(Veterinario veterinario) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : turnos) {
            if (t.getVeterinario().getDni().equals(veterinario.getDni())) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    @Override
    public List<Turno> listarTodos() {
        return new ArrayList<>(turnos);
    }
}
