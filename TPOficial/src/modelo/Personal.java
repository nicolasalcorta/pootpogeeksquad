package modelo;

public class Personal extends Persona{
	private final String legajo;
	private final RolPersonal rol;
	public Personal(String id, String nombre, String apellido, String dni, String telefono, String legajo, RolPersonal rol) {
		super(id, nombre, apellido, dni, telefono);
		if (legajo == null || legajo.trim().isEmpty()) {
			throw new IllegalArgumentException("El número de legajo es obligatorio.");
		}
		if (rol == null) { throw new IllegalArgumentException("El rol del personal es obligatorio.");
		}
		// El rol VETERINARIO queda reservado a la subclase Veterinario.
		if (rol == RolPersonal.VETERINARIO && !(this instanceof Veterinario)) {
			throw new IllegalArgumentException("Para registrar un veterinario hay que usar la clase Veterinario.");
		}
		this.legajo = legajo;
		this.rol = rol;
	}
	public String getLegajo() {
		return legajo; }
	public RolPersonal getRol() {
		return rol; }
	public boolean tieneRol(RolPersonal rol) {
		return this.rol == rol;
	}

	@Override public String toString() {
		return "Personal: " + getNombreCompleto() + " [Legajo: " + legajo + " | Rol: " + rol + "]"; }
}
