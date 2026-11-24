
package cl.tablerolab.persistence;

import cl.tablerolab.model.Pieza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class PiezaDAO {

    public int guardar(
            int proyectoId,
            String nombre,
            double anchoCm,
            double largoCm,
            int cantidad
    ) throws SQLException {

        if (nombre == null || nombre.isBlank()) {
            throw new SQLException("El nombre de la pieza es obligatorio.");
        }

        if (anchoCm <= 0 || largoCm <= 0) {
            throw new SQLException("Las dimensiones deben ser mayores que cero.");
        }

        if (cantidad <= 0) {
            throw new SQLException("La cantidad debe ser mayor que cero.");
        }

        String sql = """
                INSERT INTO piezas (
                    proyecto_id,
                    nombre,
                    ancho_cm,
                    largo_cm,
                    cantidad
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setInt(1, proyectoId);
            statement.setString(2, nombre.trim());
            statement.setDouble(3, anchoCm);
            statement.setDouble(4, largoCm);
            statement.setInt(5, cantidad);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID de la pieza.");
    }

    public List<String> listarPorProyecto(int proyectoId)
            throws SQLException {

        String sql = """
                SELECT
                    id,
                    nombre,
                    ancho_cm,
                    largo_cm,
                    cantidad
                FROM piezas
                WHERE proyecto_id = ?
                ORDER BY id
                """;

        List<String> piezas = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    piezas.add(
                            "ID: " + resultado.getInt("id")
                                    + " | Pieza: " + resultado.getString("nombre")
                                    + " | Medidas: "
                                    + resultado.getDouble("ancho_cm")
                                    + " x "
                                    + resultado.getDouble("largo_cm")
                                    + " cm"
                                    + " | Cantidad: "
                                    + resultado.getInt("cantidad")
                    );
                }
            }
        }

        return piezas;
    }

    public void reemplazarPorProyecto(
            int proyectoId,
            List<Pieza> piezas
    ) throws SQLException {

        String sqlEliminar = """
            DELETE FROM piezas
            WHERE proyecto_id = ?
            """;

        String sqlInsertar = """
            INSERT INTO piezas (
                proyecto_id,
                nombre,
                ancho_cm,
                largo_cm,
                cantidad
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            try {

                try (PreparedStatement eliminar =
                             conexion.prepareStatement(sqlEliminar)) {

                    eliminar.setInt(1, proyectoId);
                    eliminar.executeUpdate();
                }

                try (PreparedStatement insertar =
                             conexion.prepareStatement(sqlInsertar)) {

                    for (Pieza pieza : piezas) {

                        if (pieza == null
                                || pieza.getNombre() == null
                                || pieza.getNombre().isBlank()
                                || pieza.getAncho() <= 0
                                || pieza.getLargo() <= 0
                                || pieza.getCantidad() <= 0) {

                            throw new SQLException(
                                    "La lista contiene una pieza inválida."
                            );
                        }

                        insertar.setInt(1, proyectoId);
                        insertar.setString(2, pieza.getNombre().trim());
                        insertar.setDouble(3, pieza.getAncho());
                        insertar.setDouble(4, pieza.getLargo());
                        insertar.setInt(5, pieza.getCantidad());

                        insertar.addBatch();
                    }

                    insertar.executeBatch();
                }

                conexion.commit();

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {

                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public void reemplazarPorProyecto(
            Connection conexion,
            int proyectoId,
            List<Pieza> piezas
    ) throws SQLException {

        if (conexion == null || conexion.isClosed()) {
            throw new SQLException(
                    "La conexión a la base de datos no está disponible."
            );
        }

        if (piezas == null) {
            throw new SQLException("La lista de piezas no puede ser nula.");
        }

        String sqlEliminar = """
        DELETE FROM piezas
        WHERE proyecto_id = ?
        """;

        String sqlInsertar = """
        INSERT INTO piezas (
            proyecto_id,
            nombre,
            ancho_cm,
            largo_cm,
            cantidad
        )
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement eliminar =
                     conexion.prepareStatement(sqlEliminar)) {

            eliminar.setInt(1, proyectoId);
            eliminar.executeUpdate();
        }

        try (PreparedStatement insertar =
                     conexion.prepareStatement(sqlInsertar)) {

            for (Pieza pieza : piezas) {

                if (pieza == null
                        || pieza.getNombre() == null
                        || pieza.getNombre().isBlank()
                        || pieza.getAncho() <= 0
                        || pieza.getLargo() <= 0
                        || pieza.getCantidad() <= 0) {

                    throw new SQLException(
                            "La lista contiene una pieza inválida."
                    );
                }

                insertar.setInt(1, proyectoId);
                insertar.setString(2, pieza.getNombre().trim());
                insertar.setDouble(3, pieza.getAncho());
                insertar.setDouble(4, pieza.getLargo());
                insertar.setInt(5, pieza.getCantidad());

                insertar.addBatch();
            }

            insertar.executeBatch();
        }
    }

    public boolean existeNombreEnProyecto(
            int proyectoId,
            String nombre
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM piezas
                WHERE proyecto_id = ?
                  AND nombre = ?
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);
            statement.setString(2, nombre.trim());

            try (ResultSet resultado = statement.executeQuery()) {

                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    public List<Pieza> listarObjetosPorProyecto(int proyectoId)
            throws SQLException {

        String sql = """
            SELECT
                nombre,
                ancho_cm,
                largo_cm,
                cantidad
            FROM piezas
            WHERE proyecto_id = ?
            ORDER BY id
            """;

        List<Pieza> piezas = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    piezas.add(
                            new Pieza(
                                    resultado.getString("nombre"),
                                    resultado.getDouble("ancho_cm"),
                                    resultado.getDouble("largo_cm"),
                                    resultado.getInt("cantidad")
                            )
                    );
                }
            }
        }

        return piezas;
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
            DELETE FROM piezas
            WHERE proyecto_id = ?
            """;

        try (PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, proyectoId);
            statement.executeUpdate();
        }
    }
}
