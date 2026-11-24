
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("materiales_test.db").toString()
        );

        InicializadorBD.inicializar();
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
    void debeGuardarYConsultarMaterial() throws SQLException {

        MaterialDAO dao = new MaterialDAO();

        int id = dao.guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material utilizado en pruebas"
        );

        assertTrue(id > 0);

        assertTrue(dao.existeCodigo("MDF-TEST"));

        assertEquals(
                id,
                dao.obtenerIdPorCodigo("MDF-TEST")
        );

        List<String> materiales = dao.listar();

        assertTrue(
                materiales.stream().anyMatch(
                        material -> material.contains("MDF-TEST")
                )
        );
    }

    @Test
    void noDebeEncontrarUnMaterialInexistente() throws SQLException {

        MaterialDAO dao = new MaterialDAO();

        assertFalse(dao.existeCodigo("CODIGO-INEXISTENTE"));
    }
}
