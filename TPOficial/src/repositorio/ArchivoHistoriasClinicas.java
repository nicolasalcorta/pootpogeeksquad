package repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import excepciones.PesoNoRegistradoException;
import modelo.Consulta;
import modelo.HistoriaClinica;
import modelo.Lote;
import modelo.Mascota;
import modelo.NotaRectificatoria;
import modelo.Personal;
import modelo.Producto;
import modelo.Tratamiento;
import modelo.Turno;
import modelo.Vacuna;
import modelo.VacunaAplicada;
import modelo.Veterinario;

/**
 * Guarda las historias clínicas de todas las mascotas en un único archivo .txt.
 * Lo usa RepositorioMascotasArchivo, porque la historia forma parte de la mascota (composición).
 *
 * Cada consulta se identifica por el id de la mascota y su posición en la historia (0, 1, 2...).
 * Tipos de línea:
 * C;idMascota;fechaHora;motivo;peso;diagnostico;observaciones;dniVeterinario;fechaHoraTurno;confirmada
 * T;idMascota;nroConsulta;codigoProducto;cantidad;dosis;frecuencia;duracionDias
 * V;idMascota;nroConsulta;nombreVacuna;numeroLote;fechaAplicacion;fechaProximoRefuerzo;recordatorioEnviado
 * N;idMascota;nroConsulta;fecha;dniVeterinario;descripcion
 */
public class ArchivoHistoriasClinicas extends ArchivoTexto {

    public ArchivoHistoriasClinicas(String rutaArchivo) {
        super(rutaArchivo);
    }

    /** Escribe las historias clínicas de todas las mascotas. */
    public void guardar(Collection<Mascota> mascotas) {
        List<String> lineas = new ArrayList<>();
        for (Mascota m : mascotas) {
            HistoriaClinica historia = m.getHistoriaClinica();
            List<Consulta> consultas = historia.getConsultas();
            for (int i = 0; i < consultas.size(); i++) {
                Consulta c = consultas.get(i);
                String dniVet = c.getVeterinario() == null ? "" : c.getVeterinario().getDni();
                String turno = c.getTurno() == null ? "" : c.getTurno().getFechaHora().toString();
                lineas.add(unir("C", m.getIdMascota(), c.getFechaHora(), c.getMotivo(), c.getPesoDelDia(),
                        c.getDiagnostico(), c.getObservaciones(), dniVet, turno, c.isConfirmada()));
                for (Tratamiento t : c.getTratamientos()) {
                    lineas.add(unir("T", m.getIdMascota(), i, t.getMedicamento().getCodigo(), t.getCantidad(),
                            t.getDosis(), t.getFrecuencia(), t.getDuracionDias()));
                }
                for (VacunaAplicada v : c.getVacunas()) {
                    lineas.add(unir("V", m.getIdMascota(), i, v.getVacuna().getNombre(), v.getLote().getNumeroLote(),
                            v.getFechaAplicacion(), v.getFechaProximoRefuerzo(), v.isRecordatorioEnviado()));
                }
            }
            for (NotaRectificatoria n : historia.getNotas()) {
                lineas.add(unir("N", m.getIdMascota(), consultas.indexOf(n.getConsulta()), n.getFecha(),
                        n.getFirmadaPor().getDni(), n.getDescripcion()));
            }
        }
        escribirLineas(lineas);
    }

