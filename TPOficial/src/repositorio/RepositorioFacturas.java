package repositorio;

import modelo.Factura;

public interface RepositorioFacturas {
    void guardar(Factura factura);
    Factura buscarPorNumero(String numero);
}
