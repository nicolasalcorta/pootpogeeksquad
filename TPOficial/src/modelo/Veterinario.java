package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Veterinario extends Personal {
	private String matricula;
	private String especialidad;
	private boolean deGuardia;
	private final List<HorarioAtencion> horarios;

	public Veterinario(String id, String nombre, String apellido, String dni, String telefono, String legajo,
					   String matricula, String especialidad) {
		super(id, nombre, apellido, dni, telefono, legajo, RolPersonal.VETERINARIO);
		if (matricula == null || matricula.trim().isEmpty()) {
			throw new IllegalArgumentException("La matrícula es obligatoria.");
		}
		this.matricula = matricula;
		this.especialidad = especialidad;
		this.deGuardia = false;
		this.horarios = new ArrayList<>();
	}

	public void agregarHorario(HorarioAtencion horario) {
		if (horario == null) {
			throw new IllegalArgumentException("El horario no puede ser nulo.");
		}
		horarios.add(horario);
	}

	public boolean atiendeEnHorario(LocalDateTime fechaHora) {
		for (HorarioAtencion h : horarios) {
			if (h.incluye(fechaHora)) {
				return true;
			}
		}
		return false;
	}

	/** Indica si un mismo horario de atención cubre todo el período [inicio, fin). */
	public boolean atiendeEntre(LocalDateTime inicio, LocalDateTime fin) {
		for (HorarioAtencion h : horarios) {
			if (h.incluye(inicio) && h.incluye(fin.minusMinutes(1))) {
				return true;
			}
		}
		return false;
	}

	public String getMatricula() {
		return matricula;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public boolean isDeGuardia() {
		return deGuardia;
	}

	public void setDeGuardia(boolean deGuardia) {
		this.deGuardia = deGuardia;
	}

	public List<HorarioAtencion> getHorarios() {
		return Collections.unmodifiableList(horarios);
	}
}
