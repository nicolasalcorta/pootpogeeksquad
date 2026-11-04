package excepciones;

public class FacturaAnuladaException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public FacturaAnuladaException(String mensaje) {
        super(mensaje);
    }
}
