package excepciones;

public class RolNoAutorizadoException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public RolNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
