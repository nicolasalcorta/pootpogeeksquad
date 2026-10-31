package modelo;

import java.util.ArrayList;
import java.util.List;

public class Vacuna {
	private String idVacuna; 
	private String nombre; 
	private int edadMinimaMeses;
	private int intervaloRefuerzoMeses; 
	private List<Especie> especiesPermitidas;
	
	public Vacuna(String idVacuna, String nombre, int edadMinimaMeses, int intervaloRefuerzoMeses) { 
		this.idVacuna = idVacuna;
		this.nombre = nombre;
		this.edadMinimaMeses = edadMinimaMeses; 
		this.intervaloRefuerzoMeses = intervaloRefuerzoMeses; 
		this.especiesPermitidas = new ArrayList<>(); } 
	
	public void agregarEspecie(Especie especie) {
		if (especie != null && !especiesPermitidas.contains(especie)) { 
			especiesPermitidas.add(especie); 
			} 
		}
	
	public boolean esAptaParaEspecie(Especie especie) { 
		return especiesPermitidas.contains(especie); }

	
	// Getters 
	public String getIdVacuna() { 
		return idVacuna; } 
	
	public String getNombre() { 
		return nombre; }
	
	public int getEdadMinimaMeses() { 
		return edadMinimaMeses; } 
	
	public int getIntervaloRefuerzoMeses() { 
		return intervaloRefuerzoMeses; }
	
	public List<Especie> getEspeciesPermitidas() {
		return especiesPermitidas; }
}