    /**
     * Lee el archivo y reconstruye las consultas dentro de la historia de cada mascota.
     * Las consultas confirmadas se confirman al final, después de agregarles tratamientos y vacunas.
     */
    public void cargar(RepositorioMascotas mascotas, RepositorioTurnos turnos, RepositorioPersonal personal,
                       RepositorioProductos productos, RepositorioVacunas vacunas) {
        List<Consulta> aConfirmar = new ArrayList<>();
        for (String linea : leerLineas()) {
            try {
                String[] c = separar(linea);
                Mascota mascota = mascotas.buscarPorId(c[1]);
                if (mascota == null) {
                    throw new IllegalStateException("No existe la mascota " + c[1] + ".");
                }
                switch (c[0]) {
                    case "C":
                        Consulta consulta = mascota.getHistoriaClinica().crearConsulta(c[3], Double.parseDouble(c[4]));
                        consulta.setFechaHora(LocalDateTime.parse(c[2]));
                        consulta.setDiagnostico(c[5]);
                        consulta.setObservaciones(c[6]);
                        consulta.setVeterinario(veterinario(personal, c[7]));
                        consulta.setTurno(buscarTurno(turnos, mascota, c[8]));
                        if (Boolean.parseBoolean(c[9])) {
                            aConfirmar.add(consulta);
                        }
                        break;
                    case "T":
                        Producto medicamento = productos.buscarPorCodigo(c[3]);
                        if (medicamento == null) {
                            throw new IllegalStateException("No existe el producto " + c[3] + ".");
                        }
                        consulta(mascota, c[2]).agregarTratamiento(medicamento, Integer.parseInt(c[4]), c[5], c[6],
                                Integer.parseInt(c[7]));
                        break;
                    case "V":
                        Vacuna vacuna = vacunas.buscarPorNombre(c[3]);
                        if (vacuna == null) {
                            throw new IllegalStateException("No existe la vacuna " + c[3] + ".");
                        }
                        LocalDate refuerzo = c[6].isEmpty() ? null : LocalDate.parse(c[6]);
                        VacunaAplicada aplicada = new VacunaAplicada(vacuna, buscarLote(vacuna, c[4]), LocalDate.parse(c[5]), refuerzo);
                        // Campo agregado después: los archivos anteriores no lo tienen y se toman como "no enviado".
                        if (c.length > 7 && Boolean.parseBoolean(c[7])) {
                            aplicada.marcarRecordatorioEnviado();
                        }
                        consulta(mascota, c[2]).restaurarVacunaAplicada(aplicada);
                        break;
                    case "N":
                        Veterinario firma = veterinario(personal, c[4]);
                        mascota.getHistoriaClinica().restaurarNota(
                                new NotaRectificatoria(consulta(mascota, c[2]), firma, c[5], LocalDateTime.parse(c[3])));
                        break;
                    default:
                        throw new IllegalStateException("Tipo de registro desconocido: " + c[0]);
                }
            } catch (PesoNoRegistradoException | RuntimeException e) {
                throw new IllegalStateException("Línea inválida en " + getRuta() + ": " + linea, e);
            }
        }
        for (Consulta c : aConfirmar) {
            c.confirmar();
        }
    }

    private static Consulta consulta(Mascota mascota, String nroConsulta) {
        return mascota.getHistoriaClinica().getConsultas().get(Integer.parseInt(nroConsulta));
    }

    private static Veterinario veterinario(RepositorioPersonal personal, String dni) {
        if (dni.isEmpty()) {
            return null;
        }
        Personal p = personal.buscarPorDni(dni);
        if (!(p instanceof Veterinario)) {
            throw new IllegalStateException("No existe el veterinario con DNI " + dni + ".");
        }
        return (Veterinario) p;
    }

    private static Turno buscarTurno(RepositorioTurnos turnos, Mascota mascota, String fechaHora) {
        if (fechaHora.isEmpty()) {
            return null;
        }
        LocalDateTime momento = LocalDateTime.parse(fechaHora);
        for (Turno t : turnos.listarTodos()) {
            if (t.getMascota() == mascota && t.getFechaHora().equals(momento)) {
                return t;
            }
        }
        throw new IllegalStateException("No existe el turno de " + mascota.getNombre() + " del " + fechaHora + ".");
    }

    private static Lote buscarLote(Vacuna vacuna, String numeroLote) {
        for (Lote l : vacuna.getInsumo().getLotes()) {
            if (l.getNumeroLote().equals(numeroLote)) {
                return l;
            }
        }
        throw new IllegalStateException("No existe el lote " + numeroLote + " de la vacuna " + vacuna.getNombre() + ".");
    }
}
