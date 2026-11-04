package controlador;

import modelo.Consulta;
import modelo.Dueño;
import modelo.EstadoTurno;
import modelo.HistoriaClinica;
import modelo.Lote;
import modelo.Mascota;
import modelo.NotaRectificatoria;
import modelo.Producto;
import modelo.Tratamiento;
import modelo.Turno;
import modelo.Vacuna;
import modelo.VacunaAplicada;
import modelo.Veterinario;
import repositorio.RepositorioMascotas;

public class GestorConsultas {
    private final RepositorioMascotas repositorioMascotas;

    public GestorConsultas(RepositorioMascotas repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio.");
        }
        this.repositorioMascotas = repositorio;
    }

    public Consulta registrarConsulta(Turno turno, Veterinario veterinario, String motivo, double pesoDelDia) {
        if (turno == null || veterinario == null) {
            throw new IllegalArgumentException("El turno y el veterinario son obligatorios.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la consulta es obligatorio.");
        }
        if (pesoDelDia < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo.");
        }
        if (turno.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede registrar una consulta sobre un turno programado.");
        }
        if (!turno.isSeniaPagada()) {
            throw new IllegalStateException("El turno requiere el pago de la seña antes de ser atendido.");
        }
        if (!turno.getVeterinario().getDni().equals(veterinario.getDni())) {
            throw new IllegalStateException("El turno pertenece a otro veterinario.");
        }

        Mascota mascota = turno.getMascota();
        Consulta consulta = mascota.getHistoriaClinica().crearConsulta(motivo, pesoDelDia);
        consulta.setVeterinario(veterinario);
        consulta.setTurno(turno);
        if (pesoDelDia > 0) {
            mascota.setPesoActual(pesoDelDia);
        }

        turno.setEstado(EstadoTurno.ATENDIDO);
        Dueño dueño = mascota.getDueño();
        if (dueño != null) {
            dueño.reiniciarAusencias();
        }
        repositorioMascotas.guardar(mascota);
        return consulta;
    }

    public NotaRectificatoria registrarNotaRectificatoria(Consulta consulta, Veterinario veterinario, String descripcion) {
        if (consulta == null || veterinario == null) {
            throw new IllegalArgumentException("La consulta y el veterinario son obligatorios.");
        }
        Mascota mascota = mascotaDe(consulta);
        NotaRectificatoria nota = mascota.getHistoriaClinica().agregarNota(consulta, veterinario, descripcion);
        repositorioMascotas.guardar(mascota);
        return nota;
    }

    public Tratamiento agregarTratamiento(Consulta consulta, Veterinario veterinario, Producto medicamento,
                                          String dosis, String frecuencia, int duracionDias) {
        validarConsultaDelVeterinario(consulta, veterinario);
        if (medicamento == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio.");
        }
        // Primero el peso (lo valida Consulta y lanza PesoNoRegistradoException); después el stock.
        if (consulta.tienePesoRegistrado() && !medicamento.tieneStock(1)) {
            throw new IllegalStateException("No hay stock disponible de " + medicamento.getNombre() + ".");
        }
        Tratamiento tratamiento = consulta.agregarTratamiento(medicamento, dosis, frecuencia, duracionDias);
        repositorioMascotas.guardar(mascotaDe(consulta));
        return tratamiento;
    }

    public VacunaAplicada aplicarVacuna(Consulta consulta, Veterinario veterinario, Vacuna vacuna, Lote lote) {
        validarConsultaDelVeterinario(consulta, veterinario);
        VacunaAplicada aplicada = consulta.aplicarVacuna(vacuna, lote);
        repositorioMascotas.guardar(mascotaDe(consulta));
        return aplicada;
    }

    public HistoriaClinica consultarHistoria(Mascota mascota) {
        if (mascota == null) {
            throw new IllegalArgumentException("La mascota no puede ser nula.");
        }
        return mascota.getHistoriaClinica();
    }

    private void validarConsultaDelVeterinario(Consulta consulta, Veterinario veterinario) {
        if (consulta == null || veterinario == null) {
            throw new IllegalArgumentException("La consulta y el veterinario son obligatorios.");
        }
        if (consulta.getVeterinario() == null || !consulta.getVeterinario().getDni().equals(veterinario.getDni())) {
            throw new IllegalStateException("Solo el veterinario que atiende la consulta puede modificarla.");
        }
    }

    private Mascota mascotaDe(Consulta consulta) {
        if (consulta.getTurno() == null) {
            throw new IllegalStateException("La consulta no tiene un turno asociado.");
        }
        return consulta.getTurno().getMascota();
    }
}
