
package cl.tablerolab.persistence;

import cl.tablerolab.model.EstadoRetazo;
import cl.tablerolab.model.Retazo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetazoDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaOriginal;
    private static int proyectoId;

    @BeforeAll
    static void prepararBaseDatos() throws SQLException {

        rutaOriginal = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("retazos-test.db").toString()
        );

        InicializadorBD.inicializar();

        MaterialDAO materialDAO = new MaterialDAO();
        FormatoTableroDAO formatoDAO = new FormatoTableroDAO();
        ProyectoDAO proyectoDAO = new ProyectoDAO();

        materialDAO.guardar(
                "MDF-RET-TEST",
                "MDF prueba retazos",
                "Material para pruebas de retazos"
        );

        int materialId =
                materialDAO.obtenerIdPorCodigo("MDF-RET-TEST");

        formatoDAO.guardar(
                materialId,
                "MDF-RET-122-244",
                122,
                244,
                15
        );

        int formatoId =
                formatoDAO.obtenerIdPorCodigo("MDF-RET-122-244");

        proyectoDAO.guardar(
                "Proyecto prueba retazos",
                materialId,
                formatoId,
                3.0
        );

        proyectoId =
                proyectoDAO.obtenerIdPorNombre("Proyecto prueba retazos");
    }

    @AfterAll
    static void restaurarPropiedad() {

        if (rutaOriginal == null) {
            System.clearProperty("tablerolab.db.path");
        } else {
            System.setProperty("tablerolab.db.path", rutaOriginal);
        }
    }

    @Test
    void debeGuardarYRecuperarRetazoCompleto() throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        Retazo retazo = new Retazo(
                10,
                20,
                80,
                45,
                15
        );

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                dao.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        List.of(retazo)
                );

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }

        List<Retazo> recuperados =
                dao.listarObjetosPorProyecto(proyectoId);

        assertEquals(1, recuperados.size());

        Retazo recuperado = recuperados.get(0);

        assertEquals(10, recuperado.getX(), 0.000001);
        assertEquals(20, recuperado.getY(), 0.000001);
        assertEquals(80, recuperado.getAncho(), 0.000001);
        assertEquals(45, recuperado.getLargo(), 0.000001);
        assertEquals(15, recuperado.getEspesor(), 0.000001);

        assertEquals(
                EstadoRetazo.APROVECHABLE,
                recuperado.getEstado()
        );
    }

    @Test
    void debeReemplazarRetazosSinGenerarDuplicados() throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {

                dao.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        List.of(
                                new Retazo(0, 0, 80, 45, 15),
                                new Retazo(80, 0, 30, 20, 15)
                        )
                );

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }

        List<Retazo> primeraConsulta =
                dao.listarObjetosPorProyecto(proyectoId);

        assertEquals(2, primeraConsulta.size());

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {

                dao.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        List.of(
                                new Retazo(5, 5, 70, 40, 15)
                        )
                );

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }

        List<Retazo> segundaConsulta =
                dao.listarObjetosPorProyecto(proyectoId);

        assertEquals(1, segundaConsulta.size());

        assertEquals(
                70,
                segundaConsulta.get(0).getAncho(),
                0.000001
        );
    }

    @Test
    void debePermitirEliminarTodosLosRetazosMedianteListaVacia()
            throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {

                dao.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        List.of()
                );

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }

        assertTrue(
                dao.listarObjetosPorProyecto(proyectoId).isEmpty()
        );
    }

    @Test
    void noDebeAceptarListaNula() throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            assertThrows(
                    SQLException.class,
                    () -> dao.reemplazarPorProyecto(
                            conexion,
                            proyectoId,
                            null
                    )
            );
        }
    }

    @Test
    void noDebeAceptarRetazoNulo() throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            assertThrows(
                    SQLException.class,
                    () -> dao.reemplazarPorProyecto(
                            conexion,
                            proyectoId,
                            java.util.Arrays.asList((Retazo) null)
                    )
            );
        }
    }


    @Test
    void debeGuardarYRecuperarRetazosPorNumeroDeTablero() throws SQLException {

        RetazoDAO dao = new RetazoDAO();

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                dao.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        List.of(
                                new Retazo(10, 20, 80, 45, 15, 1),
                                new Retazo(5, 10, 60, 30, 15, 2),
                                new Retazo(0, 0, 40, 25, 15, 2)
                        )
                );

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }

        List<Retazo> tablero1 =
                dao.listarObjetosPorProyectoYTablero(proyectoId, 1);

        List<Retazo> tablero2 =
                dao.listarObjetosPorProyectoYTablero(proyectoId, 2);

        assertEquals(1, tablero1.size());
        assertEquals(2, tablero2.size());

        assertEquals(1, tablero1.get(0).getNumeroTablero());
        assertEquals(2, tablero2.get(0).getNumeroTablero());
        assertEquals(2, tablero2.get(1).getNumeroTablero());

        assertEquals(80, tablero1.get(0).getAncho(), 0.000001);
        assertEquals(60, tablero2.get(0).getAncho(), 0.000001);
        assertEquals(40, tablero2.get(1).getAncho(), 0.000001);
    }


}
