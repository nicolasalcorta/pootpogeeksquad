package modelo;

import java.time.LocalDate;

public class VacunaAplicada {
	private final Vacuna vacuna;
	private final Lote lote;
	private final LocalDate fechaAplicacion;
	private final LocalDate fechaProximoRefuerzo;

	public VacunaAplicada(Vacuna vacuna, Lote lote, LocalDate fechaProximoRefuerzo) {
		if (vacuna == null || lote == null) {
			throw new IllegalArgumentException("La vacuna y el lote son obligatorios.");
		}
		this.vacuna = vacuna;
		this.lote = lote;
		this.fechaAplicacion = LocalDate.now();
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
