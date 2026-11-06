package controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.TurnoNoDisponibleException;
import modelo.ConceptoFactura;
import modelo.Duenio;
import modelo.EstadoTurno;
import modelo.Factura;
import modelo.Mascota;
import modelo.MedioPago;
import modelo.TipoAtencion;
import modelo.Turno;
import modelo.Veterinario;
import notificacion.Notificador;
import repositorio.RepositorioPersonal;
import repositorio.RepositorioTurnos;

public class GestorTurnos {
    private static final int DIAS_DE_BUSQUEDA = 14;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Porcentaje del precio base que se cobra como seña a los dueños con ausencias reiteradas. */
    private static final double PORCENTAJE_SENIA = 0.50;

    private final RepositorioTurnos repositorioTurnos;
    private final RepositorioPersonal repositorioPersonal;
    private final Notificador notificador;
    private final GestorFacturacion gestorFacturacion;

    public GestorTurnos(RepositorioTurnos repositorioTurnos, RepositorioPersonal repositorioPersonal,
                        Notificador notificador, GestorFacturacion gestorFacturacion) {
        if (repositorioTurnos == null || repositorioPersonal == null || notificador == null || gestorFacturacion == null) {
            throw new IllegalArgumentException("Los repositorios, el notificador y el gestor de facturación son obligatorios.");
        }
        this.repositorioTurnos = repositorioTurnos;
        this.repositorioPersonal = repositorioPersonal;
        this.notificador = notificador;
        this.gestorFacturacion = gestorFacturacion;
    }

    public List<Turno> listarTurnos() {
        List<Turno> turnos = new ArrayList<>(repositorioTurnos.listarTodos());
        turnos.sort(Comparator.comparing(Turno::getFechaHora));
        return turnos;
    }

