
package cl.tablerolab.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CotizacionDAOTest {

    @TempDir
    static Path carpetaTemporal;

    private static String rutaAnterior;
    private static int proyectoId;
    private static int proveedorId;

    @BeforeAll
    static void prepararBaseDeDatos() throws SQLException {

        rutaAnterior = System.getProperty("tablerolab.db.path");

        System.setProperty(
                "tablerolab.db.path",
                carpetaTemporal.resolve("cotizaciones_test.db").toString()
        );

        InicializadorBD.inicializar();

        int materialId = new MaterialDAO().guardar(
                "MDF-TEST",
                "MDF de prueba",
                "Material para cotizaciones"
        );

        int formatoId = new FormatoTableroDAO().guardar(
                materialId,
                "MDF-122-244-TEST",
                122.0,
                244.0,
                15.0
        );

        proyectoId = new ProyectoDAO().guardar(
                "Proyecto cotización JUnit",
                materialId,
                formatoId,
                3.0
        );

        proveedorId = new ProveedorDAO().guardar(
                "Proveedor cotización JUnit",
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
    void debeGuardarYConsultarCotizacion() throws SQLException {

        CotizacionDAO dao = new CotizacionDAO();

        int id = dao.guardar(
                proyectoId,
                proveedorId,
                45000L,
                2,
                "Cotización de prueba JUnit"
        );

        assertTrue(id > 0);

        List<String> cotizaciones = dao.listarPorProyecto(proyectoId);

        String cotizacionGuardada = cotizaciones.stream()
                .filter(cotizacion ->
                        cotizacion.contains("Cotización de prueba JUnit")
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("No se encontró la cotización guardada.")
                );

        assertTrue(cotizacionGuardada.contains("Proveedor cotización JUnit"));
        assertTrue(cotizacionGuardada.contains("45000"));
        assertTrue(cotizacionGuardada.contains("Cantidad: 2"));
        assertTrue(cotizacionGuardada.contains("90000"));
    }

    @Test
    void debePermitirCotizacionSinProveedor() throws SQLException {

        CotizacionDAO dao = new CotizacionDAO();

        int id = dao.guardar(
                proyectoId,
                null,
                30000L,
                1,
                "Cotización sin proveedor"
        );

        assertTrue(id > 0);

        List<String> cotizaciones = dao.listarPorProyecto(proyectoId);

        assertTrue(
                cotizaciones.stream().anyMatch(
                        cotizacion -> cotizacion.contains("No especificado")
                )
        );
    }

    @Test
    void noDebeAceptarPrecioCero() {

        CotizacionDAO dao = new CotizacionDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        proyectoId,
                        proveedorId,
                        0L,
                        1,
                        "Precio inválido"
                )
        );
    }

    @Test
    void noDebeAceptarCantidadCero() {

        CotizacionDAO dao = new CotizacionDAO();

        assertThrows(
                SQLException.class,
                () -> dao.guardar(
                        proyectoId,
                        proveedorId,
                        45000L,
                        0,
                        "Cantidad inválida"
                )
        );
    }
}
