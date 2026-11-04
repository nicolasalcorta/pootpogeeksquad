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
	private Duenio duenio;
	private final HistoriaClinica historiaClinica;

	public Mascota(String idMascota, String nombre, String raza, String sexo, LocalDate fechaNacimiento, double pesoActual, Duenio duenio) {
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
		this.duenio = duenio;
		this.historiaClinica = new HistoriaClinica("HC-" + idMascota);

		if (duenio != null) {
			duenio.agregarMascota(this);
		}
	}

	public abstract Especie getEspecie();

	public abstract String getInfoEspecie();

	public int getEdad() {
		return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, LocalDate.now()).getYears();
	}

	public void cambiarDuenio(Duenio nuevo) {
		if (nuevo == null) {
			throw new IllegalArgumentException("El nuevo dueño no puede ser nulo.");
		}
		if (this.duenio != null) {
			this.duenio.quitarMascota(this);
		}
		this.duenio = nuevo;
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

	public void actualizarPeso(double peso) {
		if (peso <= 0) {
			throw new IllegalArgumentException("El peso debe ser mayor a cero.");
		}
		this.pesoActual = peso;
	}

	public Duenio getDuenio() {
		return duenio;
	}

	public void setDuenio(Duenio duenio) {
		this.duenio = duenio;
	}

	public HistoriaClinica getHistoriaClinica() {
		return historiaClinica;
	}
}
