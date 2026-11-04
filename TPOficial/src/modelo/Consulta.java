package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import excepciones.PesoNoRegistradoException;
import excepciones.VacunaNoAplicableException;

public class Consulta {
	private LocalDateTime fechaHora;
	private String motivo;
	private double pesoDelDia;
	private String diagnostico;
	private String observaciones;
	private Veterinario veterinario;
	private Turno turno;
	private Factura factura;
	private boolean confirmada;
	private final List<Tratamiento> tratamientos;
	private final List<VacunaAplicada> vacunas;

	public Consulta(String motivo, double pesoDelDia) {
		this.fechaHora = LocalDateTime.now();
		this.motivo = motivo;
		this.pesoDelDia = pesoDelDia;
		this.diagnostico = "";
		this.observaciones = "";
		this.tratamientos = new ArrayList<>();
		this.vacunas = new ArrayList<>();
	}

	public boolean tienePesoRegistrado() {
		return pesoDelDia > 0;
	}

	public Tratamiento agregarTratamiento(Producto medicamento, int cantidad, String dosis, String frecuencia,
			int duracionDias) throws PesoNoRegistradoException {
		verificarEditable();
		if (!tienePesoRegistrado()) {
			throw new PesoNoRegistradoException("No se puede indicar un medicamento: no se registró el peso del animal en esta consulta.");
		}
		Tratamiento tratamiento = new Tratamiento(medicamento, cantidad, dosis, frecuencia, duracionDias);
		this.tratamientos.add(tratamiento);
		return tratamiento;
	}

	public VacunaAplicada aplicarVacuna(Vacuna vacuna, Lote lote) throws VacunaNoAplicableException {
		verificarEditable();
		if (vacuna == null || lote == null) {
			throw new IllegalArgumentException("La vacuna y el lote son obligatorios.");
		}
		if (turno == null) {
			throw new IllegalStateException("La consulta no tiene un turno asociado.");
		}
		Mascota mascota = turno.getMascota();
		if (!vacuna.esAptaParaEspecie(mascota.getEspecie())) {
			throw new VacunaNoAplicableException("La vacuna " + vacuna.getNombre() + " no corresponde a la especie " + mascota.getEspecie() + ".");
		}
		if (!vacuna.esAplicableA(mascota)) {
			throw new VacunaNoAplicableException("La mascota no tiene la edad mínima (" + vacuna.getEdadMinimaMeses()
					+ " meses) para la vacuna " + vacuna.getNombre() + ".");
		}
		if (!vacuna.usaLote(lote)) {
			throw new VacunaNoAplicableException("El lote " + lote.getNumeroLote() + " no corresponde al producto de la vacuna "
					+ vacuna.getNombre() + ".");
		}
		LocalDate hoy = LocalDate.now();
		if (lote.estaVencido(hoy)) {
			throw new VacunaNoAplicableException("El lote " + lote.getNumeroLote() + " está vencido y no puede usarse.");
		}
		VacunaAplicada aplicada = new VacunaAplicada(vacuna, lote, vacuna.proximoRefuerzo(mascota.getEspecie(), hoy));
		this.vacunas.add(aplicada);
		return aplicada;
	}

	/**
	 * Vuelve a incorporar una vacuna ya aplicada, sin repetir las validaciones de aplicarVacuna()
	 * (que dependen de la fecha de hoy). Se usa al leer la historia clínica de un archivo.
	 */
	public void restaurarVacunaAplicada(VacunaAplicada aplicada) {
		verificarEditable();
		if (aplicada == null) {
			throw new IllegalArgumentException("La vacuna aplicada no puede ser nula.");
		}
		this.vacunas.add(aplicada);
	}

	public void confirmar() {
		if (confirmada) {
			throw new IllegalStateException("La consulta ya fue confirmada.");
		}
		this.confirmada = true;
	}

	private void verificarEditable() {
		if (confirmada) {
			throw new IllegalStateException("La consulta ya fue confirmada y no admite cambios.");
		}
	}

	// Getters y setters
	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		verificarEditable();
		this.fechaHora = fechaHora;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		verificarEditable();
		this.motivo = motivo;
	}

	public double getPesoDelDia() {
		return pesoDelDia;
	}

	public void setPesoDelDia(double pesoDelDia) {
		verificarEditable();
		this.pesoDelDia = pesoDelDia;
	}

	public String getDiagnostico() {
		return diagnostico;
	}

	public void setDiagnostico(String diagnostico) {
		verificarEditable();
		this.diagnostico = diagnostico;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		verificarEditable();
		this.observaciones = observaciones;
	}

	public Veterinario getVeterinario() {
		return veterinario;
	}

	public void setVeterinario(Veterinario veterinario) {
		this.veterinario = veterinario;
	}

	public Turno getTurno() {
		return turno;
	}

	public void setTurno(Turno turno) {
		this.turno = turno;
	}

	public Factura getFactura() {
		return factura;
	}

	public void setFactura(Factura factura) {
		this.factura = factura;
	}

	public boolean isConfirmada() {
		return confirmada;
	}

	public List<Tratamiento> getTratamientos() {
		return Collections.unmodifiableList(tratamientos);
	}

	public List<VacunaAplicada> getVacunas() {
		return Collections.unmodifiableList(vacunas);
	}
}
