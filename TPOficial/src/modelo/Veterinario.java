package modelo;

import java.time.LocalTime;

public class Veterinario extends Persona{
	private String matricula;
	private String especialidad;
	private LocalTime horaInicioAtencion;
	private LocalTime horaFinAtencion;
	
	public Veterinario(String id, String nombre, String apellido, String dni, String telefono, LocalTime inicio, LocalTime fin) {
		super(id, nombre, apellido, dni, telefono);
		this.matricula = matricula;
		this.especialidad = especialidad;
		this.horaInicioAtencion = inicio;
		this.horaFinAtencion = fin;
	}
	
	public boolean atiendeEnHorario(LocalTime hora) {
		return(hora.equals(horaInicioAtencion) || hora.isAfter(horaInicioAtencion) && hora.isBefore(horaFinAtencion));
	}
	
	public String getMatricula(){
		return matricula;
	}
	
	public String getEspecialidad(){
		return especialidad;
	}
	
	public LocalTime getInicionAtencion(){
		return horaInicioAtencion;
	}
	
	public LocalTime getHoraFinAtencion(){
		return horaFinAtencion;
	}
}
