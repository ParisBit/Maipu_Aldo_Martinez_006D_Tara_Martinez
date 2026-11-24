
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormatoTableroDAO {

    public int guardar(
            int materialId,
            String codigo,
            double anchoCm,
            double largoCm,
            double espesorMm
    ) throws SQLException {

        String sql = """
                INSERT INTO formatos_tablero (
                    material_id,
                    codigo,
                    ancho_cm,
                    largo_cm,
                    espesor_mm
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

            statement.setInt(1, materialId);
            statement.setString(2, codigo.trim());
            statement.setDouble(3, anchoCm);
            statement.setDouble(4, largoCm);
            statement.setDouble(5, espesorMm);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del formato.");
    }

    public List<String> listarPorMaterial(int materialId)
            throws SQLException {

        String sql = """
                SELECT codigo, ancho_cm, largo_cm, espesor_mm
                FROM formatos_tablero
                WHERE material_id = ?
                ORDER BY ancho_cm, largo_cm
                """;

        List<String> formatos = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, materialId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    formatos.add(
                            resultado.getString("codigo")
                                    + " - "
                                    + resultado.getDouble("ancho_cm")
                                    + " x "
                                    + resultado.getDouble("largo_cm")
                                    + " cm - "
                                    + resultado.getDouble("espesor_mm")
                                    + " mm"
                    );
                }
            }
        }

        return formatos;
    }

    public boolean existeCodigo(String codigo) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM formatos_tablero
                WHERE codigo = ?
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setString(1, codigo.trim());

            try (ResultSet resultado = statement.executeQuery()) {

                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }


    public int obtenerIdPorCodigo(String codigo) throws SQLException {

        String sql = """
            SELECT id
            FROM formatos_tablero
            WHERE codigo = ?
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setString(1, codigo.trim());

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt("id");
                }
            }
        }

        throw new SQLException(
                "No existe un formato con el código: " + codigo
        );
    }

    public Map<String, Integer> listarConIdsPorMaterial(int materialId)
            throws SQLException {

        String sql = """
            SELECT id, codigo, ancho_cm, largo_cm, espesor_mm
            FROM formatos_tablero
            WHERE material_id = ?
            ORDER BY ancho_cm, largo_cm
            """;

        Map<String, Integer> formatos = new LinkedHashMap<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, materialId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    String etiqueta =
                            resultado.getString("codigo")
                                    + " - "
                                    + resultado.getDouble("ancho_cm")
                                    + " x "
                                    + resultado.getDouble("largo_cm")
                                    + " cm - "
                                    + resultado.getDouble("espesor_mm")
                                    + " mm";

                    formatos.put(etiqueta, resultado.getInt("id"));
                }
            }
        }

        return formatos;
    }

    public double[] obtenerDimensionesPorId(int formatoId)
            throws SQLException {

        String sql = """
        SELECT ancho_cm, largo_cm, espesor_mm
        FROM formatos_tablero
        WHERE id = ?
        """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, formatoId);

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return new double[]{
                            resultado.getDouble("ancho_cm"),
                            resultado.getDouble("largo_cm"),
                            resultado.getDouble("espesor_mm")
                    };
                }
            }
        }

        throw new SQLException("No existe el formato seleccionado.");
    }

}
