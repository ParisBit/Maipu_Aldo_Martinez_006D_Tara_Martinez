
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaProyectoDAO {

    private PruebaProyectoDAO() {
    }

    public static void ejecutar() {

        ProyectoDAO proyectoDAO = new ProyectoDAO();

        try {

            String nombre = "Proyecto MDF de prueba";

            int materialId = new MaterialDAO()
                    .obtenerIdPorCodigo("MDF");

            int formatoId = new FormatoTableroDAO()
                    .obtenerIdPorCodigo("MDF-122-244-15");

            if (!proyectoDAO.existeNombre(nombre)) {

                int proyectoId = proyectoDAO.guardar(
                        nombre,
                        materialId,
                        formatoId,
                        3.0
                );

                System.out.println(
                        "Proyecto registrado con ID: " + proyectoId
                );

            } else {

                System.out.println("El proyecto ya existe.");
            }

            System.out.println("\n===== PROYECTOS REGISTRADOS =====");

            for (String proyecto : proyectoDAO.listar()) {
                System.out.println(proyecto);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar ProyectoDAO:");
            e.printStackTrace();
        }
    }
}
