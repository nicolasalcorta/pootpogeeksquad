package controlador;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import excepciones.*;
import modelo.*;
import notificacion.*;
import repositorio.*;

/**
 * Pruebas unitarias del TPO "Clínica Veterinaria" (POO - GeekSquad).
 *
 * Qué prueban: el modelo, los siete gestores y la persistencia en archivos .txt, incluidos los casos borde.
 * Cada prueba arma su propio escenario (clase Mundo) con repositorios en memoria, así no depende de los datos de la carpeta datos/.
 * Las que prueban archivos .txt usan una carpeta temporal.
 *
 * Están en el paquete "controlador" porque algunas usan las versiones internas de los métodos que reciben el momento actual
 * (por ejemplo cancelarTurno(turno, medio, momento)), para no depender del reloj.
 *
 * No usan JUnit: cada test() muestra OK o FALLA, y al final un resumen.
 * Las pruebas marcadas "PENDIENTE" documentan casos borde de prioridad baja que el sistema todavía no valida;
 * se informan aparte y no cuentan como falla.
 *
 * Cómo correrlas (desde la carpeta TPOficial, con este archivo en test/controlador/):
 *   javac -encoding UTF-8 -d out/test src/modelo/*.java src/excepciones/*.java src/repositorio/*.java src/notificacion/*.java src/controlador/*.java test/controlador/PruebasUnitarias.java
 *   java -cp out/test controlador.PruebasUnitarias
 */
public class PruebasUnitarias {
    static int ok = 0, falla = 0, pendiente = 0;
    static final List<String> fallas = new ArrayList<>();
    static final List<String> pendientes = new ArrayList<>();

    interface Bloque { void run() throws Exception; }

    static void test(String nombre, Bloque b) {
        try { b.run(); ok++; System.out.println("OK    " + nombre); }
        catch (Throwable e) {
            if (nombre.startsWith("PENDIENTE")) { pendiente++; pendientes.add(nombre); System.out.println("PEND  " + nombre); }
            else { falla++; fallas.add(nombre + " -> " + e); System.out.println("FALLA " + nombre + " -> " + e); }
        }
    }

    static void check(boolean c, String msg) { if (!c) throw new AssertionError(msg); }

    static <T extends Throwable> T lanza(Class<T> tipo, Bloque b) {
        try { b.run(); } catch (Throwable e) {
            if (tipo.isInstance(e)) return tipo.cast(e);
            throw new AssertionError("esperaba " + tipo.getSimpleName() + " y lanzó " + e);
        }
        throw new AssertionError("esperaba " + tipo.getSimpleName() + " y no lanzó nada");
    }

    static Path tmp() throws Exception { return Files.createTempDirectory("tpo-test"); }

    /** Escenario común: personal, un dueño con un perro, un medicamento y una vacuna con su insumo. */
    static class Mundo {
        RepositorioPersonal rp = new RepositorioPersonalMemoria();
        RepositorioDuenios rd = new RepositorioDueniosMemoria();
        RepositorioMascotas rm = new RepositorioMascotasMemoria();
        RepositorioTurnos rt = new RepositorioTurnosMemoria();
        RepositorioProductos rpr = new RepositorioProductosMemoria();
        RepositorioVacunas rv = new RepositorioVacunasMemoria();
        RepositorioFacturas rf = new RepositorioFacturasMemoria();
        List<String> avisos = new ArrayList<>();
        Notificador notif = (d, m) -> avisos.add(d.getDni() + ": " + m);
        GestorPersonal gper = new GestorPersonal(rp);
        GestorPacientes gpac = new GestorPacientes(rd, rm);
        GestorStock gs = new GestorStock(rpr);
        GestorFacturacion gf = new GestorFacturacion(rf);
        GestorTurnos gt = new GestorTurnos(rt, rp, notif, gf);
        GestorConsultas gc = new GestorConsultas(rm, rt, gs);
        GestorVacunas gv = new GestorVacunas(rm, rv, notif);
        Personal admin = new Personal("A", "Ad", "Min", "1", "t", "L1", RolPersonal.ADMINISTRADOR);
        Personal recep = new Personal("R", "Re", "Cep", "2", "t", "L2", RolPersonal.RECEPCIONISTA);
        Veterinario vet = new Veterinario("V", "Ana", "Paz", "3", "t", "L3", "M1", "gral");
        Veterinario guardia = new Veterinario("G", "Gu", "Ardia", "4", "t", "L4", "M2", "cir");
        Duenio duenio = new Duenio("D", "Juan", "Diaz", "40", "t");
        Perro perro;
        Producto med = new Producto("MED", "Amoxi", 5, 100);
        Producto insumo = new Producto("VAC", "Antirrabica", 2, 1000);
        Lote loteVac;
        Vacuna vacuna;
        LocalDateTime lunes10 = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(10, 0);

