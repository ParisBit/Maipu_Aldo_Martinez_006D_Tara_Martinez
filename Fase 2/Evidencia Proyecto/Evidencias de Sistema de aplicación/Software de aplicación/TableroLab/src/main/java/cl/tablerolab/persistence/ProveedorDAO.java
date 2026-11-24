
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public int guardar(
            String nombre,
            String sitioWeb,
            String telefono,
            String observaciones
    ) throws SQLException {

        String sql = """
                INSERT INTO proveedores (
                    nombre,
                    sitio_web,
                    telefono,
                    observaciones
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setString(1, nombre.trim());
            statement.setString(2, sitioWeb);
            statement.setString(3, telefono);
            statement.setString(4, observaciones);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del proveedor.");
    }

    public List<String> listar() throws SQLException {

        String sql = """
                SELECT id, nombre, sitio_web, telefono
                FROM proveedores
                ORDER BY nombre
                """;

        List<String> proveedores = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()
        ) {

            while (resultado.next()) {

                proveedores.add(
                        resultado.getInt("id")
                                + " - "
                                + resultado.getString("nombre")
                                + " - "
                                + resultado.getString("sitio_web")
                                + " - "
                                + resultado.getString("telefono")
                );
            }
        }

        return proveedores;
    }

    public boolean existeNombre(String nombre) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM proveedores
                WHERE nombre = ?
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setString(1, nombre.trim());

            try (ResultSet resultado = statement.executeQuery()) {

                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }


    public int obtenerIdPorNombre(String nombre) throws SQLException {

        String sql = """
            SELECT id
            FROM proveedores
            WHERE nombre = ?
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setString(1, nombre.trim());

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt("id");
                }
            }
        }

        throw new SQLException(
                "No existe un proveedor con el nombre: " + nombre
        );
    }

}
