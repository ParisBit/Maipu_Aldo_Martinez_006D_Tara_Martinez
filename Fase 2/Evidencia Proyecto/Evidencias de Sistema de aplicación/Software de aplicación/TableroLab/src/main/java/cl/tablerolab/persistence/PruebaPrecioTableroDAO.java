
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaPrecioTableroDAO {

    private PruebaPrecioTableroDAO() {
    }

    public static void ejecutar() {

        PrecioTableroDAO precioDAO = new PrecioTableroDAO();

        try {

            int formatoId = new FormatoTableroDAO()
                    .obtenerIdPorCodigo("MDF-122-244-15");

            int proveedorId = new ProveedorDAO()
                    .obtenerIdPorNombre("Proveedor de prueba");

            precioDAO.guardar(
                    formatoId,
                    proveedorId,
                    45000L,
                    "NO_VERIFICADA",
                    "Registro temporal de prueba"
            );

            System.out.println("Precio de prueba registrado.");

            System.out.println("\n===== PRECIOS DEL FORMATO =====");

            for (String precio : precioDAO.listarPorFormato(formatoId)) {
                System.out.println(precio);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar PrecioTableroDAO:");
            e.printStackTrace();
        }
    }
}
