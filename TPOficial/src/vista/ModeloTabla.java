package vista;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

/**
 * Modelo de tabla genérico: una fila por objeto y una función por columna que dice qué mostrar.
 * Así cada vista arma su tabla sin repetir el código de AbstractTableModel (TurnoTableModel lo extiende).
 */
public class ModeloTabla<T> extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final String[] columnas;
    private final List<Function<T, Object>> valores;
    private final List<T> filas = new ArrayList<>();

    @SafeVarargs
    public ModeloTabla(String[] columnas, Function<T, Object>... valores) {
        if (columnas.length != valores.length) {
            throw new IllegalArgumentException("Tiene que haber un valor por cada columna.");
        }
        this.columnas = columnas.clone();
        this.valores = new ArrayList<>();
        for (Function<T, Object> valor : valores) {
            this.valores.add(valor);
        }
    }

    public void setFilas(List<T> nuevasFilas) {
        filas.clear();
        filas.addAll(nuevasFilas);
        fireTableDataChanged();
    }

    public T getFila(int indice) {
        return filas.get(indice);
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Object valor = valores.get(columna).apply(filas.get(fila));
        return valor == null ? "" : valor;
    }
}
