package vista;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import controlador.GestorConsultas;
import controlador.GestorFacturacion;
import controlador.GestorPacientes;
import controlador.GestorPersonal;
import controlador.GestorStock;
import controlador.GestorTurnos;
import controlador.GestorVacunas;
import modelo.Especie;
import modelo.HorarioAtencion;
import modelo.Lote;
import modelo.Personal;
import modelo.Producto;
import modelo.RolPersonal;
import modelo.Vacuna;
import modelo.Veterinario;
import notificacion.Notificador;
import notificacion.NotificadorVentana;
import repositorio.RepositorioDueniosArchivo;
import repositorio.RepositorioFacturasArchivo;
import repositorio.RepositorioMascotasArchivo;
import repositorio.RepositorioPersonalArchivo;
import repositorio.RepositorioProductosArchivo;
import repositorio.RepositorioTurnosArchivo;
import repositorio.RepositorioVacunasArchivo;

/**
 * Punto de arranque. Crea los repositorios de archivo, el notificador y los siete gestores
 * (inyección por constructor), los agrupa en ContextoAplicacion y abre la selección de usuario.
 */
public class Principal {
    /** Carpeta donde se guardan los archivos .txt (relativa a donde se ejecuta el programa). */
    private static final String CARPETA_DATOS = "datos";

    public static void main(String[] args) {
        // Buena práctica de Swing: crear la interfaz en el hilo de eventos (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            try {
                ContextoAplicacion contexto = crearContexto();
                new VistaSeleccionUsuario(contexto).setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "No se pudieron cargar los datos de la carpeta \"" + CARPETA_DATOS
                        + "\":\n" + e.getMessage(), "Error al iniciar", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static ContextoAplicacion crearContexto() {
        // El orden importa: cada repositorio busca en los anteriores los objetos que referencia.
        RepositorioPersonalArchivo repoPersonal = new RepositorioPersonalArchivo(ruta("personal"));
        RepositorioDueniosArchivo repoDuenios = new RepositorioDueniosArchivo(ruta("duenios"));
        RepositorioProductosArchivo repoProductos = new RepositorioProductosArchivo(ruta("productos"));
        RepositorioVacunasArchivo repoVacunas = new RepositorioVacunasArchivo(ruta("vacunas"), repoProductos);
        RepositorioMascotasArchivo repoMascotas = new RepositorioMascotasArchivo(ruta("mascotas"), repoDuenios);
        RepositorioTurnosArchivo repoTurnos = new RepositorioTurnosArchivo(ruta("turnos"), repoMascotas, repoPersonal);
        repoMascotas.cargarHistoriasClinicas(ruta("historias"), repoTurnos, repoPersonal, repoProductos, repoVacunas);
        RepositorioFacturasArchivo repoFacturas = new RepositorioFacturasArchivo(ruta("facturas"), repoDuenios, repoMascotas);

        Notificador notificador = new NotificadorVentana();
        GestorPersonal gestorPersonal = new GestorPersonal(repoPersonal);
        GestorPacientes gestorPacientes = new GestorPacientes(repoDuenios, repoMascotas);
        GestorStock gestorStock = new GestorStock(repoProductos);
        GestorFacturacion gestorFacturacion = new GestorFacturacion(repoFacturas);
        GestorTurnos gestorTurnos = new GestorTurnos(repoTurnos, repoPersonal, notificador, gestorFacturacion);
        GestorConsultas gestorConsultas = new GestorConsultas(repoMascotas, repoTurnos, gestorStock);
        GestorVacunas gestorVacunas = new GestorVacunas(repoMascotas, repoVacunas, notificador);

        if (gestorPersonal.listarPersonal().isEmpty()) {
            cargarDatosDePrueba(gestorPersonal, repoProductos, repoVacunas);
        }

        return new ContextoAplicacion(gestorTurnos, gestorPacientes, gestorConsultas, gestorVacunas,
                gestorFacturacion, gestorStock, gestorPersonal);
    }

    private static String ruta(String nombre) {
        return CARPETA_DATOS + "/" + nombre + ".txt";
    }

    /**
     * Primera ejecución (sin archivos): carga personal, productos y una vacuna de prueba
     * para poder usar el sistema. Después todo se guarda y se lee de los .txt.
     */
    private static void cargarDatosDePrueba(GestorPersonal gestorPersonal, RepositorioProductosArchivo repoProductos,
                                            RepositorioVacunasArchivo repoVacunas) {
        gestorPersonal.registrarPersonal(
                new Personal("P1", "Laura", "Gómez", "30111222", "1155550001", "ADM-01", RolPersonal.ADMINISTRADOR));
        gestorPersonal.registrarPersonal(
                new Personal("P2", "Martín", "Ruiz", "30222333", "1155550002", "REC-01", RolPersonal.RECEPCIONISTA));

        Veterinario vetTurnoManiana = new Veterinario("P3", "Sofía", "Pérez", "30333444", "1155550003", "VET-01",
                "MP-1001", "Clínica general");
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            vetTurnoManiana.agregarHorario(new HorarioAtencion(dia, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        }
        gestorPersonal.registrarPersonal(vetTurnoManiana);

        Veterinario vetGuardia = new Veterinario("P4", "Diego", "Fernández", "30444555", "1155550004", "VET-02",
                "MP-1002", "Cirugía");
        vetGuardia.setDeGuardia(true);
        for (DayOfWeek dia : DayOfWeek.values()) {
            vetGuardia.agregarHorario(new HorarioAtencion(dia, LocalTime.of(0, 0), LocalTime.of(23, 59)));
        }
        gestorPersonal.registrarPersonal(vetGuardia);

        Producto antibiotico = new Producto("MED-001", "Amoxicilina 500 mg", 10, 1200);
        antibiotico.agregarLote(new Lote("AMX-2401", LocalDate.now().plusMonths(8), 40));
        repoProductos.guardar(antibiotico);

        Producto antirrabica = new Producto("VAC-001", "Vacuna antirrábica (dosis)", 5, 8500);
        antirrabica.agregarLote(new Lote("RAB-2401", LocalDate.now().plusMonths(12), 20));
        repoProductos.guardar(antirrabica);

        Vacuna vacunaAntirrabica = new Vacuna("V-01", "Antirrábica", 3, antirrabica);
        vacunaAntirrabica.agregarEspecie(Especie.PERRO, 12);
        vacunaAntirrabica.agregarEspecie(Especie.GATO, 12);
        repoVacunas.guardar(vacunaAntirrabica);
    }
}
