package excepciones;

public class PesoNoRegistradoException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public PesoNoRegistradoException(String mensaje) {
        super(mensaje);
    }
}
