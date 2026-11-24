
package cl.tablerolab.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class RestauracionPendienteService {

    private static final Path ARCHIVO_PENDIENTE =
            Path.of(
                    System.getProperty("user.home"),
                    ".tablerolab",
                    "restauracion-pendiente.txt"
            );

    public void guardar(Path respaldo) throws IOException {

        if (respaldo == null) {
            throw new IllegalArgumentException(
                    "No se ha seleccionado un archivo de respaldo."
            );
        }

        Path rutaRespaldo = respaldo.toAbsolutePath().normalize();

        if (!Files.isRegularFile(rutaRespaldo)) {
            throw new IOException(
                    "El archivo de respaldo seleccionado no existe."
            );
        }

        Files.createDirectories(ARCHIVO_PENDIENTE.getParent());

        Files.writeString(
                ARCHIVO_PENDIENTE,
                rutaRespaldo.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    public Path obtenerPendiente() throws IOException {

        if (!Files.isRegularFile(ARCHIVO_PENDIENTE)) {
            return null;
        }

        String contenido = Files.readString(
                ARCHIVO_PENDIENTE,
                StandardCharsets.UTF_8
        ).trim();

        if (contenido.isEmpty()) {
            return null;
        }

        return Path.of(contenido).toAbsolutePath().normalize();
    }

    public void eliminarPendiente() throws IOException {

        Files.deleteIfExists(ARCHIVO_PENDIENTE);
    }
}
