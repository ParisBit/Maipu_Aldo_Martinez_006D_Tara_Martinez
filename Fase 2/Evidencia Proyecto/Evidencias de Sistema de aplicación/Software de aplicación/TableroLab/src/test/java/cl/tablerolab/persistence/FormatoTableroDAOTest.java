
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatoTableroDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;
    private static int materialId;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("formatos_test.db").toString()
        );

        InicializadorBD.inicializar();

        MaterialDAO materialDAO = new MaterialDAO();

        materialId = materialDAO.guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material para pruebas de formatos"
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
    void debeGuardarYConsultarFormato() throws SQLException {

        FormatoTableroDAO dao = new FormatoTableroDAO();

        int id = dao.guardar(
                materialId,
                "MDF-122-244-15-TEST",
                122.0,
                244.0,
                15.0
        );

        assertTrue(id > 0);

        assertTrue(
                dao.existeCodigo("MDF-122-244-15-TEST")
        );

        assertEquals(
                id,
                dao.obtenerIdPorCodigo("MDF-122-244-15-TEST")
        );

        List<String> formatos = dao.listarPorMaterial(materialId);

        assertTrue(
                formatos.stream().anyMatch(
                        formato -> formato.contains("122.0 x 244.0")
                )
        );
    }

    @Test
    void noDebeEncontrarUnFormatoInexistente() throws SQLException {

        FormatoTableroDAO dao = new FormatoTableroDAO();

        assertFalse(
                dao.existeCodigo("FORMATO-INEXISTENTE")
        );
    }
}
