package controlador;

import modelo.Consulta;
import modelo.Dueño;
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
        Dueño dueño = consulta.getTurno().getMascota().getDueño();
        if (dueño == null) {
            throw new IllegalStateException("La mascota no tiene dueño responsable.");
        }

        Factura factura = new Factura(consulta, dueño, medioPago);

        TipoAtencion tipo = consulta.getTurno().getTipo();
        factura.agregarLinea("Consulta (" + tipo + ")", 1, tipo.getPreciobase());

        for (Tratamiento t : consulta.getTratamientos()) {
            Producto medicamento = t.getMedicamento();
            factura.agregarLinea(medicamento.getNombre(), 1, medicamento.getPrecio());
        }

        factura.calcularTotal();
        repositorioFacturas.guardar(factura);
        consulta.setFactura(factura);
        return factura;
    }

    public NotaCredito anularFactura(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula.");
        }
        NotaCredito nota = factura.anularConNotaCredito();
        repositorioFacturas.guardar(factura);
        return nota;
    }
}
