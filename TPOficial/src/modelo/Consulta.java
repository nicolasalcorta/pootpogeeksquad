package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Consulta {
	private LocalDateTime fechaHora; 
	private String motivo;
	private double pesoDelDia; 
	private String diagnostico; 
	private String observaciones; 
	private Veterinario veterinario; 
	private List<Tratamiento> tratamientos;
	private List<VacunaAplicada> vacunas;

	public Consulta(String motivo, double pesoDelDia) { 
		this.fechaHora = LocalDateTime.now(); 
		this.motivo = motivo; 
		this.pesoDelDia = pesoDelDia;
		this.diagnostico = ""; 
		this.observaciones = ""; 
		this.tratamientos = new ArrayList<>(); 
		this.vacunas = new ArrayList<>(); 
	}
	
	public void agregarTratamiento(Tratamiento t) { 
		if (t != null) {
			this.tratamientos.add(t); 
			}
		}
	
	public void aplicarVacuna(VacunaAplicada v) { 
		if (v != null) {
			this.vacunas.add(v); 
			} 
		}

	//Getterr y setter
	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public double getPesoDelDia() {
		return pesoDelDia;
	}

	public void setPesoDelDia(double pesoDelDia) {
		this.pesoDelDia = pesoDelDia;
	}

	public String getDiagnostico() {
		return diagnostico;
	}

	public void setDiagnostico(String diagnostico) {
		this.diagnostico = diagnostico;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Veterinario getVeterinario() {
		return veterinario;
	}

	public void setVeterinario(Veterinario veterinario) {
		this.veterinario = veterinario;
	}

	public List<Tratamiento> getTratamientos() {
		return tratamientos;
	}

	public void setTratamientos(List<Tratamiento> tratamientos) {
		this.tratamientos = tratamientos;
	}

	public List<VacunaAplicada> getVacunas() {
		return vacunas;
	}

	public void setVacunas(List<VacunaAplicada> vacunas) {
		this.vacunas = vacunas;
	}
	
	
}
