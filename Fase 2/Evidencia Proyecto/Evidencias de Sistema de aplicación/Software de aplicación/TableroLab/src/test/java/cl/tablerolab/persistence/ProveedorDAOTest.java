
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProveedorDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("proveedores_test.db").toString()
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
    void debeGuardarYConsultarProveedor() throws SQLException {

        ProveedorDAO dao = new ProveedorDAO();

        int id = dao.guardar(
                "Proveedor JUnit",
                "https://ejemplo.cl",
                "+56912345678",
                "Proveedor utilizado en pruebas"
        );

        assertTrue(id > 0);

        assertTrue(dao.existeNombre("Proveedor JUnit"));

        assertEquals(
                id,
                dao.obtenerIdPorNombre("Proveedor JUnit")
        );

        List<String> proveedores = dao.listar();

        assertTrue(
                proveedores.stream().anyMatch(
                        proveedor -> proveedor.contains("Proveedor JUnit")
                )
        );
    }

    @Test
    void noDebeEncontrarProveedorInexistente() throws SQLException {

        ProveedorDAO dao = new ProveedorDAO();

        assertFalse(
                dao.existeNombre("Proveedor Inexistente")
        );
    }
}
