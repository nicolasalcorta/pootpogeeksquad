package modelo;

public abstract class Persona {
	private String id;
	private String nombre;
	private String apellido;
	private String dni;
	private String telefono;

	public Persona(String id, String nombre, String apellido, String dni, String telefono) {
		if(dni == null || dni.trim().isEmpty()) {
			throw new IllegalArgumentException("EL DNI es un campo obligatorio");
		}
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.dni = dni;
		this.telefono = telefono;
	}

	public String getNombreCompleto(){
		return nombre + " " + apellido;
	}

	public String getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public String getDni() {
		return dni;
	}

	public String getTelefono() {
		return telefono;
	}

}
