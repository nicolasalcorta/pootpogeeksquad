package modelo;

import java.time.LocalDateTime;

public class Turno {
	private LocalDateTime fechaHora;
	private TipoAtencion tipo; 
	private EstadoTurno estado; 
	private boolean esUrgencia;
	private Mascota mascota; 
	private Veterinario veterinario;
	
	public Turno(LocalDateTime fechaHora, TipoAtencion tipo, Mascota mascota, Veterinario veterinario, boolean esUrgencia) { 
		if (fechaHora == null) { 
			throw new IllegalArgumentException("La fecha y hora del turno son obligatorias."); 
			} 
		if (tipo == null) {
			throw new IllegalArgumentException("El tipo de atención es obligatorio.");
			} 
		if (mascota == null) {
			throw new IllegalArgumentException("La mascota es obligatoria."); 
			}
		if (veterinario == null) {
			throw new IllegalArgumentException("El veterinario es obligatorio.");
			} 
		this.fechaHora = fechaHora;
		this.tipo = tipo; 
		this.mascota = mascota; 
		this.veterinario = veterinario; 
		this.esUrgencia = esUrgencia; 
		this.estado = EstadoTurno.PROGRAMADO; 
		} 
	
	public void reprogramar(LocalDateTime nuevaFecha) {
		if (nuevaFecha == null) { 
			throw new IllegalArgumentException("La nueva fecha no puede ser nula."); 
			}
		this.fechaHora = nuevaFecha; 
		}
	public void cancelar() { 
		this.estado = EstadoTurno.CANCELADO;
		} 
	
	public boolean seSuperponeCon(Turno otro) { 
		if (otro == null || otro.getEstado() == EstadoTurno.CANCELADO) { 
			return false; 
			} 
		LocalDateTime inicioA = this.fechaHora;
		LocalDateTime finA = selfFin(this.fechaHora, this.tipo);
		
		LocalDateTime inicioB = otro.getFechaHora(); 
		LocalDateTime finB = selfFin(otro.getFechaHora(), otro.getTipo());
		return inicioA.isBefore(finB) && finA.isAfter(inicioB);
		}
	
	private LocalDateTime selfFin(LocalDateTime inicio, TipoAtencion t) {
		return inicio.plusMinutes(t.getDuracionMinutos()); 
		} 
	
	public LocalDateTime getFechaHora() { 
		return fechaHora; 
	}
	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora; 
	} 
	public TipoAtencion getTipo() {
		return tipo; 
	} public void setTipo(TipoAtencion tipo) {
		this.tipo = tipo; 
	} 
	public EstadoTurno getEstado() { 
		return estado; 
	} 
	public void setEstado(EstadoTurno estado) {
		this.estado = estado;
	} 
	public boolean isEsUrgencia() {
		return esUrgencia; 
	} public void setEsUrgencia(boolean esUrgencia) {
		this.esUrgencia = esUrgencia;
	} 
	public Mascota getMascota() { 
		return mascota; 
	} 
	public void setMascota(Mascota mascota) {
		this.mascota = mascota; 
	} 
	public Veterinario getVeterinario() { 
		return veterinario; 
	} 
	public void setVeterinario(Veterinario veterinario) {
		this.veterinario = veterinario; 
	} 
	
	@Override public String toString() {
		return "Turno [" + tipo + "] - Fecha: " + fechaHora + " | Mascota: " + (mascota != null ? mascota.getNombre() : "N/A") + " | Vet: " + (veterinario != null ? veterinario.getNombreCompleto() : "N/A") + " | Estado: " + estado + (esUrgencia ? " (URGENCIA)" : ""); }
}
