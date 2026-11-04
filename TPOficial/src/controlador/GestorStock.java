package controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.StockInsuficienteException;
import modelo.Consulta;
import modelo.Lote;
import modelo.Producto;
import modelo.Tratamiento;
import modelo.VacunaAplicada;
import repositorio.RepositorioProductos;

public class GestorStock {
    private final RepositorioProductos repositorioProductos;

    public GestorStock(RepositorioProductos repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio.");
        }
        this.repositorioProductos = repositorio;
    }

    public void confirmarConsumo(Consulta consulta) throws StockInsuficienteException {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta no puede ser nula.");
        }
        if (consulta.isConfirmada()) {
            throw new IllegalStateException("La consulta ya fue confirmada.");
        }

        // Tratamientos: se descuenta del producto (lotes por vencimiento más próximo).
        Map<Producto, Integer> requeridos = new LinkedHashMap<>();
        for (Tratamiento t : consulta.getTratamientos()) {
            requeridos.merge(t.getMedicamento(), t.getCantidad(), Integer::sum);
        }
        // Vacunas: se descuenta una dosis del lote que se usó.
        Map<Lote, Integer> dosisPorLote = new LinkedHashMap<>();
        for (VacunaAplicada v : consulta.getVacunas()) {
            dosisPorLote.merge(v.getLote(), 1, Integer::sum);
        }

        // Se valida todo antes de descontar, para no dejar el stock a medias.
        for (Map.Entry<Producto, Integer> e : requeridos.entrySet()) {
            if (!e.getKey().tieneStock(e.getValue())) {
                throw new StockInsuficienteException("No hay stock suficiente de " + e.getKey().getNombre()
                        + ": se necesitan " + e.getValue() + " y hay " + e.getKey().getStockActual() + ".");
            }
        }
        for (Map.Entry<Lote, Integer> e : dosisPorLote.entrySet()) {
            if (e.getKey().getCantidad() < e.getValue()) {
                throw new StockInsuficienteException("No quedan dosis suficientes en el lote " + e.getKey().getNumeroLote()
                        + ": se necesitan " + e.getValue() + " y hay " + e.getKey().getCantidad() + ".");
            }
        }

        for (Map.Entry<Producto, Integer> e : requeridos.entrySet()) {
            e.getKey().descontarStock(e.getValue());
            repositorioProductos.guardar(e.getKey());
        }
        for (VacunaAplicada v : consulta.getVacunas()) {
            v.getLote().descontar(1);
            repositorioProductos.guardar(v.getVacuna().getInsumo());
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
