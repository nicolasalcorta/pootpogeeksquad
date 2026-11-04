package excepciones;

public class TurnoNoDisponibleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TurnoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
