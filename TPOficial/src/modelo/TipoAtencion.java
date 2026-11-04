package modelo;

public enum TipoAtencion {
	CONSULTA_GENERAL(30,5000.0),
	VACUNACION(15,3000.0),
	CIRUGIA(120, 25000.0);
	
	private final int duracionMinutos;
	private final double precioBase;
	
	private TipoAtencion(int duracionMinutos, double precioBase) {
		this.duracionMinutos = duracionMinutos;
		this.precioBase = precioBase;
	}

	//getters
	public int getDuracionMinutos() {
		return duracionMinutos;
	}

	public double getPrecioBase() {
		return precioBase;
	}
	
	
}
