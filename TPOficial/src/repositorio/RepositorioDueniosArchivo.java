package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Duenio;

/**
 * Formato de cada línea:
 * id;nombre;apellido;dni;telefono;mail;tienePlanSalud;ausenciasConsecutivas
 * Las mascotas se guardan en su propio archivo y se vinculan al dueño por DNI.
 */
public class RepositorioDueniosArchivo extends ArchivoTexto implements RepositorioDuenios {
    private final Map<String, Duenio> items = new LinkedHashMap<>();

    public RepositorioDueniosArchivo(String rutaArchivo) {
        super(rutaArchivo);
        for (String linea : leerLineas()) {
            Duenio d = desdeLinea(linea);
            items.put(d.getDni(), d);
        }
    }

    @Override
    public void guardar(Duenio duenio) {
        if (duenio != null) {
            items.put(duenio.getDni(), duenio);
            persistir();
        }
    }

    @Override
    public Duenio buscarPorDni(String dni) {
        return items.get(dni);
    }

    @Override
    public List<Duenio> listarTodos() {
        return new ArrayList<>(items.values());
    }

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Duenio d : items.values()) {
            lineas.add(unir(d.getId(), d.getNombre(), d.getApellido(), d.getDni(), d.getTelefono(), d.getMail(),
                    d.isTienePlanSalud(), d.getAusenciasConsecutivas()));
        }
        escribirLineas(lineas);
    }

    private Duenio desdeLinea(String linea) {
        try {
            String[] c = separar(linea);
            Duenio d = new Duenio(c[0], c[1], c[2], c[3], c[4]);
            d.setMail(vacioANull(c[5]));
            d.setTienePlanSalud(Boolean.parseBoolean(c[6]));
            // Duenio no expone un setter de ausencias: se reconstruye el contador con su propio método.
            int ausencias = Integer.parseInt(c[7]);
            for (int i = 0; i < ausencias; i++) {
                d.registrarAusencia();
            }
            return d;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
