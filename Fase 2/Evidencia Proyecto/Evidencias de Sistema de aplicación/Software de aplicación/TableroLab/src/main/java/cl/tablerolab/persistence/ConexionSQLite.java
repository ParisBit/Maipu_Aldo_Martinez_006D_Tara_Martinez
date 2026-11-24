
package cl.tablerolab.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static final String PROPIEDAD_RUTA =
            "tablerolab.db.path";

    private ConexionSQLite() {
    }

    public static Connection obtenerConexion() throws SQLException {

        Path rutaBaseDatos = obtenerRutaBaseDatos();

        Path directorio = rutaBaseDatos.getParent();

        if (directorio != null) {
            try {
                Files.createDirectories(directorio);

            } catch (IOException e) {

                throw new SQLException(
                        "No se pudo crear la carpeta de la base de datos.",
                        e
                );
            }
        }

        Connection conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + rutaBaseDatos
        );

        try (var statement = conexion.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");

        } catch (SQLException e) {
            conexion.close();
            throw e;
        }

        return conexion;
    }

    public static Path obtenerRutaBaseDatos() {

        String rutaPersonalizada =
                System.getProperty(PROPIEDAD_RUTA);

        if (rutaPersonalizada != null
                && !rutaPersonalizada.isBlank()) {

            return Path.of(rutaPersonalizada)
                    .toAbsolutePath()
                    .normalize();
        }

        return Path.of(
                System.getProperty("user.home"),
                ".tablerolab",
                "tablerolab.db"
        );
    }

    public static void probarConexion() {

        try (Connection conexion = obtenerConexion()) {

            System.out.println(
                    "SQLite conectado correctamente."
            );

            System.out.println(
                    "Base de datos: " + obtenerRutaBaseDatos()
            );

        } catch (SQLException e) {

            System.err.println(
                    "Error al conectar con SQLite:"
            );

            e.printStackTrace();
        }
    }
}
