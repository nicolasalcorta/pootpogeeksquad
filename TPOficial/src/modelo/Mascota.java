package modelo;

import java.time.LocalDate;
import java.time.Period;

public class Mascota {
	private String idMascota; 
	private String nombre; 
	private String raza; 
	private String sexo; 
	private LocalDate fechaNacimiento;
	private double pesoActual; 
	private Dueño dueño; 
	
	public Mascota(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento, double pesoActual, Dueño dueño) { 
		if (nombre == null || nombre.trim().isEmpty()) { 
			throw new IllegalArgumentException("El nombre de la mascota no puede estar vacío."); 
			} 
		this.idMascota = idMascota; 
		this.nombre = nombre; 
		this.raza = raza; 
		this.sexo = sexo; 
		this.fechaNacimiento = fechaNacimiento;
		this.pesoActual = pesoActual; 
		this.dueño = dueño;
		
		if (dueño != null) { 
			dueño.agregarMascota(this); 
		}
		
	}
	
	// Getters y Setters 
	public String getIdMascota() {
		return idMascota; 
	}
	public String getNombre() { 
		return nombre; 
	}
	public String getRaza() {
		return raza; 
	}
	public String getSexo() { 
		return sexo; 
	}
	public LocalDate getFechaNacimiento() {
		return fechaNacimiento; 
	} 
	public double getPesoActual() {
		return pesoActual; 
	}
	public void setPesoActual(double pesoActual) {
		this.pesoActual = pesoActual; 
	} 
	public Dueño getDueño() {
		return dueño; 
	} public void setDueño(Dueño dueño) {
		this.dueño = dueño; 
	}

	//Los agregué porque me daba error en las clases hijas
	public Especie getEspecie() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescripcionEspecie() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
