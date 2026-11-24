
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProyectoDAO {

    public int guardar(
            String nombre,
            int materialId,
            int formatoId,
            double anchoCorteMm
    ) throws SQLException {

        if (nombre == null || nombre.isBlank()) {
            throw new SQLException("El nombre del proyecto es obligatorio.");
        }

        if (anchoCorteMm <= 0) {
            throw new SQLException("El ancho de corte debe ser mayor que cero.");
        }

        String sql = """
            INSERT INTO proyectos (
                nombre,
                material_id,
                formato_id,
                fecha_creacion,
                ancho_corte_mm
            )
            VALUES (?, ?, ?, date('now', 'localtime'), ?)
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setString(1, nombre.trim());
            statement.setInt(2, materialId);
            statement.setInt(3, formatoId);
            statement.setDouble(4, anchoCorteMm);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del proyecto.");
    }

    public int guardar(
            Connection conexion,
            String nombre,
            int materialId,
            int formatoId,
            double anchoCorteMm
    ) throws SQLException {

        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("La conexión a la base de datos no está disponible.");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new SQLException("El nombre del proyecto es obligatorio.");
        }

        if (anchoCorteMm <= 0) {
            throw new SQLException("El ancho de corte debe ser mayor que cero.");
        }

        String sql = """
        INSERT INTO proyectos (
            nombre,
            material_id,
            formato_id,
            fecha_creacion,
            ancho_corte_mm
        )
        VALUES (?, ?, ?, date('now', 'localtime'), ?)
        """;

        try (PreparedStatement statement = conexion.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, nombre.trim());
            statement.setInt(2, materialId);
            statement.setInt(3, formatoId);
            statement.setDouble(4, anchoCorteMm);

            statement.executeUpdate();

            try (ResultSet resultado = statement.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del proyecto.");
    }

    public List<String> listar() throws SQLException {

        String sql = """
                SELECT
                    p.id,
                    p.nombre,
                    m.nombre AS material,
                    f.codigo AS formato,
                    f.ancho_cm,
                    f.largo_cm,
                    f.espesor_mm,
                    p.fecha_creacion,
                    p.ancho_corte_mm
                FROM proyectos p
                INNER JOIN materiales m
                    ON p.material_id = m.id
                INNER JOIN formatos_tablero f
                    ON p.formato_id = f.id
                ORDER BY p.id DESC
                """;

        List<String> proyectos = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()
        ) {

            while (resultado.next()) {

                proyectos.add(
                        "ID: " + resultado.getInt("id")
                                + " | Proyecto: " + resultado.getString("nombre")
                                + " | Material: " + resultado.getString("material")
                                + " | Formato: " + resultado.getString("formato")
                                + " | Medidas: "
                                + resultado.getDouble("ancho_cm")
                                + " x "
                                + resultado.getDouble("largo_cm")
                                + " cm"
                                + " | Espesor: "
                                + resultado.getDouble("espesor_mm")
                                + " mm"
                                + " | Fecha: "
                                + resultado.getString("fecha_creacion")
                                + " | Ancho de corte: "
                                + resultado.getDouble("ancho_corte_mm")
                                + " mm"
                );
            }
        }

        return proyectos;
    }

    public List<String> obtenerDatosPorId(int proyectoId)
            throws SQLException {

        String sql = """
            SELECT
                p.id,
                p.nombre,
                p.material_id,
                p.formato_id,
                p.ancho_corte_mm,
                f.ancho_cm,
                f.largo_cm,
                f.espesor_mm
            FROM proyectos p
            INNER JOIN formatos_tablero f
                ON p.formato_id = f.id
            WHERE p.id = ?
            """;

        List<String> datos = new ArrayList<>();

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)
        ) {

            statement.setInt(1, proyectoId);

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {

                    datos.add(resultado.getString("nombre"));
                    datos.add(String.valueOf(resultado.getInt("material_id")));
                    datos.add(String.valueOf(resultado.getInt("formato_id")));
                    datos.add(String.valueOf(resultado.getDouble("ancho_corte_mm")));
                    datos.add(String.valueOf(resultado.getDouble("ancho_cm")));
                    datos.add(String.valueOf(resultado.getDouble("largo_cm")));
                    datos.add(String.valueOf(resultado.getDouble("espesor_mm")));

                    return datos;
                }
            }
        }

        throw new SQLException(
                "No existe un proyecto con ID: " + proyectoId
        );
    }

    public boolean existeNombre(String nombre) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM proyectos
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
                FROM proyectos
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
                "No existe un proyecto con el nombre: " + nombre
        );
    }

    public void eliminar(
            Connection conexion,
            int proyectoId
    ) throws SQLException {

        if (conexion == null || conexion.isClosed()) {
            throw new SQLException(
                    "La conexión a la base de datos no está disponible."
            );
        }

        String sql = """
            DELETE FROM proyectos
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     conexion.prepareStatement(sql)) {

            statement.setInt(1, proyectoId);

            int filasEliminadas = statement.executeUpdate();

            if (filasEliminadas == 0) {
                throw new SQLException(
                        "No se encontró el proyecto que se desea eliminar."
                );
            }
        }
    }
}
