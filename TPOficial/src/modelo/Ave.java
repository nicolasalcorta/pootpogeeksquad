package modelo;

import java.time.LocalDate;

public class Ave extends Mascota {
	private boolean puedeVolar;

	public Ave(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento,
			   double pesoActual, Duenio duenio, boolean puedeVolar) {
		super(idMascota, nombre, raza, sexo, fechaNacimiento, pesoActual, duenio);
		this.puedeVolar = puedeVolar;
	}

	@Override
	public Especie getEspecie() {
		return Especie.AVE;
	}

	@Override
	public String getInfoEspecie() {
		return "Ave [Puede volar: " + (puedeVolar ? "Sí" : "No") + "]";
	}

	public boolean isPuedeVolar() {
		return puedeVolar;
	}
}
