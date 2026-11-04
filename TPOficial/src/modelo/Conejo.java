package modelo;

import java.time.LocalDate;

public class Conejo extends Mascota {
	private boolean tieneJaula;

	public Conejo(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento,
				  double pesoActual, Dueño dueño, boolean tieneJaula) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, dueño);
		this.tieneJaula = tieneJaula;
	}

	@Override
	public Especie getEspecie() {
		return Especie.CONEJO;
	}

	@Override
	public String getDescripcionEspecie() {
		return "Conejo [Tiene jaula: " + (tieneJaula ? "Sí" : "No") + "]";
	}

	public boolean isTieneJaula() {
		return tieneJaula;
	}
}