    public Turno asignarTurno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora, TipoAtencion tipo) throws TurnoNoDisponibleException {
        return asignarTurno(mascota, veterinario, fechaHora, tipo, LocalDateTime.now());
    }

    Turno asignarTurno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora, TipoAtencion tipo,
                       LocalDateTime ahora) throws TurnoNoDisponibleException {
        if (mascota == null || veterinario == null || fechaHora == null || tipo == null) {
            throw new IllegalArgumentException("La mascota, el veterinario, la fecha y el tipo de atención son obligatorios.");
        }
        validarNoEsPasado(fechaHora, ahora);
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
        validarMascotaLibre(nuevo, null);

        Duenio duenio = mascota.getDuenio();
        if (duenio != null && duenio.debePagarSenia()) {
            nuevo.setSeniaPagada(false);
        }
        repositorioTurnos.guardar(nuevo);
        return nuevo;
    }

    /** Registra el pago de la seña del turno y emite la factura correspondiente. */
    public Factura registrarSenia(Turno turno, MedioPago medioPago) {
        if (turno == null || medioPago == null) {
            throw new IllegalArgumentException("El turno y el medio de pago son obligatorios.");
        }
        if (turno.isSeniaPagada()) {
            throw new IllegalStateException("El turno no tiene una seña pendiente.");
        }
        Duenio duenio = duenioDe(turno);
        turno.marcarSeniaPagada();
        repositorioTurnos.guardar(turno);
        double monto = turno.getTipo().getPrecioBase() * PORCENTAJE_SENIA;
        return gestorFacturacion.emitirFacturaCargo(duenio, ConceptoFactura.SENIA, monto, medioPago);
    }

    public Turno asignarUrgencia(Mascota mascota, TipoAtencion tipo) throws TurnoNoDisponibleException {
        return asignarUrgencia(mascota, tipo, LocalDateTime.now());
    }

    Turno asignarUrgencia(Mascota mascota, TipoAtencion tipo, LocalDateTime ahora) throws TurnoNoDisponibleException {
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
            Duenio duenio = d.getMascota().getDuenio();
            if (duenio != null) {
                notificador.notificar(duenio, "Su turno del " + anterior.format(FORMATO) + " para " + d.getMascota().getNombre()
                        + " fue reprogramado al " + e.getValue().format(FORMATO) + " por una urgencia.");
            }
        }
        return urgente;
    }

    public void reprogramarTurno(Turno turno, LocalDateTime nuevaFechaHora) throws TurnoNoDisponibleException {
        reprogramarTurno(turno, nuevaFechaHora, LocalDateTime.now());
    }

    void reprogramarTurno(Turno turno, LocalDateTime nuevaFechaHora, LocalDateTime ahora) throws TurnoNoDisponibleException {
        if (turno == null || nuevaFechaHora == null) {
            throw new IllegalArgumentException("El turno y la nueva fecha son obligatorios.");
        }
        if (turno.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede reprogramar un turno programado.");
        }
        validarNoEsPasado(nuevaFechaHora, ahora);
        Veterinario vet = turno.getVeterinario();
        Turno hipotetico = new Turno(nuevaFechaHora, turno.getTipo(), turno.getMascota(), vet, turno.isEsUrgencia());
        if (!entraEnHorario(vet, nuevaFechaHora, hipotetico.getFechaHoraFin()) || haySuperposicion(hipotetico, vet, turno)) {
            // El turno queda como estaba.
            throw new TurnoNoDisponibleException("El horario " + nuevaFechaHora.format(FORMATO) + " no está disponible.");
        }
        validarMascotaLibre(hipotetico, turno);
        turno.reprogramar(nuevaFechaHora);
        repositorioTurnos.guardar(turno);
    }

    /**
     * Cancela el turno. Si se cancela tarde, factura el cargo por cancelación y devuelve esa factura;
     * si se cancela a tiempo no hay cargo y devuelve null.
     */
    public Factura cancelarTurno(Turno turno, MedioPago medioPago) {
        return cancelarTurno(turno, medioPago, LocalDateTime.now());
    }

    Factura cancelarTurno(Turno turno, MedioPago medioPago, LocalDateTime momento) {
        if (turno == null || medioPago == null) {
            throw new IllegalArgumentException("El turno y el medio de pago son obligatorios.");
        }
        Duenio duenio = duenioDe(turno); // se valida antes de cancelar para no dejar el turno a medias
        double cargo = turno.cancelar(momento);
        repositorioTurnos.guardar(turno);
        if (cargo <= 0) {
            return null;
        }
        return gestorFacturacion.emitirFacturaCargo(duenio, ConceptoFactura.CARGO_CANCELACION, cargo, medioPago);
    }

    private Duenio duenioDe(Turno turno) {
        Duenio duenio = turno.getMascota().getDuenio();
        if (duenio == null) {
            throw new IllegalStateException("La mascota del turno no tiene dueño a quien facturar.");
        }
        return duenio;
    }

    public void marcarAusente(Turno turno) {
        marcarAusente(turno, LocalDateTime.now());
    }

    void marcarAusente(Turno turno, LocalDateTime ahora) {
        if (turno == null || ahora == null) {
            throw new IllegalArgumentException("El turno y el momento son obligatorios.");
        }
        if (ahora.isBefore(turno.getFechaHora())) {
            throw new IllegalStateException("Todavía no llegó la hora del turno: se puede marcar ausente desde el "
                    + turno.getFechaHora().format(FORMATO) + ".");
        }
        turno.marcarAusente();
        Duenio duenio = turno.getMascota().getDuenio();
        if (duenio != null) {
            duenio.registrarAusencia();
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

    /** El turno entero tiene que caer dentro de UN mismo horario de atención (no puede cruzar un corte). */
    private boolean entraEnHorario(Veterinario vet, LocalDateTime inicio, LocalDateTime fin) {
        return vet.atiendeEntre(inicio, fin);
    }

    private void validarNoEsPasado(LocalDateTime fechaHora, LocalDateTime ahora) throws TurnoNoDisponibleException {
        if (fechaHora.isBefore(ahora)) {
            throw new TurnoNoDisponibleException("No se pueden dar turnos en el pasado (" + fechaHora.format(FORMATO) + ").");
        }
    }

    /** Una mascota no puede tener dos turnos programados que se superpongan, aunque sean con distintos veterinarios. */
    private void validarMascotaLibre(Turno candidato, Turno ignorar) throws TurnoNoDisponibleException {
        for (Turno existente : repositorioTurnos.listarTodos()) {
            if (existente != ignorar && existente.getMascota() == candidato.getMascota()
                    && existente.getEstado() == EstadoTurno.PROGRAMADO && candidato.seSuperponeCon(existente)) {
                throw new TurnoNoDisponibleException(candidato.getMascota().getNombre() + " ya tiene un turno el "
                        + existente.getFechaHora().format(FORMATO) + " con " + existente.getVeterinario().getNombreCompleto() + ".");
            }
        }
    }

    private Veterinario buscarVeterinarioDeGuardia() {
        for (Veterinario v : repositorioPersonal.listarVeterinarios()) {
            if (v.isDeGuardia()) {
                return v;
            }
        }
        throw new IllegalStateException("No hay un veterinario de guardia registrado.");
    }

    private LocalDateTime primerHuecoLibre(Veterinario vet, LocalDateTime desde, TipoAtencion tipo, List<Turno> ocupados) throws TurnoNoDisponibleException {
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
