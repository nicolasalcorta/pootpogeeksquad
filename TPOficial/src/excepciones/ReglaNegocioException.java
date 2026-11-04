package excepciones;

/**
 * Base de las excepciones de negocio. Es checked: quien llama debe tratarla
 * (las vistas la capturan y muestran el mensaje con mostrarError()).
 */
public class ReglaNegocioException extends Exception {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
