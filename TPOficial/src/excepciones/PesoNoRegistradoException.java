package excepciones;

public class PesoNoRegistradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PesoNoRegistradoException(String mensaje) {
        super(mensaje);
    }
}
