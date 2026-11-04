package controlador;

import modelo.Dueño;
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

    public void registrarDuenio(Dueño duenio) {
        if (duenio == null) {
            throw new IllegalArgumentException("El dueño no puede ser nulo.");
        }
        if (repositorioDuenios.buscarPorDni(duenio.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe un dueño registrado con el DNI " + duenio.getDni() + ".");
        }
        repositorioDuenios.guardar(duenio);
    }

    public void registrarMascota(Dueño duenio, Mascota mascota) {
        if (duenio == null || mascota == null) {
            throw new IllegalArgumentException("El dueño y la mascota son obligatorios.");
        }
        if (repositorioDuenios.buscarPorDni(duenio.getDni()) == null) {
            throw new IllegalArgumentException("El dueño con DNI " + duenio.getDni() + " no está registrado.");
        }
        if (repositorioMascotas.buscarPorId(mascota.getIdMascota()) != null) {
            throw new IllegalArgumentException("Ya existe una mascota con el id " + mascota.getIdMascota() + ".");
        }
        if (mascota.getDueño() == null) {
            mascota.setDueño(duenio);
        } else if (mascota.getDueño() != duenio) {
            throw new IllegalArgumentException("La mascota pertenece a otro dueño.");
        }
        duenio.agregarMascota(mascota);
        repositorioMascotas.guardar(mascota);
        repositorioDuenios.guardar(duenio);
    }

    public void transferirMascota(Mascota mascota, Dueño nuevoDuenio) {
        if (mascota == null || nuevoDuenio == null) {
            throw new IllegalArgumentException("La mascota y el nuevo dueño son obligatorios.");
        }
        if (repositorioDuenios.buscarPorDni(nuevoDuenio.getDni()) == null) {
            throw new IllegalArgumentException("El nuevo dueño no está registrado.");
        }
        Dueño anterior = mascota.getDueño();
        if (anterior == nuevoDuenio) {
            throw new IllegalArgumentException("La mascota ya pertenece a ese dueño.");
        }
        mascota.cambiarDueño(nuevoDuenio);
        repositorioMascotas.guardar(mascota);
        repositorioDuenios.guardar(nuevoDuenio);
        if (anterior != null) {
            repositorioDuenios.guardar(anterior);
        }
    }

    public Dueño buscarDuenioPorDni(String dni) {
        return repositorioDuenios.buscarPorDni(dni);
    }
}
