package modelo;

import java.time.Duration;
import java.time.LocalDateTime;

public class Turno {
	private static final long HORAS_CANCELACION_SIN_CARGO = 2;
	private static final double PORCENTAJE_CANCELACION_TARDIA = 0.20;

	private LocalDateTime fechaHora;
	private TipoAtencion tipo;
	private EstadoTurno estado;
	private boolean esUrgencia;
	private boolean seniaPagada;
	private Mascota mascota;
	private Veterinario veterinario;

	public Turno(LocalDateTime fechaHora, TipoAtencion tipo, Mascota mascota, Veterinario veterinario, boolean esUrgencia) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La fecha y hora del turno son obligatorias.");
		}
		if (tipo == null) {
			throw new IllegalArgumentException("El tipo de atención es obligatorio.");
		}
		if (mascota == null) {
			throw new IllegalArgumentException("La mascota es obligatoria.");
		}
		if (veterinario == null) {
			throw new IllegalArgumentException("El veterinario es obligatorio.");
		}
		this.fechaHora = fechaHora;
		this.tipo = tipo;
		this.mascota = mascota;
		this.veterinario = veterinario;
		this.esUrgencia = esUrgencia;
		this.seniaPagada = true; // solo pasa a false si el dueño debe pagar seña (lo decide el gestor)
		this.estado = EstadoTurno.PROGRAMADO;
	}

	public LocalDateTime getFechaHoraFin() {
		return fechaHora.plusMinutes(tipo.getDuracionMinutos());
	}

	public void reprogramar(LocalDateTime nuevaFecha) {
		if (nuevaFecha == null) {
			throw new IllegalArgumentException("La nueva fecha no puede ser nula.");
		}
		if (estado != EstadoTurno.PROGRAMADO) {
			throw new IllegalStateException("Solo se puede reprogramar un turno programado.");
		}
		this.fechaHora = nuevaFecha;
	}

	public double cancelar(LocalDateTime momento) {
		if (momento == null) {
			throw new IllegalArgumentException("El momento de la cancelación es obligatorio.");
		}
		if (estado != EstadoTurno.PROGRAMADO) {
			throw new IllegalStateException("Solo se puede cancelar un turno programado.");
		}
		this.estado = EstadoTurno.CANCELADO;
		Duration anticipacion = Duration.between(momento, fechaHora);
		if (anticipacion.compareTo(Duration.ofHours(HORAS_CANCELACION_SIN_CARGO)) >= 0) {
			return 0.0;
		}
		return tipo.getPrecioBase() * PORCENTAJE_CANCELACION_TARDIA;
	}

	public void marcarAtendido() {
		if (estado != EstadoTurno.PROGRAMADO) {
			throw new IllegalStateException("Solo se puede atender un turno programado.");
		}
		this.estado = EstadoTurno.ATENDIDO;
	}

	public void marcarSeniaPagada() {
		if (estado != EstadoTurno.PROGRAMADO) {
			throw new IllegalStateException("Solo se puede registrar la seña de un turno programado.");
		}
		this.seniaPagada = true;
	}

	public void marcarAusente() {
		if (estado != EstadoTurno.PROGRAMADO) {
			throw new IllegalStateException("Solo se puede marcar ausente un turno programado.");
		}
		this.estado = EstadoTurno.AUSENTE;
	}

	public boolean seSuperponeCon(Turno otro) {
		if (otro == null || otro.getEstado() == EstadoTurno.CANCELADO) {
			return false;
		}
		return this.fechaHora.isBefore(otro.getFechaHoraFin()) && this.getFechaHoraFin().isAfter(otro.getFechaHora());
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public TipoAtencion getTipo() {
		return tipo;
	}

	public void setTipo(TipoAtencion tipo) {
		this.tipo = tipo;
	}

	public EstadoTurno getEstado() {
		return estado;
	}

	public void setEstado(EstadoTurno estado) {
		this.estado = estado;
	}

	public boolean isEsUrgencia() {
		return esUrgencia;
	}

	public void setEsUrgencia(boolean esUrgencia) {
		this.esUrgencia = esUrgencia;
	}

	public boolean isSeniaPagada() {
		return seniaPagada;
	}

	public void setSeniaPagada(boolean seniaPagada) {
		this.seniaPagada = seniaPagada;
	}

	public Mascota getMascota() {
		return mascota;
	}

	public void setMascota(Mascota mascota) {
		this.mascota = mascota;
	}

	public Veterinario getVeterinario() {
		return veterinario;
	}

	public void setVeterinario(Veterinario veterinario) {
		this.veterinario = veterinario;
	}

	@Override
	public String toString() {
		return "Turno [" + tipo + "] - Fecha: " + fechaHora + " | Mascota: " + (mascota != null ? mascota.getNombre() : "N/A")
				+ " | Vet: " + (veterinario != null ? veterinario.getNombreCompleto() : "N/A") + " | Estado: " + estado
				+ (esUrgencia ? " (URGENCIA)" : "");
	}
}
