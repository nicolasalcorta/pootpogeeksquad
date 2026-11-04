package controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.TurnoNoDisponibleException;
import modelo.Dueño;
import modelo.EstadoTurno;
import modelo.Mascota;
import modelo.TipoAtencion;
import modelo.Turno;
import modelo.Veterinario;
import notificacion.Notificador;
import repositorio.RepositorioTurnos;

public class GestorTurnos {
    private static final int DIAS_DE_BUSQUEDA = 14;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RepositorioTurnos repositorioTurnos;
    private final Notificador notificador;
    private final List<Veterinario> veterinarios;

    public GestorTurnos(RepositorioTurnos repositorio, Notificador notificador) {
        if (repositorio == null || notificador == null) {
            throw new IllegalArgumentException("El repositorio y el notificador son obligatorios.");
        }
        this.repositorioTurnos = repositorio;
        this.notificador = notificador;
        this.veterinarios = new ArrayList<>();
    }

    public void registrarVeterinario(Veterinario veterinario) {
        if (veterinario == null) {
            throw new IllegalArgumentException("El veterinario no puede ser nulo.");
        }
        if (!veterinarios.contains(veterinario)) {
            veterinarios.add(veterinario);
        }
    }

    public List<Veterinario> getVeterinarios() {
        return Collections.unmodifiableList(veterinarios);
    }

