package modelo;

import java.time.LocalDate;

public class Perro extends Mascota{
	private String numeroChip;
	private String tamanio;
	
	public Perro(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento,
			double pesoActual, Dueño dueño) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, dueño);
		this.numeroChip = numeroChip;
		this.tamanio = tamanio;
	}
	
	@Override 
	public Especie getEspecie() {
		return Especie.PERRO; 
	}
	
	@Override public String getDescripcionEspecie() {
		return "Canino [Chip: " + (numeroChip != null ? numeroChip : "Sin Chip") + ", Tamaño: " + tamanio + "]";
	} 
	
	public String getNumeroChip() { 
		return numeroChip; 
	} 
	
	public String getTamaño() {
		return tamanio; 
		}
}
