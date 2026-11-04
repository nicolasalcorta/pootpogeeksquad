package modelo;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HorarioAtencion {
    private final DayOfWeek diaSemana;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    public HorarioAtencion(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        if (diaSemana == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("El día y las horas del horario son obligatorios.");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public boolean incluye(LocalDateTime fechaHora) {
        LocalTime hora = fechaHora.toLocalTime();
        return fechaHora.getDayOfWeek() == diaSemana && !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }
}
