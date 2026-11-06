package modelo;

import java.time.LocalDate;

public class VacunaAplicada {
	private final Vacuna vacuna;
	private final Lote lote;
	private final LocalDate fechaAplicacion;
	private final LocalDate fechaProximoRefuerzo;
	private boolean recordatorioEnviado;

	public VacunaAplicada(Vacuna vacuna, Lote lote, LocalDate fechaProximoRefuerzo) {
		this(vacuna, lote, LocalDate.now(), fechaProximoRefuerzo);
	}

	/** Recrea una vacuna ya aplicada, con su fecha original (por ejemplo, al leerla de un archivo). */
	public VacunaAplicada(Vacuna vacuna, Lote lote, LocalDate fechaAplicacion, LocalDate fechaProximoRefuerzo) {
		if (vacuna == null || lote == null || fechaAplicacion == null) {
			throw new IllegalArgumentException("La vacuna, el lote y la fecha de aplicación son obligatorios.");
		}
		this.vacuna = vacuna;
		this.lote = lote;
		this.fechaAplicacion = fechaAplicacion;
		this.fechaProximoRefuerzo = fechaProximoRefuerzo;
	}

	public boolean requiereRecordatorio(LocalDate hoy) {
		if (fechaProximoRefuerzo == null) return false;
		return !hoy.isBefore(fechaProximoRefuerzo.minusDays(7)) && hoy.isBefore(fechaProximoRefuerzo);
	}

	public boolean estaVencida(LocalDate hoy) {
		if (fechaProximoRefuerzo == null) return false;
		return hoy.isAfter(fechaProximoRefuerzo.plusDays(30));
	}

	/** Registra que ya se avisó al dueño de este refuerzo, para no repetir el recordatorio. */
	public void marcarRecordatorioEnviado() {
		this.recordatorioEnviado = true;
	}

	public boolean isRecordatorioEnviado() {
		return recordatorioEnviado;
	}

	public Vacuna getVacuna() {
		return vacuna;
	}

	public String getNombreVacuna() {
		return vacuna.getNombre();
	}

	public Lote getLote() {
		return lote;
	}

	public LocalDate getFechaAplicacion() {
		return fechaAplicacion;
	}

	public LocalDate getFechaProximoRefuerzo() {
		return fechaProximoRefuerzo;
	}

	@Override
	public String toString() {
		return "Vacuna: " + vacuna.getNombre() + " [Lote: " + lote.getNumeroLote() + "] - Aplicada: " + fechaAplicacion
				+ " - Próximo refuerzo: " + fechaProximoRefuerzo;
	}
}
