package modelo;

import java.time.LocalDate;

public class Conejo extends Mascota{
	private boolean tieneJaula;
	
	public Conejo(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento, double pesoActual,
			Dueño dueño) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, dueño);
		this.tieneJaula = tieneJaula;
	}
	
	@Override
	public Especie getEspecie() {
		return Especie.CONEJO; 
	}
	
	@Override
	public String getDescripcionEspecie() {
		return "Conejo [Hábito: " + (tieneJaula ? "Tiene Jaula" : "Solo está en jaula") + "]"; 
	}
	
	public boolean isEsInterior() { 
		return tieneJaula; 
	}
}
