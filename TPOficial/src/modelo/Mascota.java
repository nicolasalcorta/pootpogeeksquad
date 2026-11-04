package modelo;

import java.time.LocalDate;
import java.time.Period;

public abstract class Mascota {
	private String idMascota;
	private String nombre;
	private String raza;
	private String sexo;
	private LocalDate fechaNacimiento;
	private double pesoActual;
	private Dueño dueño;
	private final HistoriaClinica historiaClinica;

	public Mascota(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento, double pesoActual, Dueño dueño) {
		if (idMascota == null || idMascota.trim().isEmpty()) {
			throw new IllegalArgumentException("El id de la mascota es obligatorio.");
		}
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
		this.historiaClinica = new HistoriaClinica("HC-" + idMascota);

		if (dueño != null) {
			dueño.agregarMascota(this);
		}
	}

	public abstract Especie getEspecie();

	public abstract String getDescripcionEspecie();

	public int getEdad() {
		return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, LocalDate.now()).getYears();
	}

	public void cambiarDueño(Dueño nuevo) {
		if (nuevo == null) {
			throw new IllegalArgumentException("El nuevo dueño no puede ser nulo.");
		}
		if (this.dueño != null) {
			this.dueño.quitarMascota(this);
		}
		this.dueño = nuevo;
		nuevo.agregarMascota(this);
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
	}

	public void setDueño(Dueño dueño) {
		this.dueño = dueño;
	}

	public HistoriaClinica getHistoriaClinica() {
		return historiaClinica;
	}
}
