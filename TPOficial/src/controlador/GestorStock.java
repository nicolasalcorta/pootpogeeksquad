package controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Consulta;
import modelo.Producto;
import modelo.Tratamiento;
import repositorio.RepositorioProductos;

public class GestorStock {
    private final RepositorioProductos repositorioProductos;

    public GestorStock(RepositorioProductos repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio.");
        }
        this.repositorioProductos = repositorio;
    }

    public void confirmarConsumo(Consulta consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta no puede ser nula.");
        }
        if (consulta.isConfirmada()) {
            throw new IllegalStateException("La consulta ya fue confirmada.");
        }

        Map<Producto, Integer> requeridos = new LinkedHashMap<>();
        for (Tratamiento t : consulta.getTratamientos()) {
            requeridos.merge(t.getMedicamento(), 1, Integer::sum);
        }

        for (Map.Entry<Producto, Integer> e : requeridos.entrySet()) {
            if (!e.getKey().tieneStock(e.getValue())) {
                throw new IllegalStateException("No hay stock suficiente de " + e.getKey().getNombre()
                        + ": se necesitan " + e.getValue() + " y hay " + e.getKey().getStockActual() + ".");
            }
        }
        for (Map.Entry<Producto, Integer> e : requeridos.entrySet()) {
            e.getKey().descontarStock(e.getValue());
            repositorioProductos.guardar(e.getKey());
        }
        consulta.confirmar();
    }

    public List<Producto> productosConStockBajo() {
        List<Producto> bajos = new ArrayList<>();
        for (Producto p : repositorioProductos.listarTodos()) {
            if (p.hayAlertaStock()) {
                bajos.add(p);
            }
        }
        return bajos;
    }
}
