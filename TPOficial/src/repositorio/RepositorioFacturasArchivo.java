package repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.ConceptoFactura;
import modelo.Consulta;
import modelo.Duenio;
import modelo.Factura;
import modelo.LineaFactura;
import modelo.Mascota;
import modelo.MedioPago;
import modelo.NotaCredito;
import modelo.PagoCreditoCuotas;
import modelo.PagoDebito;
import modelo.PagoEfectivo;

/**
 * Cada factura ocupa una línea "F" seguida de una línea "I" por cada ítem:
 * F;numero;fecha;concepto;dniDuenio;medioPago;cuotas;total;idMascota;nroConsulta;numeroNC;fechaNC;montoNC
 * I;numeroFactura;concepto;cantidad;precioUnitario
 * medioPago = EFECTIVO, DEBITO o CREDITO (cuotas solo para CREDITO).
 * idMascota y nroConsulta identifican la consulta facturada (solo para concepto CONSULTA).
 * Los campos NC se completan si la factura fue anulada con nota de crédito.
 *
 * Se construye después de cargar las historias clínicas, porque las facturas de consulta las referencian.
 */
public class RepositorioFacturasArchivo extends ArchivoTexto implements RepositorioFacturas {
    private final Map<String, Factura> facturas = new LinkedHashMap<>();
    private final RepositorioDuenios repositorioDuenios;
    private final RepositorioMascotas repositorioMascotas;

    public RepositorioFacturasArchivo(String rutaArchivo, RepositorioDuenios repositorioDuenios,
                                      RepositorioMascotas repositorioMascotas) {
        super(rutaArchivo);
        if (repositorioDuenios == null || repositorioMascotas == null) {
            throw new IllegalArgumentException("Los repositorios de dueños y de mascotas son obligatorios.");
        }
        this.repositorioDuenios = repositorioDuenios;
        this.repositorioMascotas = repositorioMascotas;
        cargar();
    }

    @Override
    public void guardar(Factura factura) {
        if (factura != null) {
            facturas.put(factura.getNumeroFactura(), factura);
            persistir();
        }
    }

    @Override
    public Factura buscarPorNumero(String numero) {
        return facturas.get(numero);
    }

    @Override
    public List<Factura> listarTodas() {
        return new ArrayList<>(facturas.values());
    }

    private void persistir() {
        List<String> lineas = new ArrayList<>();
        for (Factura f : facturas.values()) {
            String medio;
            String cuotas = "";
            if (f.getMedioPago() instanceof PagoCreditoCuotas) {
                medio = "CREDITO";
                cuotas = String.valueOf(((PagoCreditoCuotas) f.getMedioPago()).getCantidadCuotas());
            } else if (f.getMedioPago() instanceof PagoDebito) {
                medio = "DEBITO";
            } else {
                medio = "EFECTIVO";
            }
            String idMascota = "";
            String nroConsulta = "";
            Consulta consulta = f.getConsulta();
            if (consulta != null && consulta.getTurno() != null) {
                Mascota m = consulta.getTurno().getMascota();
                idMascota = m.getIdMascota();
                nroConsulta = String.valueOf(m.getHistoriaClinica().getConsultas().indexOf(consulta));
            }
            NotaCredito nc = f.getNotaCredito();
            lineas.add(unir("F", f.getNumeroFactura(), f.getFecha(), f.getConcepto(), f.getDuenio().getDni(), medio,
                    cuotas, f.getTotal(), idMascota, nroConsulta,
                    nc == null ? "" : nc.getNumero(), nc == null ? "" : nc.getFecha(), nc == null ? "" : nc.getMonto()));
            for (LineaFactura l : f.getLineas()) {
                lineas.add(unir("I", f.getNumeroFactura(), l.getConcepto(), l.getCantidad(), l.getPrecioUnitario()));
            }
        }
        escribirLineas(lineas);
    }

    /** Primero se juntan los ítems de cada factura; después se arma cada factura completa. */
    private void cargar() {
        List<String[]> cabeceras = new ArrayList<>();
        Map<String, List<LineaFactura>> items = new LinkedHashMap<>();
        for (String linea : leerLineas()) {
            try {
                String[] c = separar(linea);
                if ("F".equals(c[0])) {
                    cabeceras.add(c);
                    items.putIfAbsent(c[1], new ArrayList<>());
                } else if ("I".equals(c[0])) {
                    items.computeIfAbsent(c[1], k -> new ArrayList<>())
                            .add(new LineaFactura(c[2], Integer.parseInt(c[3]), Double.parseDouble(c[4])));
                } else {
                    throw new IllegalStateException("Tipo de registro desconocido: " + c[0]);
                }
            } catch (RuntimeException e) {
                throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
            }
        }
        for (String[] c : cabeceras) {
            try {
                Factura f = desdeCabecera(c, items.get(c[1]));
                facturas.put(f.getNumeroFactura(), f);
                if (f.getConsulta() != null) {
                    f.getConsulta().setFactura(f);
                }
            } catch (RuntimeException e) {
                throw new IllegalStateException("Factura inválida en " + getRuta() + ": " + unir((Object[]) c), e);
            }
        }
    }

    private Factura desdeCabecera(String[] c, List<LineaFactura> lineas) {
        Duenio duenio = repositorioDuenios.buscarPorDni(c[4]);
        if (duenio == null) {
            throw new IllegalStateException("No existe el dueño con DNI " + c[4] + ".");
        }
        MedioPago medio;
        switch (c[5]) {
            case "CREDITO":
                medio = new PagoCreditoCuotas(Integer.parseInt(c[6]));
                break;
            case "DEBITO":
                medio = new PagoDebito();
                break;
            default:
                medio = new PagoEfectivo();
        }
        Consulta consulta = null;
        if (!c[8].isEmpty()) {
            Mascota m = repositorioMascotas.buscarPorId(c[8]);
            if (m == null) {
                throw new IllegalStateException("No existe la mascota " + c[8] + ".");
            }
            consulta = m.getHistoriaClinica().getConsultas().get(Integer.parseInt(c[9]));
        }
        NotaCredito nc = c[10].isEmpty() ? null
                : new NotaCredito(c[10], LocalDateTime.parse(c[11]), Double.parseDouble(c[12]));
        return Factura.restaurar(c[1], LocalDateTime.parse(c[2]), ConceptoFactura.valueOf(c[3]), consulta, duenio,
                medio, lineas, Double.parseDouble(c[7]), nc);
    }
}
