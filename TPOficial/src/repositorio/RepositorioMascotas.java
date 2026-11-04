package repositorio;

import java.util.List;

import modelo.Mascota;

public interface RepositorioMascotas {
    void guardar(Mascota mascota);
    Mascota buscarPorId(String idMascota);
    List<Mascota> listarTodas();
}
