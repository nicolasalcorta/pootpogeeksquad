package modelo;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dueño extends Persona {
	private String email;
	private boolean tienePlanSalud;
	private List<Mascota> mascotas;
	
	public Dueño(String id, String nombre, String apellido, String dni, String telefono) {
		super(id, nombre, apellido, dni, telefono);
		this.email = email;
		this.tienePlanSalud = tienePlanSalud;
		this.mascotas = new ArrayList<>();
	}
	
	public void agregarMascota(Mascota mascotas) {
		if(mascota != null && !mascotas.contians(mascota)) {
			mascotas.add(mascota);
		}
	}
	
	public double getPorcentajeDescuento(){
		return tienePlanSalud ? 0.15 : 0.0;	
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public boolean isTienePlanSalud(){
		return tienePlanSalud;
	}
	
	public void setTienePlanSalud(boolean tienePlanSalud){
		this.tienePlanSalud = tienePlanSalud;
	}
	
	public List<Macota> getMascotas(){
		return Collections.unmodifiableList(mascotas);
	}
}
