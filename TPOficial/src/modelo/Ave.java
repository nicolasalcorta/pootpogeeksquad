package modelo;

import java.time.LocalDate;

public class Ave extends Mascota{
	private boolean puedeVolar;
	
	public Ave(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento, double pesoActual,
			Dueño dueño) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, dueño);
		this.puedeVolar = puedeVolar;
	}
	
	@Override
	public Especie getEspecie() {
		return Especie.AVE; 
	}
	
	@Override
	public String getDescripcionEspecie() {
		return "Ave [Hábito: " + (puedeVolar ? "Exclusivo de Interior" : "Acceso al Exterior pero a patio techacho") + "]"; 
	}
	
	public boolean isEsInterior() { 
		return puedeVolar; 
	}
}
