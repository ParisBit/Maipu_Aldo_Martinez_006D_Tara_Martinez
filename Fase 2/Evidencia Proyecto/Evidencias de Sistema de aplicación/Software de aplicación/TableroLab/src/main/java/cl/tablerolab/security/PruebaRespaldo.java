
package cl.tablerolab.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class PruebaRespaldo {

    public static void main(String[] args) {

        RespaldoService servicio = new RespaldoService();

        Path respaldoOriginal = Path.of(
                System.getProperty("user.home"),
                "TableroLab_Respaldos",
                "respaldo_20261116_182619.db"
        );

        Path carpetaPrueba = Path.of(
                System.getProperty("user.home"),
                "TableroLab_PruebaRestauracion"
        );

        Path baseDatosPrueba = carpetaPrueba.resolve(
                "tablerolab_prueba.db"
        );

        try {

            System.out.println("INICIANDO PRUEBA DE RESTAURACIÓN");

            // 1. Comprobar que existe el respaldo original.
            if (!Files.isRegularFile(respaldoOriginal)) {
                throw new Exception(
                        "No se encontró el respaldo original."
                );
            }

            // 2. Crear una carpeta de pruebas independiente.
            Files.createDirectories(carpetaPrueba);

            // 3. Copiar el respaldo a la base de datos de prueba.
            Files.copy(
                    respaldoOriginal,
                    baseDatosPrueba,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 4. Indicar a SQLite que utilice exclusivamente
            //    la base de datos de prueba.
            System.setProperty(
                    "tablerolab.db.path",
                    baseDatosPrueba.toString()
            );

            // 5. Introducir una marca que no existe
            //    en el respaldo original.
            try (Connection conexion = DriverManager.getConnection(
                    "jdbc:sqlite:" + baseDatosPrueba);
                 Statement statement = conexion.createStatement()) {

                statement.execute(
                        "CREATE TABLE marca_prueba_restauracion ("
                                + "id INTEGER PRIMARY KEY, "
                                + "mensaje TEXT)"
                );

                statement.execute(
                        "INSERT INTO marca_prueba_restauracion "
                                + "(mensaje) VALUES ('BASE MODIFICADA')"
                );
            }

            System.out.println("Base de prueba modificada correctamente.");

            // 6. Restaurar el respaldo original.
            Path respaldoAutomatico =
                    servicio.restaurarRespaldo(respaldoOriginal);

            System.out.println("Restauración ejecutada correctamente.");

            // 7. Comprobar que la marca desapareció.
            boolean marcaExiste;

            try (Connection conexion = DriverManager.getConnection(
                    "jdbc:sqlite:" + baseDatosPrueba);
                 Statement statement = conexion.createStatement();
                 var resultado = statement.executeQuery(
                         "SELECT COUNT(*) FROM sqlite_master "
                                 + "WHERE type='table' "
                                 + "AND name='marca_prueba_restauracion'")) {

                marcaExiste = resultado.next()
                        && resultado.getInt(1) > 0;
            }

            if (marcaExiste) {
                throw new Exception(
                        "La restauración no reemplazó la base modificada."
                );
            }

            // 8. Verificar integridad de la base restaurada.
            servicio.verificarRespaldo(baseDatosPrueba);

            // 9. Comprobar que existe el respaldo automático.
            if (!Files.isRegularFile(respaldoAutomatico)) {
                throw new Exception(
                        "No se encontró el respaldo automático."
                );
            }

            servicio.verificarRespaldo(respaldoAutomatico);

            System.out.println("La marca de prueba desapareció.");
            System.out.println("La base restaurada es íntegra.");
            System.out.println(
                    "Respaldo automático: " + respaldoAutomatico
            );
            System.out.println("PRUEBA DE RESTAURACIÓN SUPERADA.");

        } catch (Exception e) {

            System.err.println("ERROR EN LA PRUEBA:");
            e.printStackTrace();
        }
    }
}
