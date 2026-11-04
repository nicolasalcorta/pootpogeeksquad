package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoriaClinica {
	private String numeroHistoria;
	private LocalDate fechaCreacion;
	private final List<Consulta> consultas;
	private final List<NotaRectificatoria> notas;

	public HistoriaClinica(String numeroHistoria) {
		this.numeroHistoria = numeroHistoria;
		this.fechaCreacion = LocalDate.now();
		this.consultas = new ArrayList<>();
		this.notas = new ArrayList<>();
	}

	public Consulta crearConsulta(String motivo, double pesoDelDia) {
		Consulta nuevaConsulta = new Consulta(motivo, pesoDelDia);
		this.consultas.add(nuevaConsulta);
		return nuevaConsulta;
	}

	public NotaRectificatoria agregarNota(Consulta consulta, Veterinario veterinario, String descripcion) {
		if (consulta == null || !consultas.contains(consulta)) {
			throw new IllegalArgumentException("La consulta no pertenece a esta historia clínica.");
		}
		NotaRectificatoria nota = new NotaRectificatoria(consulta, veterinario, descripcion);
		notas.add(nota);
		return nota;
	}

	public List<Consulta> getConsultas() {
		return Collections.unmodifiableList(consultas);
	}

	public List<NotaRectificatoria> getNotas() {
		return Collections.unmodifiableList(notas);
	}

	public String getNumeroHistoria() {
		return numeroHistoria;
	}

	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}
}
