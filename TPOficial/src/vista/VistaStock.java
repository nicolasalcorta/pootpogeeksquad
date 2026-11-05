package vista;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import controlador.GestorStock;
import modelo.Lote;
import modelo.Producto;

/** Pestaña de stock (solo administrador): productos, lotes y alertas de stock bajo. */
public class VistaStock extends VistaBase {
    private static final long serialVersionUID = 1L;

    private final GestorStock gestorStock;

    private final ModeloTabla<Producto> modeloProductos = new ModeloTabla<>(
            new String[] {"Código", "Producto", "Stock", "Mínimo", "Precio", "Lotes", "Alerta"},
            p -> p.getCodigo(),
            p -> p.getNombre(),
            p -> p.getStockActual(),
            p -> p.getStockMinimo(),
            p -> moneda(p.getPrecio()),
            p -> p.getLotes().size(),
            p -> p.hayAlertaStock() ? "STOCK BAJO" : "");
    private final ModeloTabla<Lote> modeloLotes = new ModeloTabla<>(
            new String[] {"Lote", "Vencimiento", "Cantidad"},
            l -> l.getNumeroLote(),
            l -> formatear(l.getFechaVencimiento()),
            l -> l.getCantidad());
    private final JTable tablaProductos = crearTabla(modeloProductos);
    private final JTable tablaLotes = crearTabla(modeloLotes);
    private final JCheckBox chkSoloStockBajo = new JCheckBox("Ver solo stock bajo");

    public VistaStock(SesionUsuario sesion, GestorStock gestorStock) {
        super(sesion);
        this.gestorStock = gestorStock;

        chkSoloStockBajo.addActionListener(e -> onVerStockBajo());
        JPanel pnlBotones = crearBarraBotones(
                crearBoton("Registrar producto", this::onRegistrarProducto),
                crearBoton("Ingresar lote", this::onIngresarLote));
        pnlBotones.add(chkSoloStockBajo);
        add(pnlBotones, BorderLayout.NORTH);

        // Al elegir un producto se muestran sus lotes disponibles.
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarLotesDelSeleccionado();
            }
        });
        JPanel pnlTablas = new JPanel(new BorderLayout(0, 8));
        pnlTablas.add(conTitulo("Productos", tablaProductos), BorderLayout.CENTER);
        JPanel pnlLotes = conTitulo("Lotes disponibles del producto seleccionado", tablaLotes);
        pnlLotes.setPreferredSize(new java.awt.Dimension(0, 180));
        pnlTablas.add(pnlLotes, BorderLayout.SOUTH);
        add(pnlTablas, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        List<Producto> productos = chkSoloStockBajo.isSelected()
                ? gestorStock.productosConStockBajo()
                : gestorStock.listarProductos();
        modeloProductos.setFilas(productos);
        modeloLotes.setFilas(List.of());
    }

    private void onVerStockBajo() {
        refrescar();
    }

    private void mostrarLotesDelSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        modeloLotes.setFilas(fila < 0 ? List.of() : gestorStock.lotesDisponibles(modeloProductos.getFila(fila)));
    }

    private void onRegistrarProducto() {
        JTextField txtCodigo = new JTextField(10);
        JTextField txtNombre = new JTextField(20);
        JTextField txtStockMinimo = new JTextField(5);
        JTextField txtPrecio = new JTextField(8);
        Formulario frm = new Formulario()
                .agregar("Código:", txtCodigo)
                .agregar("Nombre:", txtNombre)
                .agregar("Stock mínimo:", txtStockMinimo)
                .agregar("Precio:", txtPrecio);
        if (!frm.mostrar(this, "Registrar producto")) {
            return;
        }
        // El gestor valida que el usuario sea ADMINISTRADOR.
        ejecutar(() -> {
            Producto producto = new Producto(texto(txtCodigo, "Código"), texto(txtNombre, "Nombre"),
                    leerEntero(txtStockMinimo, "Stock mínimo"), leerDecimal(txtPrecio, "Precio"));
            gestorStock.registrarProducto(producto, sesion.getPersonal());
            mostrarMensaje("Producto " + producto.getNombre() + " registrado. Ahora ingresale un lote.");
        });
    }

    private void onIngresarLote() {
        Producto producto = seleccionado(tablaProductos, modeloProductos, "un producto");
        if (producto == null) {
            return;
        }
        JTextField txtNumero = new JTextField(10);
        JTextField txtVencimiento = new JTextField(10);
        JTextField txtCantidad = new JTextField(5);
        Formulario frm = new Formulario()
                .agregar("Número de lote:", txtNumero)
                .agregar("Vencimiento (dd/MM/aaaa):", txtVencimiento)
                .agregar("Cantidad:", txtCantidad);
        if (!frm.mostrar(this, "Ingresar lote de " + producto.getNombre())) {
            return;
        }
        ejecutar(() -> {
            Lote lote = new Lote(texto(txtNumero, "Número de lote"), leerFecha(txtVencimiento, "Vencimiento"),
                    leerEntero(txtCantidad, "Cantidad"));
            gestorStock.ingresarLote(producto, lote, sesion.getPersonal());
            mostrarMensaje("Lote " + lote.getNumeroLote() + " ingresado. Stock actual: " + producto.getStockActual() + ".");
        });
    }
}
