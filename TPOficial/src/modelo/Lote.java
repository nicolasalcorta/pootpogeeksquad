package modelo;

import java.time.LocalDate;

public class Lote implements Comparable<Lote> {
	private String numeroLote;
	private LocalDate fechaVencimiento;
	private int cantidad;

	public Lote(String numeroLote, LocalDate fechaVencimiento, int cantidad) {
		if (numeroLote == null || numeroLote.trim().isEmpty()) {
			throw new IllegalArgumentException("El número de lote no puede estar vacío.");
		}
		if (fechaVencimiento == null) {
			throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
		}
		if (cantidad <= 0) { throw new IllegalArgumentException("La cantidad inicial del lote debe ser mayor a cero."); }
		this.numeroLote = numeroLote;
		this.fechaVencimiento = fechaVencimiento;
		this.cantidad = cantidad;
	}
	public boolean estaVencido(LocalDate hoy) {
		return hoy.isAfter(fechaVencimiento);
	}
	public void descontar(int cant) {
		if (cant <= 0) {
			throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
		}
		if (cant > this.cantidad) {
			throw new IllegalStateException("Cantidad insuficiente disponible en el lote " + numeroLote);
		}
		this.cantidad -= cant;
	}

	@Override public int compareTo(Lote otro) {
		return this.fechaVencimiento.compareTo(otro.fechaVencimiento);
	}

	public String getNumeroLote() {
		return numeroLote;
	}
	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	@Override public String toString() {
		return "Lote " + numeroLote + " [Vence: " + fechaVencimiento + " | Cantidad: " + cantidad + "]"; }
}
