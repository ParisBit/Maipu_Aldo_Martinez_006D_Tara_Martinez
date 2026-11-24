
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrecioTableroDAO {

    public int guardar(
            int formatoId,
            int proveedorId,
            long precioClp,
            String disponibilidad,
            String fuente
    ) throws SQLException {

        if (precioClp <= 0) {
            throw new SQLException("El precio debe ser mayor que cero.");
        }

        if (!List.of(
                "VERIFICADA",
                "NO_VERIFICADA",
                "AGOTADA"
        ).contains(disponibilidad)) {
            throw new SQLException("Estado de disponibilidad no válido.");
        }

        String sql = """
                INSERT INTO precios_tablero (
                    formato_id,
                    proveedor_id,
                    precio_clp,
                    fecha_actualizacion,
                    disponibilidad,
                    fuente
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setInt(1, formatoId);
            statement.setInt(2, proveedorId);
            statement.setLong(3, precioClp);
            statement.setString(4, LocalDate.now().toString());
            statement.setString(5, disponibilidad);
            statement.setString(6, fuente);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del precio.");
    }

    public List<String> listarPorFormato(int formatoId)
            throws SQLException {

        String sql = """
                SELECT
                    p.id,
                    pr.nombre AS proveedor,
                    p.precio_clp,
                    p.fecha_actualizacion,
                    p.disponibilidad,
                    p.fuente
                FROM precios_tablero p
                INNER JOIN proveedores pr
                    ON p.proveedor_id = pr.id
                WHERE p.formato_id = ?
                ORDER BY p.precio_clp
                """;

        List<String> precios = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, formatoId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    precios.add(
                            "ID: " + resultado.getInt("id")
                                    + " | Proveedor: " + resultado.getString("proveedor")
                                    + " | Precio: $" + resultado.getLong("precio_clp")
                                    + " CLP | Fecha: " + resultado.getString("fecha_actualizacion")
                                    + " | Disponibilidad: " + resultado.getString("disponibilidad")
                                    + " | Fuente: " + resultado.getString("fuente")
                    );
                }
            }
        }

        return precios;
    }
}
