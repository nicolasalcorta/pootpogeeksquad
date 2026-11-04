package repositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Ave;
import modelo.Conejo;
import modelo.Duenio;
import modelo.Especie;
import modelo.Gato;
import modelo.Mascota;
import modelo.Perro;

/**
 * Formato de cada línea:
 * especie;id;nombre;raza;sexo;fechaNacimiento;pesoActual;dniDuenio;extra1;extra2
 * extra1/extra2 dependen de la especie:
 *   PERRO: numeroChip;tamanio   GATO: esInterior   AVE: puedeVolar   CONEJO: tieneJaula
 * El dueño se busca en el RepositorioDuenios por DNI, así ambos repositorios comparten el mismo objeto.
 * La historia clínica se guarda en un archivo aparte (ver ArchivoHistoriasClinicas y cargarHistoriasClinicas()).
 */
public class RepositorioMascotasArchivo extends ArchivoTexto implements RepositorioMascotas {
    private final Map<String, Mascota> items = new LinkedHashMap<>();
    private final RepositorioDuenios repositorioDuenios;
    // Se asigna en cargarHistoriasClinicas(). Mientras sea null no se toca el archivo de historias,
    // así no se borra su contenido antes de haberlo leído.
    private ArchivoHistoriasClinicas historias;

    public RepositorioMascotasArchivo(String rutaArchivo, RepositorioDuenios repositorioDuenios) {
        super(rutaArchivo);
        if (repositorioDuenios == null) {
            throw new IllegalArgumentException("El repositorio de dueños es obligatorio.");
        }
        this.repositorioDuenios = repositorioDuenios;
        for (String linea : leerLineas()) {
            Mascota m = desdeLinea(linea);
            items.put(m.getIdMascota(), m);
        }
    }

    /**
     * Lee las historias clínicas (consultas, tratamientos, vacunas y notas) y las incorpora a las mascotas.
     * Se llama una vez que están cargados los repositorios que las historias referencian, porque los turnos
     * dependen de las mascotas y no se pueden leer en el constructor.
     */
    public void cargarHistoriasClinicas(String rutaArchivo, RepositorioTurnos turnos, RepositorioPersonal personal,
                                        RepositorioProductos productos, RepositorioVacunas vacunas) {
        if (historias != null) {
            throw new IllegalStateException("Las historias clínicas ya fueron cargadas.");
        }
        ArchivoHistoriasClinicas archivo = new ArchivoHistoriasClinicas(rutaArchivo);
        archivo.cargar(this, turnos, personal, productos, vacunas);
        this.historias = archivo;
    }

    @Override
    public void guardar(Mascota mascota) {
        if (mascota != null) {
            items.put(mascota.getIdMascota(), mascota);
            persistir();
            // Guardado en cascada: el dueño puede haber cambiado junto con la mascota
            // (por ejemplo, se reiniciaron sus ausencias al atenderla).
            if (mascota.getDuenio() != null) {
                repositorioDuenios.guardar(mascota.getDuenio());
            }
        }
    }

    @Override
    public Mascota buscarPorId(String idMascota) {
        return items.get(idMascota);
    }

    @Override
    public List<Mascota> listarTodas() {
        return new ArrayList<>(items.values());
    }

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Mascota m : items.values()) {
            lineas.add(aLinea(m));
        }
        escribirLineas(lineas);
        if (historias != null) {
            historias.guardar(items.values());
        }
    }

    private String aLinea(Mascota m) {
        String extra1 = "";
        String extra2 = "";
        if (m instanceof Perro) {
            extra1 = ((Perro) m).getNumeroChip();
            extra2 = ((Perro) m).getTamanio();
        } else if (m instanceof Gato) {
            extra1 = String.valueOf(((Gato) m).isEsInterior());
        } else if (m instanceof Ave) {
            extra1 = String.valueOf(((Ave) m).isPuedeVolar());
        } else if (m instanceof Conejo) {
            extra1 = String.valueOf(((Conejo) m).isTieneJaula());
        }
        String dniDuenio = m.getDuenio() == null ? "" : m.getDuenio().getDni();
        return unir(m.getEspecie(), m.getIdMascota(), m.getNombre(), m.getRaza(), m.getSexo(),
                m.getFechaNacimiento(), m.getPesoActual(), dniDuenio, extra1, extra2);
    }

    private Mascota desdeLinea(String linea) {
        try {
            String[] c = separar(linea);
            Especie especie = Especie.valueOf(c[0]);
            LocalDate nacimiento = c[5].isEmpty() ? null : LocalDate.parse(c[5]);
            double peso = Double.parseDouble(c[6]);
            Duenio duenio = c[7].isEmpty() ? null : repositorioDuenios.buscarPorDni(c[7]);
            switch (especie) {
                case PERRO:
                    return new Perro(c[1], c[2], c[3], c[4], nacimiento, peso, duenio, vacioANull(c[8]), vacioANull(c[9]));
                case GATO:
                    return new Gato(c[1], c[2], c[3], c[4], nacimiento, peso, duenio, Boolean.parseBoolean(c[8]));
                case AVE:
                    return new Ave(c[1], c[2], c[3], c[4], nacimiento, peso, duenio, Boolean.parseBoolean(c[8]));
                case CONEJO:
                    return new Conejo(c[1], c[2], c[3], c[4], nacimiento, peso, duenio, Boolean.parseBoolean(c[8]));
                default:
                    throw new IllegalStateException("Especie desconocida: " + especie);
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
