package controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import modelo.Consulta;
import modelo.Mascota;
import modelo.Vacuna;
import modelo.VacunaAplicada;
import notificacion.Notificador;
import repositorio.RepositorioMascotas;
import repositorio.RepositorioVacunas;

public class GestorVacunas {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RepositorioMascotas repositorioMascotas;
    private final RepositorioVacunas repositorioVacunas;
    private final Notificador notificador;

    public GestorVacunas(RepositorioMascotas repositorioMascotas, RepositorioVacunas repositorioVacunas,
                         Notificador notificador) {
        if (repositorioMascotas == null || repositorioVacunas == null || notificador == null) {
            throw new IllegalArgumentException("Los repositorios y el notificador son obligatorios.");
        }
        this.repositorioMascotas = repositorioMascotas;
        this.repositorioVacunas = repositorioVacunas;
        this.notificador = notificador;
    }

    /** Vacunas disponibles para aplicar (catálogo). */
    public List<Vacuna> listarCatalogo() {
        return repositorioVacunas.listarTodas();
    }

    public List<VacunaAplicada> vacunasProximasAVencer() {
        List<VacunaAplicada> resultado = new ArrayList<>();
        for (Pendiente p : buscarPendientes()) {
            resultado.add(p.vacuna);
        }
        return resultado;
    }

    /**
     * Avisa a los dueños de los refuerzos próximos a vencer que todavía no fueron avisados.
     * Devuelve cuántos recordatorios se enviaron.
     */
    public int emitirRecordatorios() {
        int enviados = 0;
        for (Pendiente p : buscarPendientes()) {
            if (p.mascota.getDuenio() != null && !p.vacuna.isRecordatorioEnviado()) {
                notificador.notificar(p.mascota.getDuenio(),
                        "Recordatorio: el refuerzo de la vacuna " + p.vacuna.getNombreVacuna() + " de " + p.mascota.getNombre()
                                + " vence el " + p.vacuna.getFechaProximoRefuerzo().format(FORMATO) + ".");
                p.vacuna.marcarRecordatorioEnviado();
                repositorioMascotas.guardar(p.mascota);
                enviados++;
            }
        }
        return enviados;
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
