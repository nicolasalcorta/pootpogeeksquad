package controlador;

import modelo.Duenio;
import modelo.Mascota;
import repositorio.RepositorioDuenios;
import repositorio.RepositorioMascotas;

public class GestorPacientes {
    private final RepositorioDuenios repositorioDuenios;
    private final RepositorioMascotas repositorioMascotas;

    public GestorPacientes(RepositorioDuenios repoDuenios, RepositorioMascotas repoMascotas) {
        if (repoDuenios == null || repoMascotas == null) {
            throw new IllegalArgumentException("Los repositorios son obligatorios.");
        }
        this.repositorioDuenios = repoDuenios;
        this.repositorioMascotas = repoMascotas;
    }

    public void registrarDuenio(Duenio duenio) {
        if (duenio == null) {
            throw new IllegalArgumentException("El dueño no puede ser nulo.");
        }
        if (repositorioDuenios.buscarPorDni(duenio.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe un dueño registrado con el DNI " + duenio.getDni() + ".");
        }
        repositorioDuenios.guardar(duenio);
    }

    public void registrarMascota(Duenio duenio, Mascota mascota) {
        if (duenio == null || mascota == null) {
            throw new IllegalArgumentException("El dueño y la mascota son obligatorios.");
        }
        if (repositorioDuenios.buscarPorDni(duenio.getDni()) == null) {
            throw new IllegalArgumentException("El dueño con DNI " + duenio.getDni() + " no está registrado.");
        }
        if (repositorioMascotas.buscarPorId(mascota.getIdMascota()) != null) {
            throw new IllegalArgumentException("Ya existe una mascota con el id " + mascota.getIdMascota() + ".");
        }
        if (mascota.getDuenio() == null) {
            mascota.setDuenio(duenio);
        } else if (mascota.getDuenio() != duenio) {
            throw new IllegalArgumentException("La mascota pertenece a otro dueño.");
        }
        duenio.agregarMascota(mascota);
        repositorioMascotas.guardar(mascota);
        repositorioDuenios.guardar(duenio);
    }

    public void transferirMascota(Mascota mascota, Duenio nuevoDuenio) {
        if (mascota == null || nuevoDuenio == null) {
            throw new IllegalArgumentException("La mascota y el nuevo dueño son obligatorios.");
        }
        if (repositorioDuenios.buscarPorDni(nuevoDuenio.getDni()) == null) {
            throw new IllegalArgumentException("El nuevo dueño no está registrado.");
        }
        Duenio anterior = mascota.getDuenio();
        if (anterior == nuevoDuenio) {
            throw new IllegalArgumentException("La mascota ya pertenece a ese dueño.");
        }
        mascota.cambiarDuenio(nuevoDuenio);
        repositorioMascotas.guardar(mascota);
        repositorioDuenios.guardar(nuevoDuenio);
        if (anterior != null) {
            repositorioDuenios.guardar(anterior);
        }
    }

    public Duenio buscarDuenioPorDni(String dni) {
        return repositorioDuenios.buscarPorDni(dni);
    }
}
