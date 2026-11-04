package repositorio;

import java.util.List;

import modelo.Factura;

public interface RepositorioFacturas {
    void guardar(Factura factura);
    Factura buscarPorNumero(String numero);
    List<Factura> listarTodas();
}
