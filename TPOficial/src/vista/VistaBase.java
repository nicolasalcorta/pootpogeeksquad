package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import excepciones.ReglaNegocioException;
import modelo.MedioPago;
import modelo.PagoCreditoCuotas;
import modelo.PagoDebito;
import modelo.PagoEfectivo;

/**
 * Base de las seis pestañas. Reúne lo que todas comparten: la sesión, cómo mostrar mensajes y errores,
 * y pequeñas utilidades para armar combos, tablas y leer datos de los campos.
 * Las vistas no tienen reglas de negocio: piden datos, llaman al gestor y muestran el resultado.
 */
public abstract class VistaBase extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    protected final SesionUsuario sesion;

    protected VistaBase(SesionUsuario sesion) {
        super(new BorderLayout(8, 8));
        this.sesion = sesion;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /** Vuelve a leer los datos de los gestores y actualiza las tablas. */
    public abstract void refrescar();

    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
    }

    protected void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operación realizada", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Una acción de la vista que puede fallar por una regla de negocio o por un dato mal ingresado. */
    @FunctionalInterface
    protected interface Accion {
        void ejecutar() throws ReglaNegocioException;
    }

    /**
     * Ejecuta la acción de un botón: si el gestor rechaza la operación muestra el motivo con mostrarError()
     * y, pase lo que pase, refresca la vista para que muestre el estado real.
     */
    protected void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (ReglaNegocioException e) {
            mostrarError(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        } finally {
            refrescar();
        }
    }

    // ----- Armado de componentes -----

    protected static JButton crearBoton(String texto, Runnable alHacerClic) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> alHacerClic.run());
        return btn;
    }

    protected static JPanel crearBarraBotones(JButton... botones) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (JButton btn : botones) {
            pnl.add(btn);
        }
        return pnl;
    }

    protected static JTable crearTabla(ModeloTabla<?> modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(false);
        return tabla;
    }

    /** Envuelve una tabla en un panel con título y barra de desplazamiento. */
    protected static JPanel conTitulo(String titulo, JTable tabla) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder(titulo));
        pnl.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return pnl;
    }

    /** Combo que muestra cada elemento con el texto que indica la función (en lugar de su toString()). */
    protected static <T> JComboBox<T> crearCombo(List<T> elementos, Function<T, String> texto) {
        JComboBox<T> cmb = new JComboBox<>();
        for (T elemento : elementos) {
            cmb.addItem(elemento);
        }
        cmb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                String textoCelda = valor == null ? "" : texto.apply((T) valor);
                return super.getListCellRendererComponent(lista, textoCelda, indice, seleccionado, foco);
            }
        });
        return cmb;
    }

    // ----- Lectura de datos -----

    /** Devuelve el elemento elegido en la tabla, o muestra un aviso y devuelve null si no hay ninguno. */
    protected <T> T seleccionado(JTable tabla, ModeloTabla<T> modelo, String queSeleccionar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Primero seleccioná " + queSeleccionar + " en la tabla.");
            return null;
        }
        return modelo.getFila(tabla.convertRowIndexToModel(fila));
    }

    @SuppressWarnings("unchecked")
    protected static <T> T elegido(JComboBox<T> cmb, String queElegir) {
        Object valor = cmb.getSelectedItem();
        if (valor == null) {
            throw new IllegalArgumentException("No hay " + queElegir + " para elegir.");
        }
        return (T) valor;
    }

    protected static String texto(javax.swing.text.JTextComponent campo, String nombre) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El campo \"" + nombre + "\" es obligatorio.");
        }
        return valor;
    }

    protected static LocalDateTime leerFechaHora(javax.swing.text.JTextComponent campo, String nombre) {
        try {
            return LocalDateTime.parse(texto(campo, nombre), FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("\"" + nombre + "\" debe tener el formato dd/MM/aaaa HH:mm.");
        }
    }

    protected static LocalDate leerFecha(javax.swing.text.JTextComponent campo, String nombre) {
        try {
            return LocalDate.parse(texto(campo, nombre), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("\"" + nombre + "\" debe tener el formato dd/MM/aaaa.");
        }
    }

    protected static int leerEntero(javax.swing.text.JTextComponent campo, String nombre) {
        try {
            return Integer.parseInt(texto(campo, nombre));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + nombre + "\" debe ser un número entero.");
        }
    }

    protected static double leerDecimal(javax.swing.text.JTextComponent campo, String nombre) {
        try {
            return Double.parseDouble(texto(campo, nombre).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + nombre + "\" debe ser un número.");
        }
    }

    /** Pide el medio de pago. Devuelve null si el usuario cancela. */
    protected MedioPago pedirMedioPago(String titulo) {
        JComboBox<String> cmbMedio = new JComboBox<>(new String[] {"Efectivo", "Débito", "Crédito en cuotas"});
        JSpinner spnCuotas = new JSpinner(new SpinnerNumberModel(3, 1, 24, 1));
        Formulario frm = new Formulario()
                .agregar("Medio de pago:", cmbMedio)
                .agregar("Cuotas (solo crédito):", spnCuotas);
        if (!frm.mostrar(this, titulo)) {
            return null;
        }
        switch (cmbMedio.getSelectedIndex()) {
            case 1:
                return new PagoDebito();
            case 2:
                return new PagoCreditoCuotas((Integer) spnCuotas.getValue());
            default:
                return new PagoEfectivo();
        }
    }

    // ----- Formato -----

    public static String formatear(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(FORMATO_FECHA_HORA);
    }

    public static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    public static String moneda(double importe) {
        return String.format("$ %,.2f", importe);
    }

    protected static JLabel ayuda(String texto) {
        return new JLabel("<html><i>" + texto + "</i></html>");
    }
}
