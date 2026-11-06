package controlador;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import excepciones.PesoNoRegistradoException;
import excepciones.StockInsuficienteException;
import excepciones.VacunaNoAplicableException;

import modelo.Consulta;
import modelo.Duenio;
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
import repositorio.RepositorioTurnos;

public class GestorConsultas {
    private final RepositorioMascotas repositorioMascotas;
    private final RepositorioTurnos repositorioTurnos;
    private final GestorStock gestorStock;

    public GestorConsultas(RepositorioMascotas repositorioMascotas, RepositorioTurnos repositorioTurnos,
                           GestorStock gestorStock) {
        if (repositorioMascotas == null || repositorioTurnos == null || gestorStock == null) {
            throw new IllegalArgumentException("Los repositorios y el gestor de stock son obligatorios.");
        }
        this.repositorioMascotas = repositorioMascotas;
        this.repositorioTurnos = repositorioTurnos;
        this.gestorStock = gestorStock;
    }

    /**
     * Cierra la consulta: descuenta el stock de sus tratamientos y vacunas (GestorStock) y guarda la historia.
     * Después de confirmada, la consulta no admite cambios y ya se puede facturar.
     */
    public void confirmarConsulta(Consulta consulta, Veterinario veterinario) throws StockInsuficienteException {
        validarConsultaDelVeterinario(consulta, veterinario);
        gestorStock.confirmarConsumo(consulta);
        repositorioMascotas.guardar(mascotaDe(consulta));
    }

    /** Consultas atendidas por el veterinario (abiertas y confirmadas), de la más reciente a la más antigua. */
    public List<Consulta> consultasDelVeterinario(Veterinario veterinario) {
        if (veterinario == null) {
            throw new IllegalArgumentException("El veterinario no puede ser nulo.");
        }
        List<Consulta> resultado = new ArrayList<>();
        for (Mascota m : repositorioMascotas.listarTodas()) {
            for (Consulta c : m.getHistoriaClinica().getConsultas()) {
                if (c.getVeterinario() != null && c.getVeterinario().getDni().equals(veterinario.getDni())) {
                    resultado.add(c);
                }
            }
        }
        resultado.sort(Comparator.comparing(Consulta::getFechaHora).reversed());
        return resultado;
    }

    /** Consultas confirmadas que todavía no tienen una factura vigente. */
    public List<Consulta> consultasPendientesDeFacturar() {
        List<Consulta> resultado = new ArrayList<>();
        for (Mascota m : repositorioMascotas.listarTodas()) {
            for (Consulta c : m.getHistoriaClinica().getConsultas()) {
                if (c.isConfirmada() && (c.getFactura() == null || c.getFactura().isAnulada())) {
                    resultado.add(c);
                }
            }
        }
        resultado.sort(Comparator.comparing(Consulta::getFechaHora));
        return resultado;
    }

    /** Registra el diagnóstico y las observaciones de una consulta abierta. */
    public void registrarDiagnostico(Consulta consulta, Veterinario veterinario, String diagnostico, String observaciones) {
        validarConsultaDelVeterinario(consulta, veterinario);
        consulta.setDiagnostico(diagnostico == null ? "" : diagnostico.trim());
        consulta.setObservaciones(observaciones == null ? "" : observaciones.trim());
        repositorioMascotas.guardar(mascotaDe(consulta));
    }

    /** Turnos todavía no atendidos del veterinario, ordenados por fecha. */
    public List<Turno> turnosPendientes(Veterinario veterinario) {
        if (veterinario == null) {
            throw new IllegalArgumentException("El veterinario no puede ser nulo.");
        }
        List<Turno> pendientes = new ArrayList<>();
        for (Turno t : repositorioTurnos.buscarPorVeterinario(veterinario)) {
            if (t.getEstado() == EstadoTurno.PROGRAMADO) {
                pendientes.add(t);
            }
        }
        pendientes.sort(Comparator.comparing(Turno::getFechaHora));
        return pendientes;
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
            mascota.actualizarPeso(pesoDelDia);
        }

        turno.marcarAtendido();
        Duenio duenio = mascota.getDuenio();
        if (duenio != null) {
            duenio.reiniciarAusencias();
        }
        repositorioTurnos.guardar(turno);
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
                                          int cantidad, String dosis, String frecuencia, int duracionDias)
            throws PesoNoRegistradoException, StockInsuficienteException {
        validarConsultaDelVeterinario(consulta, veterinario);
        if (medicamento == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de unidades debe ser mayor a cero.");
        }
        // Primero el peso (lo valida Consulta y lanza PesoNoRegistradoException); después el stock.
        if (consulta.tienePesoRegistrado() && !medicamento.tieneStock(cantidad)) {
            throw new StockInsuficienteException("No hay stock suficiente de " + medicamento.getNombre()
                    + ": se piden " + cantidad + " y hay " + medicamento.getStockActual() + ".");
        }
        Tratamiento tratamiento = consulta.agregarTratamiento(medicamento, cantidad, dosis, frecuencia, duracionDias);
        repositorioMascotas.guardar(mascotaDe(consulta));
        return tratamiento;
    }

    public VacunaAplicada aplicarVacuna(Consulta consulta, Veterinario veterinario, Vacuna vacuna, Lote lote)
            throws VacunaNoAplicableException {
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
