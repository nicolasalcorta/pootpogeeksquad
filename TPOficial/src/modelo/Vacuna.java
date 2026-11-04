package modelo;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Vacuna {
	private String idVacuna;
	private String nombre;
	private int edadMinimaMeses;
	private final Map<Especie, Integer> intervalosPorEspecie;

	public Vacuna(String idVacuna, String nombre, int edadMinimaMeses) {
		this.idVacuna = idVacuna;
		this.nombre = nombre;
		this.edadMinimaMeses = edadMinimaMeses;
		this.intervalosPorEspecie = new EnumMap<>(Especie.class);
	}

		public void agregarEspecie(Especie especie, int intervaloRefuerzoMeses) {
		if (especie == null) {
			throw new IllegalArgumentException("La especie no puede ser nula.");
		}
		if (intervaloRefuerzoMeses <= 0) {
			throw new IllegalArgumentException("El intervalo de refuerzo debe ser mayor a cero.");
		}
		intervalosPorEspecie.put(especie, intervaloRefuerzoMeses);
	}

	public boolean esAptaParaEspecie(Especie especie) {
		return intervalosPorEspecie.containsKey(especie);
	}

	public boolean esAplicableA(Mascota m) {
		if (!esAptaParaEspecie(m.getEspecie())) {
			return false;
		}
		int edadMeses = (m.getFechaNacimiento() == null) ? 0
				: (int) Period.between(m.getFechaNacimiento(), LocalDate.now()).toTotalMonths();
		return edadMeses >= edadMinimaMeses;
	}

	public LocalDate proximoRefuerzo(Especie especie, LocalDate fechaAplicacion) {
		Integer meses = intervalosPorEspecie.get(especie);
		if (meses == null) {
			throw new IllegalArgumentException("La vacuna " + nombre + " no está definida para la especie " + especie + ".");
		}
		return fechaAplicacion.plusMonths(meses);
	}

	public String getIdVacuna() {
		return idVacuna;
	}

	public String getNombre() {
		return nombre;
	}

	public int getEdadMinimaMeses() {
		return edadMinimaMeses;
	}

	public List<Especie> getEspeciesPermitidas() {
		return new ArrayList<>(intervalosPorEspecie.keySet());
	}

	public Map<Especie, Integer> getIntervalosPorEspecie() {
		return Collections.unmodifiableMap(intervalosPorEspecie);
	}
}
