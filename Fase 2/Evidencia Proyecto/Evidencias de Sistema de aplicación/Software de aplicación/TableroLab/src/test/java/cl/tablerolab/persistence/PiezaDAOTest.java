
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import cl.tablerolab.model.Pieza;

import org.junit.jupiter.api.BeforeEach;

class PiezaDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;
    private static int proyectoId;
    private static int materialId;
    private static int formatoId;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("piezas_test.db").toString()
        );

        InicializadorBD.inicializar();

        materialId = new MaterialDAO().guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material para piezas"
        );

        formatoId = new FormatoTableroDAO().guardar(
                materialId,
                "MDF-122-244-TEST",
                122.0,
                244.0,
                15.0
        );

    }

    @BeforeEach
    void crearProyectoParaCadaPrueba() throws SQLException {

        proyectoId = new ProyectoDAO().guardar(
                "Proyecto de prueba individual",
                materialId,
                formatoId,
                3.0
        );
    }

    @AfterAll
    static void restaurarConfiguracion() {

        if (rutaAnterior == null) {
            System.clearProperty("tablerolab.db.path");
        } else {
            System.setProperty("tablerolab.db.path", rutaAnterior);
        }
    }

    @Test
    void debeGuardarYConsultarPieza() throws SQLException {

        PiezaDAO dao = new PiezaDAO();

        int id = dao.guardar(
                proyectoId,
                "Repisa JUnit",
                80.0,
                40.0,
                3
        );

        assertTrue(id > 0);

        assertTrue(
                dao.existeNombreEnProyecto(
                        proyectoId,
                        "Repisa JUnit"
                )
        );

        List<String> piezas = dao.listarPorProyecto(proyectoId);

        assertEquals(1, piezas.size());

        assertTrue(piezas.get(0).contains("Repisa JUnit"));
        assertTrue(piezas.get(0).contains("80.0"));
        assertTrue(piezas.get(0).contains("40.0"));
        assertTrue(piezas.get(0).contains("3"));
    }

    @Test
    void noDebeAceptarNombreVacio() {

        PiezaDAO dao = new PiezaDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        proyectoId,
                        " ",
                        80.0,
                        40.0,
                        1
                )
        );
    }

    @Test
    void noDebeAceptarDimensionesInvalidas() {

        PiezaDAO dao = new PiezaDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        proyectoId,
                        "Pieza inválida",
                        0,
                        40.0,
                        1
                )
        );
    }

    @Test
    void noDebeAceptarCantidadCero() {

        PiezaDAO dao = new PiezaDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        proyectoId,
                        "Pieza inválida",
                        80.0,
                        40.0,
                        0
                )
        );
    }

    @Test
    void debeRecuperarPiezasComoObjetos() throws SQLException {

        PiezaDAO dao = new PiezaDAO();

        dao.guardar(
                proyectoId,
                "Repisa recuperada",
                80.0,
                40.0,
                3
        );

        List<Pieza> piezas =
                dao.listarObjetosPorProyecto(proyectoId);

        assertEquals(1, piezas.size());

        Pieza pieza = piezas.get(0);

        assertEquals("Repisa recuperada", pieza.getNombre());
        assertEquals(80.0, pieza.getAncho());
        assertEquals(40.0, pieza.getLargo());
        assertEquals(3, pieza.getCantidad());
    }

    @Test
    void debeReemplazarPiezasDeUnProyecto() throws SQLException {

        PiezaDAO dao = new PiezaDAO();

        dao.guardar(
                proyectoId,
                "Pieza antigua",
                60.0,
                30.0,
                2
        );

        List<Pieza> nuevasPiezas = List.of(
                new Pieza("Repisa nueva", 80.0, 40.0, 3),
                new Pieza("Costado nuevo", 180.0, 50.0, 2)
        );

        dao.reemplazarPorProyecto(proyectoId, nuevasPiezas);

        List<Pieza> piezasGuardadas =
                dao.listarObjetosPorProyecto(proyectoId);

        assertEquals(2, piezasGuardadas.size());

        assertEquals("Repisa nueva", piezasGuardadas.get(0).getNombre());
        assertEquals("Costado nuevo", piezasGuardadas.get(1).getNombre());

        assertEquals(80.0, piezasGuardadas.get(0).getAncho());
        assertEquals(180.0, piezasGuardadas.get(1).getAncho());
    }
}
