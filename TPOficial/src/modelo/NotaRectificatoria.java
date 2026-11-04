package modelo;

import java.time.LocalDateTime;

public class NotaRectificatoria {
    private final LocalDateTime fecha;
    private final String descripcion;
    private final Veterinario firmadaPor;
    private final Consulta consulta;

    public NotaRectificatoria(Consulta consulta, Veterinario firmadaPor, String descripcion) {
        this(consulta, firmadaPor, descripcion, LocalDateTime.now());
    }

    /** Recrea una nota ya registrada, con su fecha original (por ejemplo, al leerla de un archivo). */
    public NotaRectificatoria(Consulta consulta, Veterinario firmadaPor, String descripcion, LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la nota es obligatoria.");
        }
        if (consulta == null || firmadaPor == null) {
            throw new IllegalArgumentException("La consulta y el veterinario que firma son obligatorios.");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la nota no puede estar vacía.");
        }
        this.fecha = fecha;
        this.consulta = consulta;
        this.firmadaPor = firmadaPor;
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Veterinario getFirmadaPor() {
        return firmadaPor;
    }

    public Consulta getConsulta() {
        return consulta;
    }
}
