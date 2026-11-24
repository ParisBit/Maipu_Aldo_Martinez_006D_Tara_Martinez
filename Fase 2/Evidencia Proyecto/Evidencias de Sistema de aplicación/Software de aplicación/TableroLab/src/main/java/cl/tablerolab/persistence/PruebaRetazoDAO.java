
package cl.tablerolab.persistence;

import cl.tablerolab.model.Retazo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PruebaRetazoDAO {

    private PruebaRetazoDAO() {
    }

    public static void ejecutar() {

        RetazoDAO retazoDAO = new RetazoDAO();

        try {

            int proyectoId = new ProyectoDAO()
                    .obtenerIdPorNombre("Proyecto MDF de prueba");

            System.out.println("===== REGISTRO DE RETAZOS =====");

            try (Connection conexion =
                         ConexionSQLite.obtenerConexion()) {

                conexion.setAutoCommit(false);

                try {

                    retazoDAO.reemplazarPorProyecto(
                            conexion,
                            proyectoId,
                            List.of(
                                    new Retazo(0, 0, 80, 45, 15),
                                    new Retazo(80, 0, 30, 20, 15)
                            )
                    );

                    conexion.commit();

                } catch (SQLException e) {
                    conexion.rollback();
                    throw e;
                }
            }

            System.out.println("\n===== RETAZOS DEL PROYECTO =====");

            List<Retazo> retazos =
                    retazoDAO.listarObjetosPorProyecto(proyectoId);

            for (Retazo retazo : retazos) {

                System.out.println(
                        "Posición: (" + retazo.getX()
                                + ", " + retazo.getY() + ")"
                                + " | Medidas: "
                                + retazo.getAncho()
                                + " x " + retazo.getLargo() + " cm"
                                + " | Espesor: "
                                + retazo.getEspesor() + " mm"
                                + " | Estado: "
                                + retazo.getEstado()
                );
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al probar RetazoDAO: " + e.getMessage()
            );
        }
    }
}
