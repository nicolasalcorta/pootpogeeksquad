package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoriaClinica {
	private String numeroHistoria;
	private LocalDate fechaCreacion; 
	private List<Consulta> consultas;
	
	public HistoriaClinica(String numeroHistoria) { 
		this.numeroHistoria = numeroHistoria;
		this.fechaCreacion = LocalDate.now();
		this.consultas = new ArrayList<>(); 
		} 
	
	public Consulta crearConsulta(String motivo, double pesoDelDia) { 
		Consulta nuevaConsulta = new Consulta(motivo, pesoDelDia); 
		this.consultas.add(nuevaConsulta); 
		return nuevaConsulta; 
		}
	
	public void agregarNota(Consulta consulta, Veterinario veterinario, String descripcion) {
		if (consulta != null && consultas.contains(consulta)) {
			consulta.setVeterinario(veterinario); 
			consulta.setObservaciones(descripcion); 
			} 
		} public List<Consulta>getConsultas() { 
			return Collections.unmodifiableList(consultas); 
		} 
		public String getNumeroHistoria() {
			return numeroHistoria; 
		} 
		public LocalDate getFechaCreacion() {
			return fechaCreacion; }
}
