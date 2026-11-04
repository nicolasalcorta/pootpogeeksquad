package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Producto {
    private final String codigo;
    private final String nombre;
    private final int stockMinimo;
    private final double precio;
    private final List<Lote> lotes;

    public Producto(String codigo, String nombre, int stockMinimo, double precio) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.stockMinimo = stockMinimo;
        this.precio = precio;
        this.lotes = new ArrayList<>();
    }

    public void agregarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        lotes.add(lote);
    }

    /** Stock utilizable: suma de los lotes que no están vencidos. */
    public int getStockActual() {
        int total = 0;
        for (Lote l : lotes) {
            if (!l.estaVencido(LocalDate.now())) {
                total += l.getCantidad();
            }
        }
        return total;
    }

    public boolean hayAlertaStock() {
        return getStockActual() <= stockMinimo;
    }

    public boolean tieneStock(int cantidad) {
        return cantidad > 0 && getStockActual() >= cantidad;
    }

    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (!tieneStock(cantidad)) {
            throw new IllegalStateException("Stock insuficiente de " + nombre + ": se piden " + cantidad
                    + " y hay " + getStockActual() + " disponibles.");
        }
        List<Lote> utilizables = new ArrayList<>();
        for (Lote l : lotes) {
            if (!l.estaVencido(LocalDate.now()) && l.getCantidad() > 0) {
                utilizables.add(l);
            }
        }
        Collections.sort(utilizables);
        int restante = cantidad;
        for (Lote l : utilizables) {
            int toma = Math.min(restante, l.getCantidad());
            l.descontar(toma);
            restante -= toma;
            if (restante == 0) {
                break;
            }
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public double getPrecio() {
        return precio;
    }

    public List<Lote> getLotes() {
        return Collections.unmodifiableList(lotes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        return codigo.equals(((Producto) o).codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " [" + codigo + "] stock: " + getStockActual() + " (mín. " + stockMinimo + ")";
    }
}