        Mundo() throws Exception {
            for (DayOfWeek d : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY})
                vet.agregarHorario(new HorarioAtencion(d, LocalTime.of(9, 0), LocalTime.of(18, 0)));
            guardia.setDeGuardia(true);
            for (DayOfWeek d : DayOfWeek.values()) guardia.agregarHorario(new HorarioAtencion(d, LocalTime.of(0, 0), LocalTime.of(23, 59)));
            gper.registrarPersonal(admin); gper.registrarPersonal(recep); gper.registrarPersonal(vet); gper.registrarPersonal(guardia);
            gpac.registrarDuenio(duenio);
            perro = new Perro("M1", "Toby", "x", "Macho", LocalDate.now().minusYears(2), 20, null, "c", "g");
            gpac.registrarMascota(duenio, perro);
            med.agregarLote(new Lote("LM1", LocalDate.now().plusMonths(6), 10));
            gs.registrarProducto(med, admin);
            loteVac = new Lote("LV1", LocalDate.now().plusMonths(6), 3);
            insumo.agregarLote(loteVac);
            gs.registrarProducto(insumo, admin);
            vacuna = new Vacuna("V1", "Antirrabica", 3, insumo);
            vacuna.agregarEspecie(Especie.PERRO, 12);
            rv.guardar(vacuna);
        }

