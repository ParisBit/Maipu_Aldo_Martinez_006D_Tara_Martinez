
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaCotizacionDAO {

    private PruebaCotizacionDAO() {
    }

    public static void ejecutar() {

        CotizacionDAO cotizacionDAO = new CotizacionDAO();

        try {

            int proyectoId = new ProyectoDAO()
                    .obtenerIdPorNombre("Proyecto MDF de prueba");

            int proveedorId = new ProveedorDAO()
                    .obtenerIdPorNombre("Proveedor de prueba");

            int cotizacionId = cotizacionDAO.guardar(
                    proyectoId,
                    proveedorId,
                    45000L,
                    2,
                    "Cotización temporal de prueba. Precio no verificado."
            );

            System.out.println(
                    "Cotización registrada con ID: " + cotizacionId
            );

            System.out.println("\n===== COTIZACIONES DEL PROYECTO =====");

            for (String cotizacion :
                    cotizacionDAO.listarPorProyecto(proyectoId)) {

                System.out.println(cotizacion);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar CotizacionDAO:");
            e.printStackTrace();
        }
    }
}
