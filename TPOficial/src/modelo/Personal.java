package modelo;

public class Personal extends Persona{
	private String legajo; 
	private RolPersonal rol;
	public Personal(String id, String nombre, String apellido, String dni, String telefono, String legajo, RolPersonal rol) {
		super(id, nombre, apellido, dni, telefono);
		if (legajo == null || legajo.trim().isEmpty()) { 
			throw new IllegalArgumentException("El número de legajo es obligatorio."); 
			}
		if (rol == null) { throw new IllegalArgumentException("El rol del personal es obligatorio."); 
		}
		this.legajo = legajo; 
		this.rol = rol; 
		} 
	public String getLegajo() { 
		return legajo; }
	public RolPersonal getRol() {
		return rol; } 
	public void setRol(RolPersonal rol) {
		this.rol = rol;
	} 
	
	@Override public String toString() { 
		return "Personal: " + getNombreCompleto() + " [Legajo: " + legajo + " | Rol: " + rol + "]"; }
}
