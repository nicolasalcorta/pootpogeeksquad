package vista;

import controlador.GestorConsultas;
import controlador.GestorFacturacion;
import controlador.GestorPacientes;
import controlador.GestorPersonal;
import controlador.GestorStock;
import controlador.GestorTurnos;
import controlador.GestorVacunas;

/**
 * Agrupa los gestores (controladores) que crea Principal, para que lleguen a las vistas.
 * VistaPrincipal le pide a este objeto solo los gestores que cada pestaña necesita.
 */
public class ContextoAplicacion {
    private final GestorTurnos gestorTurnos;
    private final GestorPacientes gestorPacientes;
    private final GestorConsultas gestorConsultas;
    private final GestorVacunas gestorVacunas;
    private final GestorFacturacion gestorFacturacion;
    private final GestorStock gestorStock;
    private final GestorPersonal gestorPersonal;

    public ContextoAplicacion(GestorTurnos gestorTurnos, GestorPacientes gestorPacientes, GestorConsultas gestorConsultas,
                              GestorVacunas gestorVacunas, GestorFacturacion gestorFacturacion, GestorStock gestorStock,
                              GestorPersonal gestorPersonal) {
        if (gestorTurnos == null || gestorPacientes == null || gestorConsultas == null || gestorVacunas == null
                || gestorFacturacion == null || gestorStock == null || gestorPersonal == null) {
            throw new IllegalArgumentException("Todos los gestores son obligatorios.");
        }
        this.gestorTurnos = gestorTurnos;
        this.gestorPacientes = gestorPacientes;
        this.gestorConsultas = gestorConsultas;
        this.gestorVacunas = gestorVacunas;
        this.gestorFacturacion = gestorFacturacion;
        this.gestorStock = gestorStock;
        this.gestorPersonal = gestorPersonal;
    }

    public GestorTurnos getGestorTurnos() {
        return gestorTurnos;
    }

    public GestorPacientes getGestorPacientes() {
        return gestorPacientes;
    }

    public GestorConsultas getGestorConsultas() {
        return gestorConsultas;
    }

    public GestorVacunas getGestorVacunas() {
        return gestorVacunas;
    }

    public GestorFacturacion getGestorFacturacion() {
        return gestorFacturacion;
    }

    public GestorStock getGestorStock() {
        return gestorStock;
    }

    public GestorPersonal getGestorPersonal() {
        return gestorPersonal;
    }
}
