package repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Especie;
import modelo.Producto;
import modelo.Vacuna;

/**
 * Formato de cada línea:
 * idVacuna;nombre;edadMinimaMeses;codigoInsumo;intervalos
 * intervalos = ESPECIE=meses separados por '|', por ejemplo: PERRO=12|GATO=12
 * El insumo se busca en el RepositorioProductos por código.
 */
public class RepositorioVacunasArchivo extends ArchivoTexto implements RepositorioVacunas {
    private final Map<String, Vacuna> items = new LinkedHashMap<>();
    private final RepositorioProductos repositorioProductos;

    public RepositorioVacunasArchivo(String rutaArchivo, RepositorioProductos repositorioProductos) {
        super(rutaArchivo);
        if (repositorioProductos == null) {
            throw new IllegalArgumentException("El repositorio de productos es obligatorio.");
        }
        this.repositorioProductos = repositorioProductos;
        for (String linea : leerLineas()) {
            Vacuna v = desdeLinea(linea);
            items.put(v.getNombre(), v);
        }
    }

    @Override
    public void guardar(Vacuna vacuna) {
        if (vacuna != null) {
            items.put(vacuna.getNombre(), vacuna);
            persistir();
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

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Vacuna v : items.values()) {
            StringBuilder intervalos = new StringBuilder();
            for (Map.Entry<Especie, Integer> e : v.getIntervalosPorEspecie().entrySet()) {
                if (intervalos.length() > 0) {
                    intervalos.append('|');
                }
                intervalos.append(e.getKey()).append('=').append(e.getValue());
            }
            lineas.add(unir(v.getIdVacuna(), v.getNombre(), v.getEdadMinimaMeses(), v.getInsumo().getCodigo(), intervalos));
        }
        escribirLineas(lineas);
    }

    private Vacuna desdeLinea(String linea) {
        try {
            String[] c = separar(linea);
            Producto insumo = repositorioProductos.buscarPorCodigo(c[3]);
            if (insumo == null) {
                throw new IllegalStateException("No existe el producto " + c[3] + " asociado a la vacuna.");
            }
            Vacuna v = new Vacuna(c[0], c[1], Integer.parseInt(c[2]), insumo);
            if (!c[4].isEmpty()) {
                for (String par : c[4].split("\\|")) {
                    String[] especieYMeses = par.split("=");
                    v.agregarEspecie(Especie.valueOf(especieYMeses[0]), Integer.parseInt(especieYMeses[1]));
                }
            }
            return v;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
