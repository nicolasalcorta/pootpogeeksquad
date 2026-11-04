package repositorio;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.HorarioAtencion;
import modelo.Personal;
import modelo.RolPersonal;
import modelo.Veterinario;

/**
 * Formato de cada línea:
 * id;nombre;apellido;dni;telefono;legajo;rol;matricula;especialidad;deGuardia;horarios
 * Los últimos cuatro campos solo se completan para veterinarios.
 * horarios = DIA inicio-fin separados por '|', por ejemplo: MONDAY 09:00-13:00|TUESDAY 14:00-18:00
 */
public class RepositorioPersonalArchivo extends ArchivoTexto implements RepositorioPersonal {
    private final Map<String, Personal> items = new LinkedHashMap<>();

    public RepositorioPersonalArchivo(String rutaArchivo) {
        super(rutaArchivo);
        for (String linea : leerLineas()) {
            Personal p = desdeLinea(linea);
            items.put(p.getDni(), p);
        }
    }

    @Override
    public void guardar(Personal personal) {
        if (personal != null) {
            items.put(personal.getDni(), personal);
            persistir();
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

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Personal p : items.values()) {
            lineas.add(aLinea(p));
        }
        escribirLineas(lineas);
    }

    private String aLinea(Personal p) {
        if (p instanceof Veterinario) {
            Veterinario v = (Veterinario) p;
            StringBuilder horarios = new StringBuilder();
            for (HorarioAtencion h : v.getHorarios()) {
                if (horarios.length() > 0) {
                    horarios.append('|');
                }
                horarios.append(h.getDiaSemana()).append(' ').append(h.getHoraInicio()).append('-').append(h.getHoraFin());
            }
            return unir(v.getId(), v.getNombre(), v.getApellido(), v.getDni(), v.getTelefono(), v.getLegajo(),
                    v.getRol(), v.getMatricula(), v.getEspecialidad(), v.isDeGuardia(), horarios);
        }
        return unir(p.getId(), p.getNombre(), p.getApellido(), p.getDni(), p.getTelefono(), p.getLegajo(),
                p.getRol(), "", "", "", "");
    }

    private Personal desdeLinea(String linea) {
        try {
            String[] c = separar(linea);
            RolPersonal rol = RolPersonal.valueOf(c[6]);
            if (rol != RolPersonal.VETERINARIO) {
                return new Personal(c[0], c[1], c[2], c[3], c[4], c[5], rol);
            }
            Veterinario v = new Veterinario(c[0], c[1], c[2], c[3], c[4], c[5], c[7], c[8]);
            v.setDeGuardia(Boolean.parseBoolean(c[9]));
            if (!c[10].isEmpty()) {
                for (String h : c[10].split("\\|")) {
                    String[] diaYHoras = h.split(" ");
                    String[] horas = diaYHoras[1].split("-");
                    v.agregarHorario(new HorarioAtencion(DayOfWeek.valueOf(diaYHoras[0]),
                            LocalTime.parse(horas[0]), LocalTime.parse(horas[1])));
                }
            }
            return v;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
