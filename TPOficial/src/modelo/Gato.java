package modelo;

import java.time.LocalDate;

public class Gato extends Mascota {
	private boolean esInterior;

	public Gato(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento,
				double pesoActual, Duenio duenio, boolean esInterior) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, duenio);
		this.esInterior = esInterior;
	}

	@Override
	public Especie getEspecie() {
		return Especie.GATO;
	}

	@Override
	public String getInfoEspecie() {
		return "Felino [Hábito: " + (esInterior ? "Exclusivo de Interior" : "Acceso al Exterior") + "]";
	}

	public boolean isEsInterior() {
		return esInterior;
	}
}
