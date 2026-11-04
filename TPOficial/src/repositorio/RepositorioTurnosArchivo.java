package repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import modelo.EstadoTurno;
import modelo.Mascota;
import modelo.Personal;
import modelo.TipoAtencion;
import modelo.Turno;
import modelo.Veterinario;

/**
 * Formato de cada línea:
 * fechaHora;tipo;estado;esUrgencia;seniaPagada;idMascota;dniVeterinario
 * La mascota y el veterinario se buscan en sus repositorios, así todos comparten los mismos objetos.
 */
public class RepositorioTurnosArchivo extends ArchivoTexto implements RepositorioTurnos {
    private final List<Turno> turnos = new ArrayList<>();
    private final RepositorioMascotas repositorioMascotas;
    private final RepositorioPersonal repositorioPersonal;

    public RepositorioTurnosArchivo(String rutaArchivo, RepositorioMascotas repositorioMascotas,
                                    RepositorioPersonal repositorioPersonal) {
        super(rutaArchivo);
        if (repositorioMascotas == null || repositorioPersonal == null) {
            throw new IllegalArgumentException("Los repositorios de mascotas y de personal son obligatorios.");
        }
        this.repositorioMascotas = repositorioMascotas;
        this.repositorioPersonal = repositorioPersonal;
        for (String linea : leerLineas()) {
            turnos.add(desdeLinea(linea));
        }
    }

    @Override
    public void guardar(Turno turno) {
        if (turno == null) {
            return;
        }
        // Un turno ya guardado se reescribe con su estado actual (por ejemplo, al cancelarlo o reprogramarlo).
        if (!turnos.contains(turno)) {
            turnos.add(turno);
        }
        persistir();
        // Guardado en cascada: marcar ausente un turno modifica las ausencias del dueño de la mascota.
        repositorioMascotas.guardar(turno.getMascota());
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

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Turno t : turnos) {
            lineas.add(unir(t.getFechaHora(), t.getTipo(), t.getEstado(), t.isEsUrgencia(), t.isSeniaPagada(),
                    t.getMascota().getIdMascota(), t.getVeterinario().getDni()));
        }
        escribirLineas(lineas);
    }

    private Turno desdeLinea(String linea) {
        try {
            String[] c = separar(linea);
            Mascota mascota = repositorioMascotas.buscarPorId(c[5]);
            Personal personal = repositorioPersonal.buscarPorDni(c[6]);
            if (mascota == null || !(personal instanceof Veterinario)) {
                throw new IllegalStateException("La mascota o el veterinario del turno no existen.");
            }
            Turno t = new Turno(LocalDateTime.parse(c[0]), TipoAtencion.valueOf(c[1]), mascota,
                    (Veterinario) personal, Boolean.parseBoolean(c[3]));
            t.setEstado(EstadoTurno.valueOf(c[2]));
            t.setSeniaPagada(Boolean.parseBoolean(c[4]));
            return t;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
