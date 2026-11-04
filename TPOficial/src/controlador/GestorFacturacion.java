package controlador;

import excepciones.FacturaAnuladaException;
import modelo.Consulta;
import modelo.Duenio;
import modelo.Factura;
import modelo.MedioPago;
import modelo.NotaCredito;
import modelo.Producto;
import modelo.TipoAtencion;
import modelo.Tratamiento;
import repositorio.RepositorioFacturas;

public class GestorFacturacion {
    private final RepositorioFacturas repositorioFacturas;

    public GestorFacturacion(RepositorioFacturas repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio.");
        }
        this.repositorioFacturas = repositorio;
    }

    public Factura emitirFactura(Consulta consulta, MedioPago medioPago) {
        if (consulta == null || medioPago == null) {
            throw new IllegalArgumentException("La consulta y el medio de pago son obligatorios.");
        }
        if (!consulta.isConfirmada()) {
            throw new IllegalStateException("Solo se puede facturar una consulta confirmada.");
        }
        if (consulta.getFactura() != null && !consulta.getFactura().isAnulada()) {
            throw new IllegalStateException("La consulta ya tiene una factura emitida.");
        }
        if (consulta.getTurno() == null) {
            throw new IllegalStateException("La consulta no tiene un turno asociado.");
        }
        Duenio duenio = consulta.getTurno().getMascota().getDuenio();
        if (duenio == null) {
            throw new IllegalStateException("La mascota no tiene dueño responsable.");
        }

        Factura factura = new Factura(consulta, duenio, medioPago);

        TipoAtencion tipo = consulta.getTurno().getTipo();
        factura.agregarLinea("Consulta (" + tipo + ")", 1, tipo.getPrecioBase());

        for (Tratamiento t : consulta.getTratamientos()) {
            Producto medicamento = t.getMedicamento();
            factura.agregarLinea(medicamento.getNombre(), t.getCantidad(), medicamento.getPrecio());
        }

        factura.calcularTotal();
        repositorioFacturas.guardar(factura);
        consulta.setFactura(factura);
        return factura;
    }

    public NotaCredito anularFactura(Factura factura) throws FacturaAnuladaException {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula.");
        }
        NotaCredito nota = factura.anularConNotaCredito();
        repositorioFacturas.guardar(factura);
        return nota;
    }
}
