package modelo;

import java.time.LocalDate;

public class Conejo extends Mascota {
	private boolean tieneJaula;

	public Conejo(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento,
				  double pesoActual, Duenio duenio, boolean tieneJaula) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, duenio);
		this.tieneJaula = tieneJaula;
	}

	@Override
	public Especie getEspecie() {
		return Especie.CONEJO;
	}

	@Override
	public String getInfoEspecie() {
		return "Conejo [Tiene jaula: " + (tieneJaula ? "Sí" : "No") + "]";
	}

	public boolean isTieneJaula() {
		return tieneJaula;
	}
}
