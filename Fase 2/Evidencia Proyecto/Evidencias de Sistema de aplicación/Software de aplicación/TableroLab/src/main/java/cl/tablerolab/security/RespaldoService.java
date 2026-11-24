
package cl.tablerolab.security;

import cl.tablerolab.persistence.ConexionSQLite;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RespaldoService {

    private static final List<String> TABLAS_REQUERIDAS = List.of(
            "materiales",
            "formatos_tablero",
            "proyectos",
            "piezas",
            "retazos",
            "cotizaciones"
    );

    /**
     * Crea una copia completa de la base de datos SQLite.
     */
    public void crearRespaldo(Path destino) throws SQLException {

        if (destino == null) {
            throw new SQLException(
                    "No se indicó dónde guardar el respaldo."
            );
        }

        Path rutaDestino = destino.toAbsolutePath().normalize();
        Path rutaOrigen = ConexionSQLite.obtenerRutaBaseDatos()
                .toAbsolutePath().normalize();

        if (rutaDestino.equals(rutaOrigen)) {
            throw new SQLException(
                    "El respaldo no puede sobrescribir la base original."
            );
        }

        try {
            Path carpeta = rutaDestino.getParent();

            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }

            if (Files.exists(rutaDestino)) {
                throw new SQLException(
                        "Ya existe un archivo en la ubicación seleccionada."
                );
            }

        } catch (IOException e) {
            throw new SQLException(
                    "No se pudo preparar la ubicación del respaldo.", e
            );
        }

        String rutaSQL = rutaDestino.toString().replace("'", "''");

        try (Connection conexion = ConexionSQLite.obtenerConexion();
             Statement statement = conexion.createStatement()) {

            statement.execute("VACUUM INTO '" + rutaSQL + "'");

        }
    }

    /**
     * Comprueba la integridad y estructura de una copia.
     */
    public void verificarRespaldo(Path archivo) throws SQLException {

        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new SQLException(
                    "El archivo de respaldo no existe."
            );
        }

        String url = "jdbc:sqlite:"
                + archivo.toAbsolutePath().normalize();

        try (Connection conexion = DriverManager.getConnection(url);
             Statement statement = conexion.createStatement()) {

            try (ResultSet resultado =
                         statement.executeQuery("PRAGMA integrity_check")) {

                if (!resultado.next()
                        || !"ok".equalsIgnoreCase(resultado.getString(1))) {

                    throw new SQLException(
                            "El respaldo presenta errores de integridad."
                    );
                }
            }

            for (String tabla : TABLAS_REQUERIDAS) {

                try (ResultSet resultado = statement.executeQuery(
                        "SELECT COUNT(*) FROM sqlite_master "
                                + "WHERE type = 'table' AND name = '"
                                + tabla + "'")) {

                    if (!resultado.next() || resultado.getInt(1) != 1) {
                        throw new SQLException(
                                "El archivo no corresponde a una base "
                                        + "de datos válida de TableroLab. "
                                        + "Falta la tabla: " + tabla
                        );
                    }
                }
            }
        }
    }

    /**
     * Restaura una copia de seguridad.
     *
     * IMPORTANTE:
     * TableroLab debe estar completamente cerrado antes de ejecutar
     * este método.
     */
    public Path restaurarRespaldo(Path archivo) throws SQLException {

        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new SQLException(
                    "No se seleccionó un respaldo válido."
            );
        }

        Path rutaOrigen = archivo.toAbsolutePath().normalize();

        Path rutaBaseDatos = ConexionSQLite.obtenerRutaBaseDatos()
                .toAbsolutePath().normalize();

        if (rutaOrigen.equals(rutaBaseDatos)) {
            throw new SQLException(
                    "No puedes restaurar la base de datos sobre sí misma."
            );
        }

        // 1. Comprobar el respaldo antes de modificar nada.
        verificarRespaldo(rutaOrigen);

        Path carpeta = rutaBaseDatos.getParent();

        if (carpeta == null) {
            throw new SQLException(
                    "No se pudo determinar la carpeta de la base de datos."
            );
        }

        Path archivoTemporal = null;

        try {

            Files.createDirectories(carpeta);

            // 2. Preparar una copia temporal del respaldo.
            archivoTemporal = Files.createTempFile(
                    carpeta,
                    "tablerolab_restauracion_",
                    ".db"
            );

            Files.copy(
                    rutaOrigen,
                    archivoTemporal,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 3. Verificar nuevamente la copia temporal.
            verificarRespaldo(archivoTemporal);

            // 4. Crear una copia de seguridad automática
            //    de la base de datos actual.
            String fecha = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );

            Path respaldoAnterior = carpeta.resolve(
                    "tablerolab_antes_de_restaurar_" + fecha + ".db"
            );

            crearRespaldo(respaldoAnterior);

            // 5. Reemplazar la base de datos mediante
            //    una operación atómica.
            try {
                Files.move(
                        archivoTemporal,
                        rutaBaseDatos,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

                archivoTemporal = null;

            } catch (AtomicMoveNotSupportedException e) {

                throw new SQLException(
                        "El sistema no permite reemplazar la base de datos "
                                + "de forma atómica. La base original "
                                + "se ha conservado y el respaldo "
                                + "automático permanece disponible.",
                        e
                );
            }

            return respaldoAnterior;

        } catch (IOException e) {

            throw new SQLException(
                    "No se pudo completar la restauración. "
                            + "Comprueba los permisos y el espacio disponible.",
                    e
            );

        } finally {

            if (archivoTemporal != null) {
                try {
                    Files.deleteIfExists(archivoTemporal);
                } catch (IOException ignored) {
                    // El archivo temporal podrá eliminarse manualmente.
                }
            }
        }
    }
}
