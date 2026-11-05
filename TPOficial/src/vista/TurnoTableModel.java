package vista;

import java.util.List;

import modelo.Turno;

/** Modelo de la tabla de turnos de VistaTurnos. */
public class TurnoTableModel extends ModeloTabla<Turno> {
    private static final long serialVersionUID = 1L;

    public TurnoTableModel() {
        super(new String[] {"Fecha y hora", "Mascota", "Dueño", "Veterinario", "Tipo", "Estado", "Urgencia", "Seña"},
                t -> VistaBase.formatear(t.getFechaHora()),
                t -> t.getMascota().getNombre(),
                t -> t.getMascota().getDuenio() == null ? "-" : t.getMascota().getDuenio().getNombreCompleto(),
                t -> t.getVeterinario().getNombreCompleto(),
                t -> t.getTipo(),
                t -> t.getEstado(),
                t -> t.isEsUrgencia() ? "Sí" : "",
                t -> t.isSeniaPagada() ? "" : "Pendiente");
    }

    public void setTurnos(List<Turno> turnos) {
        setFilas(turnos);
    }
}
