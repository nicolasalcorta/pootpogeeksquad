package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Duenio extends Persona {
	public static final int AUSENCIAS_PARA_SENIA = 3;
	private String mail;
	private boolean tienePlanSalud;
	private int ausenciasConsecutivas;
	private List<Mascota> mascotas;

	public Duenio(String id, String nombre, String apellido, String dni, String telefono) {
		super(id, nombre, apellido, dni, telefono);
		this.tienePlanSalud = false;
		this.ausenciasConsecutivas = 0;
		this.mascotas = new ArrayList<>();
	}

	public void agregarMascota(Mascota mascota) {
		if (mascota != null && !mascotas.contains(mascota)) {
			mascotas.add(mascota);
		}
	}

	public void quitarMascota(Mascota mascota) {
		mascotas.remove(mascota);
	}

	public void registrarAusencia() {
		ausenciasConsecutivas++;
	}

	public void reiniciarAusencias() {
		ausenciasConsecutivas = 0;
	}

	public boolean debePagarSenia() {
		return ausenciasConsecutivas >= AUSENCIAS_PARA_SENIA;
	}

	public int getAusenciasConsecutivas() {
		return ausenciasConsecutivas;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public boolean isTienePlanSalud() {
		return tienePlanSalud;
	}

	public void setTienePlanSalud(boolean tienePlanSalud) {
		this.tienePlanSalud = tienePlanSalud;
	}

	public List<Mascota> getMascotas() {
		return Collections.unmodifiableList(mascotas);
	}
}
