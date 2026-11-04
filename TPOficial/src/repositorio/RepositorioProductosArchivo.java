package repositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Lote;
import modelo.Producto;

/**
 * Cada producto ocupa una línea "P" seguida de una línea "L" por cada uno de sus lotes:
 * P;codigo;nombre;stockMinimo;precio
 * L;codigoProducto;numeroLote;fechaVencimiento;cantidad
 */
public class RepositorioProductosArchivo extends ArchivoTexto implements RepositorioProductos {
    private final Map<String, Producto> items = new LinkedHashMap<>();

    public RepositorioProductosArchivo(String rutaArchivo) {
        super(rutaArchivo);
        for (String linea : leerLineas()) {
            cargarLinea(linea);
        }
    }

    @Override
    public void guardar(Producto producto) {
        if (producto != null) {
            items.put(producto.getCodigo(), producto);
            persistir();
        }
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        return items.get(codigo);
    }

    @Override
    public List<Producto> listarTodos() {
        return new ArrayList<>(items.values());
    }

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Producto p : items.values()) {
            lineas.add(unir("P", p.getCodigo(), p.getNombre(), p.getStockMinimo(), p.getPrecio()));
            for (Lote l : p.getLotes()) {
                lineas.add(unir("L", p.getCodigo(), l.getNumeroLote(), l.getFechaVencimiento(), l.getCantidad()));
            }
        }
        escribirLineas(lineas);
    }

    private void cargarLinea(String linea) {
        try {
            String[] c = separar(linea);
            if ("P".equals(c[0])) {
                Producto p = new Producto(c[1], c[2], Integer.parseInt(c[3]), Double.parseDouble(c[4]));
                items.put(p.getCodigo(), p);
            } else if ("L".equals(c[0])) {
                Producto p = items.get(c[1]);
                if (p == null) {
                    throw new IllegalStateException("El lote hace referencia a un producto inexistente.");
                }
                int cantidad = Integer.parseInt(c[4]);
                // El constructor de Lote exige cantidad inicial > 0; un lote agotado se restaura con setCantidad.
                Lote lote = new Lote(c[2], LocalDate.parse(c[3]), Math.max(cantidad, 1));
                lote.setCantidad(cantidad);
                p.agregarLote(lote);
            } else {
                throw new IllegalStateException("Tipo de registro desconocido: " + c[0]);
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
        }
    }
}
