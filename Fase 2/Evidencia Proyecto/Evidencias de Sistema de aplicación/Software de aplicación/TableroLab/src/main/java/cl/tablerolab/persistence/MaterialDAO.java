
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

public class MaterialDAO {

    public int guardar(
            String codigo,
            String nombre,
            String descripcion
    ) throws SQLException {

        String sql = """
                INSERT INTO materiales (codigo, nombre, descripcion)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setString(1, codigo.trim());
            statement.setString(2, nombre.trim());
            statement.setString(3, descripcion);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del material.");
    }

    public List<String> listar() throws SQLException {

        String sql = """
                SELECT codigo, nombre, descripcion
                FROM materiales
                ORDER BY nombre
                """;

        List<String> materiales = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()
        ) {

            while (resultado.next()) {

                String codigo = resultado.getString("codigo");
                String nombre = resultado.getString("nombre");
                String descripcion = resultado.getString("descripcion");

                materiales.add(
                        codigo + " - " + nombre +
                                (descripcion != null ? " (" + descripcion + ")" : "")
                );
            }
        }

        return materiales;
    }

    public boolean existeCodigo(String codigo) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM materiales
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
            FROM materiales
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
                "No existe un material con el código: " + codigo
        );
    }

    public Map<String, Integer> listarConIds() throws SQLException {

        String sql = """
            SELECT id, codigo, nombre, descripcion
            FROM materiales
            ORDER BY nombre
            """;

        Map<String, Integer> materiales = new LinkedHashMap<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()
        ) {

            while (resultado.next()) {

                String codigo = resultado.getString("codigo");
                String nombre = resultado.getString("nombre");
                String descripcion = resultado.getString("descripcion");

                String etiqueta = codigo + " - " + nombre
                        + (descripcion != null ? " (" + descripcion + ")" : "");

                materiales.put(etiqueta, resultado.getInt("id"));
            }
        }

        return materiales;
    }

}
