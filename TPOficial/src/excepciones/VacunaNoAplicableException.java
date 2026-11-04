package excepciones;

public class VacunaNoAplicableException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public VacunaNoAplicableException(String mensaje) {
        super(mensaje);
    }
}
