
package cl.tablerolab.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ConexionSQLiteTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void debeConectarseCorrectamenteABaseDeDatosTemporal()
            throws SQLException {

        Path rutaBaseDatos = carpetaTemporal.resolve("prueba.db");

        try (Connection conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + rutaBaseDatos
        )) {

            assertNotNull(conexion);
            assertFalse(conexion.isClosed());

            System.out.println(
                    "Conexión SQLite temporal establecida correctamente."
            );
        }
    }
}
