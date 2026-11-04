package controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import modelo.Consulta;
import modelo.Mascota;
import modelo.VacunaAplicada;
import notificacion.Notificador;
import repositorio.RepositorioMascotas;

public class GestorVacunas {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RepositorioMascotas repositorioMascotas;
    private final Notificador notificador;

    public GestorVacunas(RepositorioMascotas repositorio, Notificador notificador) {
        if (repositorio == null || notificador == null) {
            throw new IllegalArgumentException("El repositorio y el notificador son obligatorios.");
        }
        this.repositorioMascotas = repositorio;
        this.notificador = notificador;
    }

    public List<VacunaAplicada> vacunasProximasAVencer() {
        List<VacunaAplicada> resultado = new ArrayList<>();
        for (Pendiente p : buscarPendientes()) {
            resultado.add(p.vacuna);
        }
        return resultado;
    }

    public void emitirRecordatorios() {
        for (Pendiente p : buscarPendientes()) {
            if (p.mascota.getDueño() != null) {
                notificador.notificar(p.mascota.getDueño(),
                        "Recordatorio: el refuerzo de la vacuna " + p.vacuna.getNombreVacuna() + " de " + p.mascota.getNombre()
                                + " vence el " + p.vacuna.getFechaProximoRefuerzo().format(FORMATO) + ".");
            }
        }
    }

    private List<Pendiente> buscarPendientes() {
        List<Pendiente> pendientes = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (Mascota m : repositorioMascotas.listarTodas()) {
            for (Consulta c : m.getHistoriaClinica().getConsultas()) {
                for (VacunaAplicada v : c.getVacunas()) {
                    if (v.requiereRecordatorio(hoy) && !fueReforzada(m, v)) {
                        pendientes.add(new Pendiente(m, v));
                    }
                }
            }
        }
        return pendientes;
    }

    private boolean fueReforzada(Mascota mascota, VacunaAplicada original) {
        if (original.getVacuna() == null) {
            return false;
        }
        for (Consulta c : mascota.getHistoriaClinica().getConsultas()) {
            for (VacunaAplicada otra : c.getVacunas()) {
                if (otra != original && otra.getVacuna() != null
                        && otra.getVacuna().getIdVacuna().equals(original.getVacuna().getIdVacuna())
                        && otra.getFechaAplicacion().isAfter(original.getFechaAplicacion())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class Pendiente {
        private final Mascota mascota;
        private final VacunaAplicada vacuna;

        private Pendiente(Mascota mascota, VacunaAplicada vacuna) {
            this.mascota = mascota;
            this.vacuna = vacuna;
        }
    }
}
