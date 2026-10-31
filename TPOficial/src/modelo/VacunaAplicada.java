package modelo;

import java.time.LocalDate;

public class VacunaAplicada {
	private Vacuna vacuna; 
	private String nombreVacuna; 
	private LocalDate fechaAplicacion; 
	private String lote;
	private LocalDate fechaProximoRefuerzo;
	
	public VacunaAplicada(Vacuna vacuna, String lote, LocalDate fechaProximoRefuerzo) {
		if (lote == null || lote.trim().isEmpty()) { 
			throw new IllegalArgumentException("El número de lote es obligatorio."); 
			} 
		this.vacuna = vacuna; 
		this.nombreVacuna = (vacuna != null) ? vacuna.getNombre() : "Vacuna"; 
		this.fechaAplicacion = LocalDate.now(); 
		this.lote = lote;
		this.fechaProximoRefuerzo = fechaProximoRefuerzo; 
	}
	
	// Método de negocio (GRASP Experto): Verifica si requiere recordatorio (7 días antes del refuerzo) 
	public boolean requiereRecordatorio() {
		if (fechaProximoRefuerzo == null) return false; 
		LocalDate hoy = LocalDate.now(); 
		return !hoy.isBefore(fechaProximoRefuerzo.minusDays(7)) && hoy.isBefore(fechaProximoRefuerzo); } 
	
	// Método de negocio: Verifica si la vacuna está vencida (pasados 30 días del refuerzo sin aplicar) 
	public boolean estaVencida() { 
		if (fechaProximoRefuerzo == null) return false; 
		return LocalDate.now().isAfter(fechaProximoRefuerzo.plusDays(30)); } 
	
	// Getters y Setters 
	public Vacuna getVacuna() {
		return vacuna; 
	} 
	
	public String getNombreVacuna() {
		return nombreVacuna; 
	} 
	public LocalDate getFechaAplicacion() {
		return fechaAplicacion; 
	} 
	public String getLote() {
		return lote; 
	}
	public LocalDate getFechaProximoRefuerzo() { 
		return fechaProximoRefuerzo; 
	} 
	
	@Override public String toString() { 
		return "Vacuna: " + nombreVacuna + " [Lote: " + lote + "] - Aplicada: " + fechaAplicacion + " - Próximo refuerzo: " + fechaProximoRefuerzo; 
	}
}
