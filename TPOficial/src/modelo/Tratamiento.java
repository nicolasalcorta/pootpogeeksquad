package modelo;

public class Tratamiento {
	private final Producto medicamento;
	private final String dosis;
	private final String frecuencia;
	private final int duracionDias;

	public Tratamiento(Producto medicamento, String dosis, String frecuencia, int duracionDias) {
		if (medicamento == null) {
			throw new IllegalArgumentException("El medicamento es obligatorio.");
		}
		if (dosis == null || dosis.trim().isEmpty()) {
			throw new IllegalArgumentException("La dosis no puede estar vacía.");
		}
		if (duracionDias <= 0) {
			throw new IllegalArgumentException("La duración debe ser mayor a cero días.");
		}
		this.medicamento = medicamento;
		this.dosis = dosis;
		this.frecuencia = frecuencia;
		this.duracionDias = duracionDias;
	}

	public Producto getMedicamento() {
		return medicamento;
	}

	public String getNombreMedicamento() {
		return medicamento.getNombre();
	}

	public String getDosis() {
		return dosis;
	}

	public String getFrecuencia() {
		return frecuencia;
	}

	public int getDuracionDias() {
		return duracionDias;
	}
}
