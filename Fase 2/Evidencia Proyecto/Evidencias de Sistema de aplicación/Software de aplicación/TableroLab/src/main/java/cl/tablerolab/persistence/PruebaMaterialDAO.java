package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaMaterialDAO {

    private PruebaMaterialDAO() {
    }

    public static void ejecutar() {

        MaterialDAO dao = new MaterialDAO();

        try {

            if (!dao.existeCodigo("MDF")) {

                dao.guardar(
                        "MDF",
                        "MDF",
                        "Tablero de fibra de densidad media"
                );

                System.out.println("Material MDF registrado.");

            } else {

                System.out.println("El material MDF ya existe.");
            }

            System.out.println("\n===== CATÁLOGO DE MATERIALES =====");

            for (String material : dao.listar()) {
                System.out.println(material);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar MaterialDAO:");
            e.printStackTrace();
        }
    }
}