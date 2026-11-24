
package cl.tablerolab.persistence;

import cl.tablerolab.model.Retazo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetazoDAO {

    public void reemplazarPorProyecto(
            Connection conexion,
            int proyectoId,
            List<Retazo> retazos
    ) throws SQLException {

        if (retazos == null) {
            throw new SQLException("La lista de retazos no puede ser nula.");
        }

        String sqlEliminar = """
                DELETE FROM retazos
                WHERE proyecto_id = ?
                """;

        String sqlInsertar = """
                INSERT INTO retazos (
                    proyecto_id,
                    numero_tablero,
                    x_cm,
                    y_cm,
                    ancho_cm,
                    largo_cm,
                    espesor_mm,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement eliminar =
                     conexion.prepareStatement(sqlEliminar)) {

            eliminar.setInt(1, proyectoId);
            eliminar.executeUpdate();
        }

        try (PreparedStatement insertar =
                     conexion.prepareStatement(sqlInsertar)) {

            for (Retazo retazo : retazos) {

                if (retazo == null
                        || retazo.getNumeroTablero() < 1
                        || retazo.getX() < 0
                        || retazo.getY() < 0
                        || retazo.getAncho() <= 0
                        || retazo.getLargo() <= 0
                        || retazo.getEspesor() <= 0
                        || retazo.getEstado() == null) {

                    throw new SQLException("Se encontró un retazo inválido.");
                }

                insertar.setInt(1, proyectoId);
                insertar.setInt(2, retazo.getNumeroTablero());
                insertar.setDouble(3, retazo.getX());
                insertar.setDouble(4, retazo.getY());
                insertar.setDouble(5, retazo.getAncho());
                insertar.setDouble(6, retazo.getLargo());
                insertar.setDouble(7, retazo.getEspesor());
                insertar.setString(8, retazo.getEstado().name());

                insertar.addBatch();
            }

            insertar.executeBatch();
        }
    }

    public List<Retazo> listarObjetosPorProyecto(
            int proyectoId
    ) throws SQLException {

        String sql = """
                SELECT
                    numero_tablero,
                    x_cm,
                    y_cm,
                    ancho_cm,
                    largo_cm,
                    espesor_mm
                FROM retazos
                WHERE proyecto_id = ?
                ORDER BY numero_tablero, id
                """;

        List<Retazo> retazos = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    retazos.add(new Retazo(
                            resultado.getDouble("x_cm"),
                            resultado.getDouble("y_cm"),
                            resultado.getDouble("ancho_cm"),
                            resultado.getDouble("largo_cm"),
                            resultado.getDouble("espesor_mm"),
                            resultado.getInt("numero_tablero")
                    ));
                }
            }
        }

        return retazos;
    }

    public List<Retazo> listarObjetosPorProyectoYTablero(
            int proyectoId,
            int numeroTablero
    ) throws SQLException {

        String sql = """
                SELECT
                    numero_tablero,
                    x_cm,
                    y_cm,
                    ancho_cm,
                    largo_cm,
                    espesor_mm
                FROM retazos
                WHERE proyecto_id = ?
                  AND numero_tablero = ?
                ORDER BY id
                """;

        List<Retazo> retazos = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);
            statement.setInt(2, numeroTablero);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    retazos.add(new Retazo(
                            resultado.getDouble("x_cm"),
                            resultado.getDouble("y_cm"),
                            resultado.getDouble("ancho_cm"),
                            resultado.getDouble("largo_cm"),
                            resultado.getDouble("espesor_mm"),
                            resultado.getInt("numero_tablero")
                    ));
                }
            }
        }

        return retazos;
    }

    public void eliminarPorProyecto(
            Connection conexion,
            int proyectoId
    ) throws SQLException {

        if (conexion == null || conexion.isClosed()) {
            throw new SQLException(
                    "La conexión a la base de datos no está disponible."
            );
        }

        String sql = """
            DELETE FROM retazos
            WHERE proyecto_id = ?
            """;

        try (PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, proyectoId);
            statement.executeUpdate();
        }
    }
}