    public Turno asignarTurno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora, TipoAtencion tipo) {
        if (mascota == null || veterinario == null || fechaHora == null || tipo == null) {
            throw new IllegalArgumentException("La mascota, el veterinario, la fecha y el tipo de atención son obligatorios.");
        }
        LocalDateTime fin = fechaHora.plusMinutes(tipo.getDuracionMinutos());
        if (!entraEnHorario(veterinario, fechaHora, fin)) {
            throw new TurnoNoDisponibleException("El veterinario " + veterinario.getNombreCompleto()
                    + " no atiende en ese horario (" + fechaHora.format(FORMATO) + ").");
        }

        Turno nuevo = new Turno(fechaHora, tipo, mascota, veterinario, false);
        if (haySuperposicion(nuevo, veterinario, null)) {
            throw new TurnoNoDisponibleException("El horario " + fechaHora.format(FORMATO)
                    + " se superpone con otro turno de " + veterinario.getNombreCompleto() + ".");
        }

        Dueño dueño = mascota.getDueño();
        if (dueño != null && dueño.debePagarSenia()) {
            nuevo.setSeniaPagada(false);
        }
        repositorioTurnos.guardar(nuevo);
        return nuevo;
    }

    public void registrarPagoSenia(Turno turno) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo.");
        }
        if (turno.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede registrar la seña de un turno programado.");
        }
        turno.setSeniaPagada(true);
        repositorioTurnos.guardar(turno);
    }

    public Turno asignarUrgencia(Mascota mascota, TipoAtencion tipo) {
        return asignarUrgencia(mascota, tipo, LocalDateTime.now());
    }

    Turno asignarUrgencia(Mascota mascota, TipoAtencion tipo, LocalDateTime ahora) {
        if (mascota == null || tipo == null || ahora == null) {
            throw new IllegalArgumentException("La mascota, el tipo de atención y el momento son obligatorios.");
        }
        Veterinario guardia = buscarVeterinarioDeGuardia();

        List<Turno> programados = new ArrayList<>();
        for (Turno t : repositorioTurnos.buscarPorVeterinario(guardia)) {
            if (t.getEstado() == EstadoTurno.PROGRAMADO) {
                programados.add(t);
            }
        }
        List<Turno> urgentes = new ArrayList<>();
        for (Turno t : programados) {
            if (t.isEsUrgencia()) {
                urgentes.add(t);
            }
        }

        LocalDateTime inicio = primerHuecoLibre(guardia, ahora, tipo, urgentes);
        Turno urgente = new Turno(inicio, tipo, mascota, guardia, true);

        List<Turno> desplazados = new ArrayList<>();
        for (Turno t : programados) {
            if (!t.isEsUrgencia() && urgente.seSuperponeCon(t)) {
                desplazados.add(t);
            }
        }
        desplazados.sort(Comparator.comparing(Turno::getFechaHora));

        List<Turno> ocupados = new ArrayList<>(programados);
        ocupados.removeAll(desplazados);
        ocupados.add(urgente);
        Map<Turno, LocalDateTime> nuevasFechas = new LinkedHashMap<>();
        for (Turno d : desplazados) {
            LocalDateTime nueva = primerHuecoLibre(guardia, urgente.getFechaHoraFin(), d.getTipo(), ocupados);
            nuevasFechas.put(d, nueva);
            ocupados.add(new Turno(nueva, d.getTipo(), d.getMascota(), guardia, d.isEsUrgencia()));
        }

        repositorioTurnos.guardar(urgente);
        for (Map.Entry<Turno, LocalDateTime> e : nuevasFechas.entrySet()) {
            Turno d = e.getKey();
            LocalDateTime anterior = d.getFechaHora();
            d.reprogramar(e.getValue());
            repositorioTurnos.guardar(d);
            Dueño dueño = d.getMascota().getDueño();
            if (dueño != null) {
                notificador.notificar(dueño, "Su turno del " + anterior.format(FORMATO) + " para " + d.getMascota().getNombre()
                        + " fue reprogramado al " + e.getValue().format(FORMATO) + " por una urgencia.");
            }
        }
        return urgente;
    }

    public void reprogramarTurno(Turno turno, LocalDateTime nuevaFechaHora) {
        if (turno == null || nuevaFechaHora == null) {
            throw new IllegalArgumentException("El turno y la nueva fecha son obligatorios.");
        }
        if (turno.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede reprogramar un turno programado.");
        }
        Veterinario vet = turno.getVeterinario();
        Turno hipotetico = new Turno(nuevaFechaHora, turno.getTipo(), turno.getMascota(), vet, turno.isEsUrgencia());
        if (!entraEnHorario(vet, nuevaFechaHora, hipotetico.getFechaHoraFin()) || haySuperposicion(hipotetico, vet, turno)) {
            // El turno queda como estaba.
            throw new TurnoNoDisponibleException("El horario " + nuevaFechaHora.format(FORMATO) + " no está disponible.");
        }
        turno.reprogramar(nuevaFechaHora);
        repositorioTurnos.guardar(turno);
    }

    public double cancelarTurno(Turno turno) {
        return cancelarTurno(turno, LocalDateTime.now());
    }

    double cancelarTurno(Turno turno, LocalDateTime momento) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo.");
        }
        double cargo = turno.cancelar(momento);
        repositorioTurnos.guardar(turno);
        return cargo;
    }

    public void marcarAusente(Turno turno) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo.");
        }
        turno.marcarAusente();
        Dueño dueño = turno.getMascota().getDueño();
        if (dueño != null) {
            dueño.registrarAusencia();
        }
        repositorioTurnos.guardar(turno);
    }

    public List<Turno> consultarTurnosPorVeterinario(Veterinario veterinario) {
        if (veterinario == null) {
            throw new IllegalArgumentException("El veterinario no puede ser nulo.");
        }
        List<Turno> turnos = new ArrayList<>(repositorioTurnos.buscarPorVeterinario(veterinario));
        turnos.sort(Comparator.comparing(Turno::getFechaHora));
        return turnos;
    }

        private boolean haySuperposicion(Turno candidato, Veterinario veterinario, Turno ignorar) {
        for (Turno existente : repositorioTurnos.buscarPorVeterinario(veterinario)) {
            if (existente != ignorar && candidato.seSuperponeCon(existente)) {
                return true;
            }
        }
        return false;
    }

    private boolean entraEnHorario(Veterinario vet, LocalDateTime inicio, LocalDateTime fin) {
        return vet.atiendeEnHorario(inicio) && vet.atiendeEnHorario(fin.minusMinutes(1));
    }

    private Veterinario buscarVeterinarioDeGuardia() {
        for (Veterinario v : veterinarios) {
            if (v.isDeGuardia()) {
                return v;
            }
        }
        throw new IllegalStateException("No hay un veterinario de guardia registrado.");
    }

    private LocalDateTime primerHuecoLibre(Veterinario vet, LocalDateTime desde, TipoAtencion tipo, List<Turno> ocupados) {
        LocalDateTime candidato = desde.truncatedTo(ChronoUnit.MINUTES);
        if (candidato.isBefore(desde)) {
            candidato = candidato.plusMinutes(1);
        }
        LocalDateTime limite = candidato.plusDays(DIAS_DE_BUSQUEDA);
        while (candidato.isBefore(limite)) {
            LocalDateTime fin = candidato.plusMinutes(tipo.getDuracionMinutos());
            if (!entraEnHorario(vet, candidato, fin)) {
                candidato = candidato.plusMinutes(1);
                continue;
            }
            Turno choque = primerChoque(candidato, fin, ocupados);
            if (choque == null) {
                return candidato;
            }
            candidato = choque.getFechaHoraFin();
        }
        throw new TurnoNoDisponibleException("No hay horarios disponibles para " + vet.getNombreCompleto()
                + " en los próximos " + DIAS_DE_BUSQUEDA + " días.");
    }

    private Turno primerChoque(LocalDateTime inicio, LocalDateTime fin, List<Turno> ocupados) {
        for (Turno t : ocupados) {
            if (t.getEstado() != EstadoTurno.CANCELADO && inicio.isBefore(t.getFechaHoraFin()) && fin.isAfter(t.getFechaHora())) {
                return t;
            }
        }
        return null;
    }
}
