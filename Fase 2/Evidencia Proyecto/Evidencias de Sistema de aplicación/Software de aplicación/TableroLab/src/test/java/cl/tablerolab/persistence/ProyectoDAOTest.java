
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;
    private static int materialId;
    private static int formatoId;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("proyectos_test.db").toString()
        );

        InicializadorBD.inicializar();

        materialId = new MaterialDAO().guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material para proyectos"
        );

        formatoId = new FormatoTableroDAO().guardar(
                materialId,
                "MDF-122-244-TEST",
                122.0,
                244.0,
                15.0
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
    void debeGuardarYConsultarProyecto() throws SQLException {

        ProyectoDAO dao = new ProyectoDAO();

        int id = dao.guardar(
                "Proyecto JUnit",
                materialId,
                formatoId,
                3.0
        );

        assertTrue(id > 0);

        assertTrue(dao.existeNombre("Proyecto JUnit"));

        assertEquals(
                id,
                dao.obtenerIdPorNombre("Proyecto JUnit")
        );

        List<String> proyectos = dao.listar();

        assertTrue(
                proyectos.stream().anyMatch(
                        proyecto -> proyecto.contains("Proyecto JUnit")
                )
        );

        assertTrue(
                proyectos.stream().anyMatch(
                        proyecto -> proyecto.contains("MDF-122-244-TEST")
                )
        );

        assertTrue(
                proyectos.stream().anyMatch(
                        proyecto -> proyecto.contains("Ancho de corte: 3.0 mm")
                )
        );
    }

    @Test
    void noDebeAceptarNombreVacio() {

        ProyectoDAO dao = new ProyectoDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        " ",
                        materialId,
                        formatoId,
                        3.0
                )
        );
    }

    @Test
    void noDebeEncontrarProyectoInexistente() throws SQLException {

        ProyectoDAO dao = new ProyectoDAO();

        assertFalse(
                dao.existeNombre("Proyecto Inexistente")
        );
    }
}
