
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaFormatoTableroDAO {

    private PruebaFormatoTableroDAO() {
    }

    public static void ejecutar() {

        MaterialDAO materialDAO = new MaterialDAO();
        FormatoTableroDAO formatoDAO = new FormatoTableroDAO();

        try {

            int materialId = materialDAO.obtenerIdPorCodigo("MDF");

            if (!formatoDAO.existeCodigo("MDF-122-244-15")) {

                formatoDAO.guardar(
                        materialId,
                        "MDF-122-244-15",
                        122,
                        244,
                        15
                );

                System.out.println("Formato MDF 122 x 244 registrado.");

            } else {

                System.out.println("El formato MDF 122 x 244 ya existe.");
            }

            System.out.println("\n===== FORMATOS COMERCIALES MDF =====");

            for (String formato : formatoDAO.listarPorMaterial(materialId)) {
                System.out.println(formato);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar FormatoTableroDAO:");
            e.printStackTrace();
        }
    }
}
