package repositorio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Base de los repositorios que guardan en archivos .txt.
 *
 * Cada registro ocupa una línea y sus campos van separados por ';'.
 * Si un campo contiene ';', '\' o un salto de línea, se escapa con '\'
 * para que la línea se pueda volver a leer sin errores.
 *
 * Las subclases mantienen los objetos en memoria, los cargan del archivo
 * al construirse y reescriben el archivo completo en cada guardar().
 */
public abstract class ArchivoTexto {
    private static final char SEPARADOR = ';';
    private static final char ESCAPE = '\\';

    private final Path ruta;

    protected ArchivoTexto(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        }
        this.ruta = Paths.get(rutaArchivo);
    }

    /** Devuelve las líneas no vacías del archivo, o una lista vacía si todavía no existe. */
    protected List<String> leerLineas() {
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            List<String> lineas = new ArrayList<>();
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
            return lineas;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo " + ruta, e);
        }
    }

    /** Reemplaza el contenido del archivo por las líneas dadas. */
    protected void escribirLineas(List<String> lineas) {
        try {
            Path carpeta = ruta.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir el archivo " + ruta, e);
        }
    }

    /** Une los campos en una línea, escapando los caracteres especiales. */
    protected static String unir(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            String valor = campos[i] == null ? "" : String.valueOf(campos[i]);
            for (char c : valor.toCharArray()) {
                if (c == SEPARADOR || c == ESCAPE) {
                    sb.append(ESCAPE).append(c);
                } else if (c == '\n') {
                    sb.append(ESCAPE).append('n');
                } else if (c != '\r') {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /** Separa una línea en sus campos, deshaciendo el escape. */
    protected static String[] separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == ESCAPE && i + 1 < linea.length()) {
                char siguiente = linea.charAt(++i);
                actual.append(siguiente == 'n' ? '\n' : siguiente);
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }

    /** Convierte un campo vacío en null (para valores opcionales). */
    protected static String vacioANull(String valor) {
        return (valor == null || valor.isEmpty()) ? null : valor;
    }

    protected String getRuta() {
        return ruta.toString();
    }
}