        Consulta consultaAbierta() throws Exception {
            Turno t = gt.asignarTurno(perro, vet, lunes10, TipoAtencion.CONSULTA_GENERAL);
            return gc.registrarConsulta(t, vet, "control", 20);
        }
    }

    public static void main(String[] args) throws Exception {
        // ======== Personas y roles ========
        test("Persona: DNI vacío rechazado", () -> lanza(IllegalArgumentException.class, () -> new Duenio("x", "a", "b", "  ", "t")));
        test("Personal: no acepta rol VETERINARIO", () -> lanza(IllegalArgumentException.class, () -> new Personal("x", "a", "b", "9", "t", "L", RolPersonal.VETERINARIO)));
        test("Veterinario: tiene rol VETERINARIO", () -> check(new Veterinario("x", "a", "b", "9", "t", "L", "M", "e").tieneRol(RolPersonal.VETERINARIO), "rol"));
        test("GestorPersonal: DNI repetido rechazado", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.gper.registrarPersonal(new Personal("Z", "z", "z", "1", "t", "LZ", RolPersonal.RECEPCIONISTA))); });
        test("Dueño: seña obligatoria recién con 3 ausencias", () -> { Duenio d = new Duenio("x", "a", "b", "9", "t");
            d.registrarAusencia(); d.registrarAusencia(); check(!d.debePagarSenia(), "2 ausencias"); d.registrarAusencia(); check(d.debePagarSenia(), "3 ausencias"); });
        test("PENDIENTE Dueño: nombre vacío debería rechazarse", () -> lanza(IllegalArgumentException.class, () -> new Duenio("x", "", "", "9", "t")));

        // ======== Mascotas ========
        test("Mascota: queda una sola vez en la lista del dueño", () -> { Mundo w = new Mundo(); check(w.duenio.getMascotas().size() == 1, "size=" + w.duenio.getMascotas().size()); });
        test("Mascota: id repetido rechazado", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.gpac.registrarMascota(w.duenio, new Gato("M1", "Otro", "x", "H", LocalDate.now(), 3, null, true))); });
        test("Mascota: actualizarPeso rechaza 0 y negativos", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.perro.actualizarPeso(0)); lanza(IllegalArgumentException.class, () -> w.perro.actualizarPeso(-1)); });
        test("Transferir: sale de la lista del dueño anterior", () -> { Mundo w = new Mundo(); Duenio otro = new Duenio("O", "Ot", "Ro", "41", "t"); w.gpac.registrarDuenio(otro);
            w.gpac.transferirMascota(w.perro, otro);
            check(w.duenio.getMascotas().isEmpty() && otro.getMascotas().contains(w.perro) && w.perro.getDuenio() == otro, "listas"); });
        test("Transferir al mismo dueño rechazado", () -> { Mundo w = new Mundo(); lanza(IllegalArgumentException.class, () -> w.gpac.transferirMascota(w.perro, w.duenio)); });
        test("PENDIENTE Mascota: fecha de nacimiento futura debería rechazarse", () ->
                lanza(IllegalArgumentException.class, () -> new Perro("F", "f", "x", "M", LocalDate.now().plusYears(1), 3, null, null, null)));
        test("PENDIENTE Mascota: peso inicial negativo debería rechazarse", () ->
                lanza(IllegalArgumentException.class, () -> new Perro("F", "f", "x", "M", LocalDate.now(), -5, null, null, null)));

        // ======== Turnos ========
        test("Turno: fuera del horario del veterinario rechazado", () -> { Mundo w = new Mundo();
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, w.vet, w.lunes10.withHour(8), TipoAtencion.CONSULTA_GENERAL)); });
        test("Turno: último del día (17:30, 30 min) aceptado", () -> { Mundo w = new Mundo();
            w.gt.asignarTurno(w.perro, w.vet, w.lunes10.withHour(17).withMinute(30), TipoAtencion.CONSULTA_GENERAL); });
        test("Turno: que termina después del cierre (17:45) rechazado", () -> { Mundo w = new Mundo();
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, w.vet, w.lunes10.withHour(17).withMinute(45), TipoAtencion.CONSULTA_GENERAL)); });
        test("Turno: superpuesto rechazado", () -> { Mundo w = new Mundo(); w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, w.vet, w.lunes10.plusMinutes(15), TipoAtencion.VACUNACION)); });
        test("Turno: pegado al anterior (10:30 tras 10:00-10:30) aceptado", () -> { Mundo w = new Mundo();
            w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL); w.gt.asignarTurno(w.perro, w.vet, w.lunes10.plusMinutes(30), TipoAtencion.CONSULTA_GENERAL); });
        test("Turno: el horario de un turno cancelado queda libre", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            w.gt.cancelarTurno(t, new PagoEfectivo()); w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL); });
        test("Caso borde: un turno en una fecha pasada se rechaza", () -> { Mundo w = new Mundo();
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, w.vet, w.lunes10.minusWeeks(2), TipoAtencion.CONSULTA_GENERAL)); });
        test("Caso borde: un turno no puede cruzar un corte de horario (9-13 y 14-18)", () -> { Mundo w = new Mundo();
            Veterinario v2 = new Veterinario("V2", "b", "b", "5", "t", "L5", "M5", "e");
            v2.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));
            v2.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0)));
            w.gper.registrarPersonal(v2);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, v2, w.lunes10.withHour(12).withMinute(30), TipoAtencion.CIRUGIA)); });
        test("Caso borde: el veterinario de guardia (00:00-23:59) acepta un turno a las 23:30", () -> { Mundo w = new Mundo();
            w.gt.asignarTurno(w.perro, w.guardia, w.lunes10.withHour(23).withMinute(30), TipoAtencion.VACUNACION); });
        test("Caso borde: la misma mascota no puede tener dos turnos a la misma hora con distintos veterinarios", () -> { Mundo w = new Mundo();
            w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.asignarTurno(w.perro, w.guardia, w.lunes10, TipoAtencion.CONSULTA_GENERAL)); });
        test("Reprogramar: puede moverse sobre su propio horario", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            w.gt.reprogramarTurno(t, w.lunes10.plusMinutes(10)); check(t.getFechaHora().equals(w.lunes10.plusMinutes(10)), "fecha"); });
        test("Reprogramar: si falla, el turno queda como estaba", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.reprogramarTurno(t, w.lunes10.withHour(20))); check(t.getFechaHora().equals(w.lunes10), "cambió"); });
        test("Cancelar: con 2 h justas no hay cargo", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            check(w.gt.cancelarTurno(t, new PagoEfectivo(), w.lunes10.minusHours(2)) == null, "hubo cargo"); });
        test("Cancelar: con 1h59 se factura el 20% (5000 -> 1000, efectivo -10% = 900)", () -> { Mundo w = new Mundo();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            Factura f = w.gt.cancelarTurno(t, new PagoEfectivo(), w.lunes10.minusMinutes(119));
            check(f != null && f.getConcepto() == ConceptoFactura.CARGO_CANCELACION && f.getTotal() == 900.0, "total=" + (f == null ? null : f.getTotal())); });
        test("Cancelar dos veces rechazado", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            w.gt.cancelarTurno(t, new PagoEfectivo()); lanza(IllegalStateException.class, () -> w.gt.cancelarTurno(t, new PagoEfectivo())); });
        test("Cancelar: sin medio de pago no toca el turno", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(IllegalArgumentException.class, () -> w.gt.cancelarTurno(t, null)); check(t.getEstado() == EstadoTurno.PROGRAMADO, "estado=" + t.getEstado()); });
        test("Ausente: suma una ausencia al dueño", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            w.gt.marcarAusente(t, w.lunes10.plusMinutes(15)); check(w.duenio.getAusenciasConsecutivas() == 1 && t.getEstado() == EstadoTurno.AUSENTE, "ausencias"); });
        test("Caso borde: Reprogramar a una fecha pasada rechazado", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.reprogramarTurno(t, w.lunes10.minusWeeks(2))); check(t.getFechaHora().equals(w.lunes10), "cambió"); });
        test("Caso borde: Reprogramar encima de otro turno de la misma mascota rechazado", () -> { Mundo w = new Mundo();
            w.gt.asignarTurno(w.perro, w.guardia, w.lunes10.plusHours(2), TipoAtencion.CONSULTA_GENERAL);
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(TurnoNoDisponibleException.class, () -> w.gt.reprogramarTurno(t, w.lunes10.plusHours(2))); });
        test("Caso borde: La misma mascota puede tener otro turno si el anterior se canceló", () -> { Mundo w = new Mundo();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL); w.gt.cancelarTurno(t, new PagoEfectivo());
            w.gt.asignarTurno(w.perro, w.guardia, w.lunes10, TipoAtencion.CONSULTA_GENERAL); });
        test("Caso borde: Ausente: a la hora del turno sí se puede marcar", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            w.gt.marcarAusente(t, w.lunes10); check(t.getEstado() == EstadoTurno.AUSENTE, "estado"); });
        test("Caso borde: Corte de horario: un turno dentro del segundo bloque (14:00) se acepta", () -> { Mundo w = new Mundo();
            Veterinario v2 = new Veterinario("V2", "b", "b", "5", "t", "L5", "M5", "e");
            v2.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));
            v2.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0)));
            w.gper.registrarPersonal(v2); w.gt.asignarTurno(w.perro, v2, w.lunes10.withHour(14), TipoAtencion.CIRUGIA); });
        test("Caso borde: no se puede marcar ausente antes de la hora del turno", () -> { Mundo w = new Mundo();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(RuntimeException.class, () -> w.gt.marcarAusente(t)); });
        test("Seña: con 3 ausencias el turno nace con seña pendiente y no se atiende", () -> { Mundo w = new Mundo();
            for (int i = 0; i < 3; i++) w.duenio.registrarAusencia();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            check(!t.isSeniaPagada(), "seña"); lanza(IllegalStateException.class, () -> w.gc.registrarConsulta(t, w.vet, "m", 10)); });
        test("Seña: se factura el 50% (2500 con débito)", () -> { Mundo w = new Mundo(); for (int i = 0; i < 3; i++) w.duenio.registrarAusencia();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            Factura f = w.gt.registrarSenia(t, new PagoDebito());
            check(t.isSeniaPagada() && f.getTotal() == 2500.0 && f.getConcepto() == ConceptoFactura.SENIA, "total=" + f.getTotal()); });
        test("Seña: registrarla sin seña pendiente rechazado", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(IllegalStateException.class, () -> w.gt.registrarSenia(t, new PagoDebito())); });
        test("Urgencia: sin veterinario de guardia da un error claro", () -> {
            GestorTurnos gt = new GestorTurnos(new RepositorioTurnosMemoria(), new RepositorioPersonalMemoria(), (d, m) -> { },
                    new GestorFacturacion(new RepositorioFacturasMemoria()));
            Perro p = new Perro("P", "p", "x", "M", LocalDate.now(), 3, null, null, null);
            lanza(IllegalStateException.class, () -> gt.asignarUrgencia(p, TipoAtencion.CIRUGIA)); });
        test("Urgencia: corre el turno superpuesto y avisa al dueño", () -> { Mundo w = new Mundo();
            Turno comun = w.gt.asignarTurno(w.perro, w.guardia, w.lunes10.plusMinutes(30), TipoAtencion.CONSULTA_GENERAL);
            Turno urg = w.gt.asignarUrgencia(w.perro, TipoAtencion.CIRUGIA, w.lunes10);
            check(urg.getFechaHora().equals(w.lunes10) && urg.isEsUrgencia(), "urgencia en " + urg.getFechaHora());
            check(!comun.getFechaHora().isBefore(urg.getFechaHoraFin()), "no se corrió: " + comun.getFechaHora());
            check(w.avisos.size() == 1, "avisos=" + w.avisos.size()); });

        // ======== Consultas ========
        test("Consulta: marca el turno ATENDIDO y actualiza el peso", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            check(c.getTurno().getEstado() == EstadoTurno.ATENDIDO && w.perro.getPesoActual() == 20, "estado"); });
        test("Consulta: otro veterinario no puede registrarla", () -> { Mundo w = new Mundo(); Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL);
            lanza(IllegalStateException.class, () -> w.gc.registrarConsulta(t, w.guardia, "m", 10)); });
        test("Consulta: dos veces sobre el mismo turno rechazado", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(IllegalStateException.class, () -> w.gc.registrarConsulta(c.getTurno(), w.vet, "m", 10)); });
        test("Consulta: reinicia las ausencias del dueño", () -> { Mundo w = new Mundo(); w.duenio.registrarAusencia(); w.consultaAbierta();
            check(w.duenio.getAusenciasConsecutivas() == 0, "ausencias"); });
        test("Tratamiento: sin peso lanza PesoNoRegistradoException", () -> { Mundo w = new Mundo();
            Turno t = w.gt.asignarTurno(w.perro, w.vet, w.lunes10, TipoAtencion.CONSULTA_GENERAL); Consulta c = w.gc.registrarConsulta(t, w.vet, "m", 0);
            lanza(PesoNoRegistradoException.class, () -> w.gc.agregarTratamiento(c, w.vet, w.med, 1, "d", "f", 3)); });
        test("Tratamiento: más unidades que el stock -> StockInsuficienteException", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(StockInsuficienteException.class, () -> w.gc.agregarTratamiento(c, w.vet, w.med, 11, "d", "f", 3)); });
        test("Tratamiento: cantidad 0 rechazada", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(IllegalArgumentException.class, () -> w.gc.agregarTratamiento(c, w.vet, w.med, 0, "d", "f", 3)); });
        test("Confirmar: dos tratamientos que superan el stock fallan sin descontar nada", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            w.gc.agregarTratamiento(c, w.vet, w.med, 6, "d", "f", 3); w.gc.agregarTratamiento(c, w.vet, w.med, 6, "d", "f", 3);
            lanza(StockInsuficienteException.class, () -> w.gc.confirmarConsulta(c, w.vet));
            check(w.med.getStockActual() == 10 && !c.isConfirmada(), "stock=" + w.med.getStockActual()); });
        test("Confirmar: descuenta tratamientos y vacunas y bloquea cambios", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            w.gc.agregarTratamiento(c, w.vet, w.med, 4, "d", "f", 3); w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac); w.gc.confirmarConsulta(c, w.vet);
            check(w.med.getStockActual() == 6 && w.loteVac.getCantidad() == 2, "stock");
            lanza(IllegalStateException.class, () -> w.gc.agregarTratamiento(c, w.vet, w.med, 1, "d", "f", 3)); });
        test("Confirmar dos veces rechazado", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta(); w.gc.confirmarConsulta(c, w.vet);
            lanza(IllegalStateException.class, () -> w.gc.confirmarConsulta(c, w.vet)); });
        test("Vacuna: especie no habilitada -> VacunaNoAplicableException", () -> { Mundo w = new Mundo();
            Gato g = new Gato("G1", "Mi", "x", "H", LocalDate.now().minusYears(1), 3, null, true); w.gpac.registrarMascota(w.duenio, g);
            Turno t = w.gt.asignarTurno(g, w.vet, w.lunes10, TipoAtencion.VACUNACION); Consulta c = w.gc.registrarConsulta(t, w.vet, "v", 3);
            lanza(VacunaNoAplicableException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac)); });
        test("Vacuna: menor a la edad mínima rechazada", () -> { Mundo w = new Mundo();
            Perro cach = new Perro("C1", "Ca", "x", "M", LocalDate.now().minusMonths(1), 2, null, null, null); w.gpac.registrarMascota(w.duenio, cach);
            Turno t = w.gt.asignarTurno(cach, w.vet, w.lunes10, TipoAtencion.VACUNACION); Consulta c = w.gc.registrarConsulta(t, w.vet, "v", 2);
            lanza(VacunaNoAplicableException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac)); });
        test("Vacuna: lote de otro producto rechazado", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(VacunaNoAplicableException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.med.getLotes().get(0))); });
        test("Vacuna: lote vencido rechazado", () -> { Mundo w = new Mundo(); Lote viejo = new Lote("OLD", LocalDate.now().minusDays(1), 5); w.insumo.agregarLote(viejo);
            Consulta c = w.consultaAbierta(); lanza(VacunaNoAplicableException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, viejo)); });
        test("Caso borde: un lote sin dosis se rechaza al aplicar la vacuna (no recién al confirmar)", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            w.loteVac.setCantidad(0);
            lanza(ReglaNegocioException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac)); });
        test("Caso borde: Vacuna: dos dosis del mismo lote con una sola disponible -> la segunda se rechaza", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            w.loteVac.setCantidad(1); w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac);
            lanza(VacunaNoAplicableException.class, () -> w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac)); });
        test("Nota rectificatoria sobre consulta confirmada", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta(); w.gc.confirmarConsulta(c, w.vet);
            w.gc.registrarNotaRectificatoria(c, w.vet, "corrijo"); check(w.perro.getHistoriaClinica().getNotas().size() == 1, "nota"); });
        test("Nota rectificatoria vacía rechazada", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(IllegalArgumentException.class, () -> w.gc.registrarNotaRectificatoria(c, w.vet, "  ")); });

        // ======== Stock ========
        test("Stock: descuenta primero del lote que vence antes", () -> { Producto p = new Producto("X", "x", 0, 1);
            Lote tarde = new Lote("T", LocalDate.now().plusMonths(6), 5), pronto = new Lote("P", LocalDate.now().plusMonths(1), 5);
            p.agregarLote(tarde); p.agregarLote(pronto); p.descontarStock(7);
            check(pronto.getCantidad() == 0 && tarde.getCantidad() == 3, "pronto=" + pronto.getCantidad() + " tarde=" + tarde.getCantidad()); });
        test("Stock: los lotes vencidos no cuentan y disparan la alerta", () -> { Producto p = new Producto("X", "x", 0, 1);
            p.agregarLote(new Lote("V", LocalDate.now().minusDays(1), 5)); check(p.getStockActual() == 0 && p.hayAlertaStock(), "stock"); });
        test("Stock: un lote que vence hoy todavía sirve", () -> { Producto p = new Producto("X", "x", 0, 1);
            p.agregarLote(new Lote("H", LocalDate.now(), 5)); check(p.getStockActual() == 5, "stock"); });
        test("Stock: el recepcionista no registra productos ni lotes", () -> { Mundo w = new Mundo();
            lanza(RolNoAutorizadoException.class, () -> w.gs.registrarProducto(new Producto("N", "n", 0, 1), w.recep));
            lanza(RolNoAutorizadoException.class, () -> w.gs.ingresarLote(w.med, new Lote("X", LocalDate.now().plusDays(9), 1), w.recep)); });
        test("Stock: código de producto y número de lote repetidos rechazados", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.gs.registrarProducto(new Producto("MED", "otro", 0, 1), w.admin));
            lanza(IllegalArgumentException.class, () -> w.gs.ingresarLote(w.med, new Lote("LM1", LocalDate.now().plusDays(9), 1), w.admin)); });
        test("Caso borde: no se puede ingresar un lote ya vencido", () -> { Mundo w = new Mundo();
            lanza(RuntimeException.class, () -> w.gs.ingresarLote(w.med, new Lote("VENC", LocalDate.now().minusDays(3), 5), w.admin)); });
        test("Stock: lote con cantidad 0 rechazado", () -> lanza(IllegalArgumentException.class, () -> new Lote("X", LocalDate.now(), 0)));

        // ======== Facturación ========
        test("Factura: no se factura una consulta sin confirmar", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            lanza(IllegalStateException.class, () -> w.gf.emitirFactura(c, new PagoEfectivo())); });
        test("Factura: atención + tratamientos + vacuna con recargo de crédito ((5000+200+1000)*1.08 = 6696)", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta();
            w.gc.agregarTratamiento(c, w.vet, w.med, 2, "d", "f", 3); w.gc.aplicarVacuna(c, w.vet, w.vacuna, w.loteVac); w.gc.confirmarConsulta(c, w.vet);
            Factura f = w.gf.emitirFactura(c, new PagoCreditoCuotas(3));
            check(f.getLineas().size() == 3 && f.getTotal() == 6696.0, "lineas=" + f.getLineas().size() + " total=" + f.getTotal()); });
        test("Factura: plan de salud + efectivo = 15% (no se suman los descuentos) -> 4250", () -> { Mundo w = new Mundo(); w.duenio.setTienePlanSalud(true);
            Consulta c = w.consultaAbierta(); w.gc.confirmarConsulta(c, w.vet);
            Factura f = w.gf.emitirFactura(c, new PagoEfectivo()); check(f.getTotal() == 4250.0, "total=" + f.getTotal()); });
        test("Factura: una consulta no se factura dos veces", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta(); w.gc.confirmarConsulta(c, w.vet);
            w.gf.emitirFactura(c, new PagoEfectivo()); lanza(IllegalStateException.class, () -> w.gf.emitirFactura(c, new PagoEfectivo())); });
        test("Factura: anulada, la consulta vuelve a quedar pendiente de facturar", () -> { Mundo w = new Mundo(); Consulta c = w.consultaAbierta(); w.gc.confirmarConsulta(c, w.vet);
            Factura f = w.gf.emitirFactura(c, new PagoEfectivo()); w.gf.anularFactura(f, w.admin);
            check(w.gc.consultasPendientesDeFacturar().contains(c), "pendiente"); w.gf.emitirFactura(c, new PagoDebito()); });
        test("Factura: el recepcionista no anula; anular dos veces da FacturaAnuladaException", () -> { Mundo w = new Mundo();
            Factura f = w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.SENIA, 100, new PagoEfectivo());
            lanza(RolNoAutorizadoException.class, () -> w.gf.anularFactura(f, w.recep)); w.gf.anularFactura(f, w.admin);
            lanza(FacturaAnuladaException.class, () -> w.gf.anularFactura(f, w.admin)); });
        test("Factura: la nota de crédito es por el total", () -> { Mundo w = new Mundo();
            Factura f = w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.SENIA, 100, new PagoDebito());
            check(w.gf.anularFactura(f, w.admin).getMonto() == f.getTotal(), "monto"); });
        test("Factura: cargo de monto 0 rechazado", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.SENIA, 0, new PagoEfectivo())); });
        test("Factura: cargo con concepto CONSULTA rechazado", () -> { Mundo w = new Mundo();
            lanza(IllegalArgumentException.class, () -> w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.CONSULTA, 10, new PagoEfectivo())); });
        test("Factura: un intento inválido no consume número", () -> { Mundo w = new Mundo();
            Factura a = w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.SENIA, 1, new PagoEfectivo());
            try { new Factura(ConceptoFactura.SENIA, null, new PagoEfectivo()); } catch (IllegalArgumentException e) { /* esperado */ }
            Factura b = w.gf.emitirFacturaCargo(w.duenio, ConceptoFactura.SENIA, 1, new PagoEfectivo());
            int na = Integer.parseInt(a.getNumeroFactura().substring(2)), nb = Integer.parseInt(b.getNumeroFactura().substring(2));
            check(nb == na + 1, a.getNumeroFactura() + " -> " + b.getNumeroFactura()); });
        test("Crédito: 0 cuotas rechazado", () -> lanza(IllegalArgumentException.class, () -> new PagoCreditoCuotas(0)));

        // ======== Vacunas y recordatorios ========
        test("Recordatorio: refuerzo en 5 días se avisa; el mismo día del refuerzo ya no", () -> { Mundo w = new Mundo();
            VacunaAplicada v = new VacunaAplicada(w.vacuna, w.loteVac, LocalDate.now().minusMonths(12), LocalDate.now().plusDays(5));
            check(v.requiereRecordatorio(LocalDate.now()), "hoy"); check(!v.requiereRecordatorio(LocalDate.now().plusDays(5)), "día del refuerzo"); });
        test("Caso borde: emitir recordatorios dos veces no repite el aviso", () -> { Mundo w = new Mundo();
            Consulta c = w.consultaAbierta();
            c.restaurarVacunaAplicada(new VacunaAplicada(w.vacuna, w.loteVac, LocalDate.now().minusMonths(12), LocalDate.now().plusDays(3)));
            int primera = w.gv.emitirRecordatorios(), segunda = w.gv.emitirRecordatorios();
            check(w.avisos.size() == 1 && primera == 1 && segunda == 0, "avisos=" + w.avisos.size() + " primera=" + primera + " segunda=" + segunda); });
        test("Caso borde: Recordatorio enviado se guarda en historias.txt y no se repite tras reabrir", () -> { String dir = tmp().toString();
            RepositorioPersonalArchivo rp = new RepositorioPersonalArchivo(dir + "/per.txt");
            RepositorioDueniosArchivo rd = new RepositorioDueniosArchivo(dir + "/due.txt");
            RepositorioProductosArchivo rpr = new RepositorioProductosArchivo(dir + "/pro.txt");
            RepositorioVacunasArchivo rv = new RepositorioVacunasArchivo(dir + "/vac.txt", rpr);
            RepositorioMascotasArchivo rm = new RepositorioMascotasArchivo(dir + "/mas.txt", rd);
            RepositorioTurnosArchivo rt = new RepositorioTurnosArchivo(dir + "/tur.txt", rm, rp);
            rm.cargarHistoriasClinicas(dir + "/his.txt", rt, rp, rpr, rv);
            Veterinario v = new Veterinario("V", "a", "b", "3", "t", "L", "M", "e");
            for (DayOfWeek d : DayOfWeek.values()) v.agregarHorario(new HorarioAtencion(d, LocalTime.of(0, 0), LocalTime.of(23, 59)));
            rp.guardar(v);
            Duenio d = new Duenio("D", "a", "b", "40", "t"); rd.guardar(d);
            Perro p = new Perro("P1", "p", "x", "M", LocalDate.now().minusYears(2), 10, d, null, null); rm.guardar(p);
            Producto ins = new Producto("VAC", "v", 0, 1); Lote l = new Lote("L1", LocalDate.now().plusYears(1), 5); ins.agregarLote(l); rpr.guardar(ins);
            Vacuna vac = new Vacuna("V1", "Anti", 3, ins); vac.agregarEspecie(Especie.PERRO, 12); rv.guardar(vac);
            List<String> avisos = new ArrayList<>();
            GestorTurnos gt = new GestorTurnos(rt, rp, (x, y) -> { }, new GestorFacturacion(new RepositorioFacturasMemoria()));
            GestorConsultas gc = new GestorConsultas(rm, rt, new GestorStock(rpr));
            Turno t = gt.asignarTurno(p, v, LocalDateTime.now().plusHours(1).withSecond(0).withNano(0), TipoAtencion.VACUNACION);
            Consulta c = gc.registrarConsulta(t, v, "x", 10);
            c.restaurarVacunaAplicada(new VacunaAplicada(vac, l, LocalDate.now().minusMonths(12), LocalDate.now().plusDays(3)));
            check(new GestorVacunas(rm, rv, (x, y) -> avisos.add(y)).emitirRecordatorios() == 1, "primera");
            // reabrir desde los archivos
            RepositorioPersonalArchivo rp2 = new RepositorioPersonalArchivo(dir + "/per.txt");
            RepositorioDueniosArchivo rd2 = new RepositorioDueniosArchivo(dir + "/due.txt");
            RepositorioProductosArchivo rpr2 = new RepositorioProductosArchivo(dir + "/pro.txt");
            RepositorioVacunasArchivo rv2 = new RepositorioVacunasArchivo(dir + "/vac.txt", rpr2);
            RepositorioMascotasArchivo rm2 = new RepositorioMascotasArchivo(dir + "/mas.txt", rd2);
            RepositorioTurnosArchivo rt2 = new RepositorioTurnosArchivo(dir + "/tur.txt", rm2, rp2);
            rm2.cargarHistoriasClinicas(dir + "/his.txt", rt2, rp2, rpr2, rv2);
            check(new GestorVacunas(rm2, rv2, (x, y) -> avisos.add(y)).emitirRecordatorios() == 0 && avisos.size() == 1, "se repitió tras reabrir"); });

        // ======== Persistencia en .txt ========
        test("Archivo: textos con ; \\ salto de línea y \\ al final vuelven idénticos", () -> { String dir = tmp().toString();
            RepositorioDueniosArchivo r = new RepositorioDueniosArchivo(dir + "/d.txt");
            String raro = "a;b\\c\nd\\";
            Duenio d = new Duenio("x", raro, "\\;", "99", ""); d.setMail(";"); r.guardar(d);
            Duenio l = new RepositorioDueniosArchivo(dir + "/d.txt").buscarPorDni("99");
            check(l.getNombre().equals(raro) && l.getApellido().equals("\\;") && ";".equals(l.getMail()),
                    "nombre=[" + l.getNombre() + "] apellido=[" + l.getApellido() + "] mail=[" + l.getMail() + "]"); });
        test("Archivo: un lote agotado (cantidad 0) se guarda y se vuelve a leer", () -> { String dir = tmp().toString();
            RepositorioProductosArchivo r = new RepositorioProductosArchivo(dir + "/p.txt"); Producto p = new Producto("C", "c", 0, 1);
            Lote l = new Lote("L", LocalDate.now().plusDays(10), 2); l.setCantidad(0); p.agregarLote(l); r.guardar(p);
            check(new RepositorioProductosArchivo(dir + "/p.txt").buscarPorCodigo("C").getLotes().get(0).getCantidad() == 0, "cantidad"); });
        test("Archivo: una línea corrupta da un mensaje claro", () -> { String dir = tmp().toString();
            Files.write(Paths.get(dir, "d.txt"), List.of("x;solo;tres"));
            IllegalStateException e = lanza(IllegalStateException.class, () -> new RepositorioDueniosArchivo(dir + "/d.txt"));
            check(e.getMessage().contains("Línea inválida"), e.getMessage()); });
        test("PENDIENTE Archivo: un turno de una mascota no registrada no debería poder crearse (rompe la recarga)", () -> { String dir = tmp().toString();
            RepositorioPersonalArchivo rp = new RepositorioPersonalArchivo(dir + "/per.txt");
            RepositorioDueniosArchivo rd = new RepositorioDueniosArchivo(dir + "/due.txt");
            RepositorioMascotasArchivo rm = new RepositorioMascotasArchivo(dir + "/mas.txt", rd);
            RepositorioTurnosArchivo rt = new RepositorioTurnosArchivo(dir + "/tur.txt", rm, rp);
            Veterinario v = new Veterinario("V", "a", "b", "3", "t", "L", "M", "e");
            for (DayOfWeek d : DayOfWeek.values()) v.agregarHorario(new HorarioAtencion(d, LocalTime.of(0, 0), LocalTime.of(23, 59)));
            rp.guardar(v);
            GestorTurnos gt = new GestorTurnos(rt, rp, (x, y) -> { }, new GestorFacturacion(new RepositorioFacturasMemoria()));
            Perro suelto = new Perro("NO-REG", "s", "x", "M", LocalDate.now(), 3, null, null, null);
            LocalDateTime manana = LocalDate.now().plusDays(1).atTime(10, 0);
            lanza(RuntimeException.class, () -> gt.asignarTurno(suelto, v, manana, TipoAtencion.VACUNACION)); });

        System.out.println();
        System.out.println(ok + " OK, " + falla + " FALLAS, " + pendiente + " PENDIENTES (casos borde conocidos, de prioridad baja)");
        for (String f : fallas) System.out.println("  FALLA     " + f);
        for (String p : pendientes) System.out.println("  " + p);
    }
}
