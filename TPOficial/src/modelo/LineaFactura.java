package modelo;

public class LineaFactura {
    private final String concepto;
    private final int cantidad;
    private final double precioUnitario;

    public LineaFactura(String concepto, int cantidad, double precioUnitario) {
        if (concepto == null || concepto.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto de la línea es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
        }
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public String getConcepto() {
        return concepto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }
}
