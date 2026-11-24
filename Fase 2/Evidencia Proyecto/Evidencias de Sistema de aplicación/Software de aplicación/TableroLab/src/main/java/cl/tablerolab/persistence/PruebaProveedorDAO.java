
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaProveedorDAO {

    private PruebaProveedorDAO() {
    }

    public static void ejecutar() {

        ProveedorDAO dao = new ProveedorDAO();

        try {

            if (!dao.existeNombre("Proveedor de prueba")) {

                dao.guardar(
                        "Proveedor de prueba",
                        "https://ejemplo.cl",
                        "",
                        "Registro temporal para probar SQLite"
                );

                System.out.println("Proveedor registrado.");

            } else {

                System.out.println("El proveedor ya existe.");
            }

            System.out.println("\n===== PROVEEDORES REGISTRADOS =====");

            for (String proveedor : dao.listar()) {
                System.out.println(proveedor);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar ProveedorDAO:");
            e.printStackTrace();
        }
    }
}
