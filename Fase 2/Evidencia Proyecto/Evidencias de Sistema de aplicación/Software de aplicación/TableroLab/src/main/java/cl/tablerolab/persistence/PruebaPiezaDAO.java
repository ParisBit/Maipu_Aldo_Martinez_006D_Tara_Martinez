
package cl.tablerolab.persistence;

import java.sql.SQLException;

public class PruebaPiezaDAO {

    private PruebaPiezaDAO() {
    }

    public static void ejecutar() {

        PiezaDAO piezaDAO = new PiezaDAO();
        ProyectoDAO proyectoDAO = new ProyectoDAO();

        try {

            int proyectoId = proyectoDAO.obtenerIdPorNombre(
                    "Proyecto MDF de prueba"
            );

            registrarPieza(
                    piezaDAO,
                    proyectoId,
                    "Lateral",
                    80.0,
                    40.0,
                    2
            );

            registrarPieza(
                    piezaDAO,
                    proyectoId,
                    "Repisa",
                    60.0,
                    30.0,
                    3
            );

            registrarPieza(
                    piezaDAO,
                    proyectoId,
                    "Base",
                    100.0,
                    50.0,
                    1
            );

            System.out.println("\n===== PIEZAS DEL PROYECTO =====");

            for (String pieza : piezaDAO.listarPorProyecto(proyectoId)) {
                System.out.println(pieza);
            }

        } catch (SQLException e) {

            System.err.println("Error al probar PiezaDAO:");
            e.printStackTrace();
        }
    }

    private static void registrarPieza(
            PiezaDAO dao,
            int proyectoId,
            String nombre,
            double ancho,
            double largo,
            int cantidad
    ) throws SQLException {

        if (!dao.existeNombreEnProyecto(proyectoId, nombre)) {

            int piezaId = dao.guardar(
                    proyectoId,
                    nombre,
                    ancho,
                    largo,
                    cantidad
            );

            System.out.println(
                    "Pieza registrada: " + nombre
                            + " (ID: " + piezaId + ")"
            );

        } else {

            System.out.println(
                    "La pieza " + nombre + " ya existe en el proyecto."
            );
        }
    }
}
