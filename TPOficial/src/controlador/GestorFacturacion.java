package controlador;

import java.util.List;

import excepciones.FacturaAnuladaException;
import excepciones.RolNoAutorizadoException;
import modelo.ConceptoFactura;
import modelo.Consulta;
import modelo.Personal;
import modelo.RolPersonal;
import modelo.Duenio;
import modelo.Factura;
import modelo.MedioPago;
import modelo.NotaCredito;
import modelo.Producto;
import modelo.TipoAtencion;
import modelo.Tratamiento;
import modelo.VacunaAplicada;
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

        // Cada vacuna aplicada se cobra al precio de su producto de stock (una dosis).
        for (VacunaAplicada v : consulta.getVacunas()) {
            Producto insumo = v.getVacuna().getInsumo();
            factura.agregarLinea("Vacuna " + v.getNombreVacuna() + " (lote " + v.getLote().getNumeroLote() + ")",
                    1, insumo.getPrecio());
        }

        factura.calcularTotal();
        repositorioFacturas.guardar(factura);
        consulta.setFactura(factura);
        return factura;
    }

    /** Factura un cargo que no proviene de una consulta: la seña de un turno o una cancelación tardía. */
    public Factura emitirFacturaCargo(Duenio duenio, ConceptoFactura concepto, double monto, MedioPago medioPago) {
        if (duenio == null || concepto == null || medioPago == null) {
            throw new IllegalArgumentException("El dueño, el concepto y el medio de pago son obligatorios.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del cargo debe ser mayor a cero.");
        }
        Factura factura = new Factura(concepto, duenio, medioPago);
        factura.agregarLinea(descripcionDe(concepto), 1, monto);
        factura.calcularTotal();
        repositorioFacturas.guardar(factura);
        return factura;
    }

    public NotaCredito anularFactura(Factura factura, Personal usuario)
            throws FacturaAnuladaException, RolNoAutorizadoException {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula.");
        }
        ValidadorRol.exigir(usuario, RolPersonal.ADMINISTRADOR, "anular facturas");
        NotaCredito nota = factura.anularConNotaCredito();
        repositorioFacturas.guardar(factura);
        return nota;
    }

    public List<Factura> listarFacturas() {
        return repositorioFacturas.listarTodas();
    }

    private String descripcionDe(ConceptoFactura concepto) {
        switch (concepto) {
            case SENIA:
                return "Seña de turno";
            case CARGO_CANCELACION:
                return "Cargo por cancelación tardía";
            default:
                return "Consulta";
        }
    }
}
