package modelo;

public class Tratamiento {
	private final Producto medicamento;
	private final int cantidad;
	private final String dosis;
	private final String frecuencia;
	private final int duracionDias;

	public Tratamiento(Producto medicamento, int cantidad, String dosis, String frecuencia, int duracionDias) {
		if (medicamento == null) {
			throw new IllegalArgumentException("El medicamento es obligatorio.");
		}
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad de unidades debe ser mayor a cero.");
		}
		if (dosis == null || dosis.trim().isEmpty()) {
			throw new IllegalArgumentException("La dosis no puede estar vacía.");
		}
		if (duracionDias <= 0) {
			throw new IllegalArgumentException("La duración debe ser mayor a cero días.");
		}
		this.medicamento = medicamento;
		this.cantidad = cantidad;
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

	public int getCantidad() {
		return cantidad;
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
