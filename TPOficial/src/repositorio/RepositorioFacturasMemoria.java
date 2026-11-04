package repositorio;

import java.util.LinkedHashMap;
import java.util.Map;

import modelo.Factura;

public class RepositorioFacturasMemoria implements RepositorioFacturas {
    private final Map<String, Factura> facturas = new LinkedHashMap<>();

    @Override
    public void guardar(Factura factura) {
        if (factura != null) {
            facturas.put(factura.getNumeroFactura(), factura);
        }
    }

    @Override
    public Factura buscarPorNumero(String numero) {
        return facturas.get(numero);
    }
}
