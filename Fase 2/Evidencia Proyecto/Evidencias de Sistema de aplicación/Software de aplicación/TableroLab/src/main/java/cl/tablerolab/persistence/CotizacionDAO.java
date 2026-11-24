
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CotizacionDAO {

    public int guardar(
            int proyectoId,
            Integer proveedorId,
            long precioTableroClp,
            int cantidadTableros,
            String observaciones
    ) throws SQLException {

        if (precioTableroClp <= 0) {
            throw new SQLException("El precio debe ser mayor que cero.");
        }

        if (cantidadTableros <= 0) {
            throw new SQLException("La cantidad de tableros debe ser mayor que cero.");
        }

        String sql = """
                INSERT INTO cotizaciones (
                    proyecto_id,
                    proveedor_id,
                    precio_tablero_clp,
                    cantidad_tableros,
                    fecha,
                    observaciones
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

            statement.setInt(1, proyectoId);

            if (proveedorId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, proveedorId);
            }

            statement.setLong(3, precioTableroClp);
            statement.setInt(4, cantidadTableros);
            statement.setString(5, LocalDate.now().toString());
            statement.setString(6, observaciones);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID de la cotización.");
    }

    public List<String> listarPorProyecto(int proyectoId)
            throws SQLException {

        String sql = """
                SELECT
                    c.id,
                    pr.nombre AS proveedor,
                    c.precio_tablero_clp,
                    c.cantidad_tableros,
                    c.fecha,
                    c.observaciones
                FROM cotizaciones c
                LEFT JOIN proveedores pr
                    ON c.proveedor_id = pr.id
                WHERE c.proyecto_id = ?
                ORDER BY c.id DESC
                """;

        List<String> cotizaciones = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);

            try (ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {

                    long precio = resultado.getLong("precio_tablero_clp");
                    int cantidad = resultado.getInt("cantidad_tableros");
                    long total = Math.multiplyExact(precio, cantidad);

                    String proveedor = resultado.getString("proveedor");

                    cotizaciones.add(
                            "ID: " + resultado.getInt("id")
                                    + " | Proveedor: "
                                    + (proveedor != null ? proveedor : "No especificado")
                                    + " | Precio por tablero: $" + precio + " CLP"
                                    + " | Cantidad: " + cantidad
                                    + " | Total: $" + total + " CLP"
                                    + " | Fecha: " + resultado.getString("fecha")
                                    + " | Observaciones: "
                                    + resultado.getString("observaciones")
                    );
                }
            }
        }

        return cotizaciones;
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
            DELETE FROM cotizaciones
            WHERE proyecto_id = ?
            """;

        try (PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, proyectoId);
            statement.executeUpdate();
        }
    }
}
