package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoriaClinica {
	private String numeroHistoria;
	private LocalDate fechaCreacion;
	
    public HistoriaClinica(String numeroHistoria) {
        this.numeroHistoria = numeroHistoria;
        this.fechaCreacion = LocalDate.now();
    }
    
    public void agregarNota(Consulta consulta, Veterinario veterinario, String descripcion) { 
    	if (consulta != null && consultas.contains(consulta)) { 
    		consulta.setVeterinario(veterinario); consulta.setObservaciones(descripcion); 
    		}
    	}
    
    public List<Consulta> getConsulta(){
    	return Collections.unmodifiableList(consultas);
    }
    
   //Getters 
    public String getNumeroHistoria() { 
    	return numeroHistoria; 
    }
    public LocalDate getFechaCreacion() {
    	return fechaCreacion; 
    }
}
