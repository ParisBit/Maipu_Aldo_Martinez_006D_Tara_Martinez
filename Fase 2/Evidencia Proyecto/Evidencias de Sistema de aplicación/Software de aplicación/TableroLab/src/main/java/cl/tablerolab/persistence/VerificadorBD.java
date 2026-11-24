package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VerificadorBD {

    private VerificadorBD() {
    }

    public static void mostrarTablas() {

        String consulta = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                AND name NOT LIKE 'sqlite_%'
                ORDER BY name
                """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                Statement statement = conexion.createStatement();
                ResultSet resultado = statement.executeQuery(consulta)
        ) {

            System.out.println("\n===== TABLAS DE TABLEROLAB =====");

            int cantidad = 0;

            while (resultado.next()) {

                System.out.println("- " + resultado.getString("name"));
                cantidad++;
            }

            System.out.println("--------------------------------");
            System.out.println("Total de tablas: " + cantidad);
            System.out.println("================================\n");

        } catch (SQLException e) {

            System.err.println("Error al consultar las tablas:");
            e.printStackTrace();
        }
    }

    public static void verificarAnchoCorte() {

        String consulta = "PRAGMA table_info(proyectos)";

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                Statement statement = conexion.createStatement();
                ResultSet resultado = statement.executeQuery(consulta)
        ) {

            System.out.println("\n===== COLUMNAS DE PROYECTOS =====");

            boolean encontrada = false;

            while (resultado.next()) {

                String nombreColumna = resultado.getString("name");

                System.out.println("- " + nombreColumna);

                if ("ancho_corte_mm".equals(nombreColumna)) {
                    encontrada = true;
                }
            }

            System.out.println("--------------------------------");

            if (encontrada) {
                System.out.println("OK: ancho_corte_mm existe.");
            } else {
                System.out.println("ERROR: no se encontró ancho_corte_mm.");
            }

            System.out.println("================================\n");

        } catch (SQLException e) {

            System.err.println("Error al verificar las columnas:");
            e.printStackTrace();
        }
    }

    public static void verificarCatalogo() {

        String consulta = """
            SELECT
                m.id AS material_id,
                m.codigo AS material_codigo,
                m.nombre AS material_nombre,
                f.id AS formato_id,
                f.codigo AS formato_codigo,
                f.ancho_cm,
                f.largo_cm,
                f.espesor_mm
            FROM materiales m
            LEFT JOIN formatos_tablero f
                ON f.material_id = m.id
            ORDER BY m.nombre, f.codigo
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                Statement statement = conexion.createStatement();
                ResultSet resultado = statement.executeQuery(consulta)
        ) {

            System.out.println("\n===== CATÁLOGO DE MATERIALES Y FORMATOS =====");

            while (resultado.next()) {

                System.out.println(
                        "Material ID: " + resultado.getInt("material_id")
                                + " | Código: " + resultado.getString("material_codigo")
                                + " | Nombre: " + resultado.getString("material_nombre")
                );

                int formatoId = resultado.getInt("formato_id");

                if (resultado.wasNull()) {
                    System.out.println("   Sin formatos registrados.");
                } else {
                    System.out.println(
                            "   Formato ID: " + formatoId
                                    + " | Código: " + resultado.getString("formato_codigo")
                                    + " | Medidas: " + resultado.getDouble("ancho_cm")
                                    + " x " + resultado.getDouble("largo_cm")
                                    + " cm | Espesor: " + resultado.getDouble("espesor_mm")
                                    + " mm"
                    );
                }
            }

            System.out.println("============================================\n");

        } catch (SQLException e) {
            System.err.println("Error al consultar el catálogo:");
            e.printStackTrace();
        }
    }

    public static void verificarProyectos() {

        String consulta = """
            SELECT
                p.id,
                p.nombre,
                m.nombre AS material,
                f.codigo AS formato,
                p.ancho_corte_mm,
                p.fecha_creacion
            FROM proyectos p
            JOIN materiales m ON m.id = p.material_id
            JOIN formatos_tablero f ON f.id = p.formato_id
            ORDER BY p.id
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                Statement statement = conexion.createStatement();
                ResultSet resultado = statement.executeQuery(consulta)
        ) {

            System.out.println("\n===== PROYECTOS GUARDADOS =====");

            boolean hayProyectos = false;

            while (resultado.next()) {
                hayProyectos = true;

                System.out.println(
                        "ID: " + resultado.getInt("id")
                                + " | Nombre: " + resultado.getString("nombre")
                                + " | Material: " + resultado.getString("material")
                                + " | Formato: " + resultado.getString("formato")
                                + " | Ancho de corte: " + resultado.getDouble("ancho_corte_mm")
                                + " mm"
                                + " | Fecha: " + resultado.getString("fecha_creacion")
                );
            }

            if (!hayProyectos) {
                System.out.println("No hay proyectos guardados.");
            }

            System.out.println("================================\n");

        } catch (SQLException e) {
            System.err.println("Error al consultar los proyectos:");
            e.printStackTrace();
        }
    }

    public static void verificarPiezas() {

        String consulta = """
            SELECT
                pi.id,
                pi.proyecto_id,
                p.nombre AS proyecto,
                pi.nombre AS pieza,
                pi.ancho_cm,
                pi.largo_cm,
                pi.cantidad
            FROM piezas pi
            JOIN proyectos p ON p.id = pi.proyecto_id
            ORDER BY pi.proyecto_id, pi.id
            """;

        try (
                Connection conexion = ConexionSQLite.obtenerConexion();
                Statement statement = conexion.createStatement();
                ResultSet resultado = statement.executeQuery(consulta)
        ) {

            System.out.println("\n===== PIEZAS GUARDADAS =====");

            boolean hayPiezas = false;

            while (resultado.next()) {
                hayPiezas = true;

                System.out.println(
                        "ID: " + resultado.getInt("id")
                                + " | Proyecto ID: " + resultado.getInt("proyecto_id")
                                + " | Proyecto: " + resultado.getString("proyecto")
                                + " | Pieza: " + resultado.getString("pieza")
                                + " | Medidas: " + resultado.getDouble("ancho_cm")
                                + " x " + resultado.getDouble("largo_cm")
                                + " cm | Cantidad: " + resultado.getInt("cantidad")
                );
            }

            if (!hayPiezas) {
                System.out.println("No hay piezas guardadas.");
            }

            System.out.println("================================\n");

        } catch (SQLException e) {
            System.err.println("Error al consultar las piezas:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        mostrarTablas();

        verificarAnchoCorte();

        verificarCatalogo();

        verificarProyectos();

        verificarPiezas();
    }


}