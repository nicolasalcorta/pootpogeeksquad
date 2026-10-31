package modelo;

public class Tratamiento {
	private Producto medicamento; 
	private String nombreMedicamento; 
	private String dosis; 
	private String frecuencia; 
	private int duracionDosis; 
	
	public Tratamiento(Producto medicamento, String dosis, String frecuencia, int duracionDosis) {
		if (dosis == null || dosis.trim().isEmpty()) { 
			throw new IllegalArgumentException("La dosis no puede estar vacía."); 
			} 
		if (duracionDosis <= 0) { 
			throw new IllegalArgumentException("La duración debe ser mayor a cero días."); 
			} 
		this.medicamento = medicamento; 
		this.nombreMedicamento = (medicamento != null) ? medicamento.getNombre() : "Medicamento no especificado";
		this.dosis = dosis; 
		this.frecuencia = frecuencia; 
		this.duracionDosis = duracionDosis; 
	}
	
	// Getters y Setters
	public Producto getMedicamento() { 
		return medicamento; 
	}
	
	public String getNombreMedicamento() { 
		return nombreMedicamento; 
	} 
	public String getDosis() { 
		return dosis; 
	}
	public String getFrecuencia() { 
		return frecuencia; 
	}
	public int getDuracionDosis() { 
		return duracionDosis; 
	}

	@Override 
	public String toString() {
		return "Tratamiento: " + nombreMedicamento + " | Dosis: " + dosis + " | Frecuencia: " + frecuencia + " | Duración: " + duracionDosis + " días"; 
	}
}
