
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrecioTableroDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;
    private static int formatoId;
    private static int proveedorId;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("precios_test.db").toString()
        );

        InicializadorBD.inicializar();

        MaterialDAO materialDAO = new MaterialDAO();

        int materialId = materialDAO.guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material para pruebas de precios"
        );

        formatoId = new FormatoTableroDAO().guardar(
                materialId,
                "MDF-122-244-TEST",
                122.0,
                244.0,
                15.0
        );

        proveedorId = new ProveedorDAO().guardar(
                "Proveedor de precios JUnit",
                "https://ejemplo.cl",
                "+56912345678",
                "Proveedor para pruebas"
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
    void debeGuardarYConsultarPrecio() throws SQLException {

        PrecioTableroDAO dao = new PrecioTableroDAO();

        int id = dao.guardar(
                formatoId,
                proveedorId,
                45000L,
                "NO_VERIFICADA",
                "Precio de prueba JUnit"
        );

        assertTrue(id > 0);

        List<String> precios = dao.listarPorFormato(formatoId);

        assertEquals(1, precios.size());

        assertTrue(precios.get(0).contains("45000"));
        assertTrue(precios.get(0).contains("Proveedor de precios JUnit"));
        assertTrue(precios.get(0).contains("NO_VERIFICADA"));
    }

    @Test
    void noDebeAceptarPrecioCero() {

        PrecioTableroDAO dao = new PrecioTableroDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        formatoId,
                        proveedorId,
                        0L,
                        "NO_VERIFICADA",
                        "Precio inválido"
                )
        );
    }

    @Test
    void noDebeAceptarDisponibilidadInvalida() {

        PrecioTableroDAO dao = new PrecioTableroDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        formatoId,
                        proveedorId,
                        45000L,
                        "DISPONIBLE",
                        "Estado inválido"
                )
        );
    }
}
