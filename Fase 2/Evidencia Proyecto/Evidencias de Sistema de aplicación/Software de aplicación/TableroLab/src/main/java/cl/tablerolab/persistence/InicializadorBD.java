
package cl.tablerolab.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorBD {

    private InicializadorBD() {
    }

    public static void inicializar() throws SQLException {

        try (Connection conexion = ConexionSQLite.obtenerConexion();
             Statement statement = conexion.createStatement()) {

            conexion.setAutoCommit(false);

            try {

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS materiales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        codigo TEXT NOT NULL UNIQUE,
                        nombre TEXT NOT NULL,
                        descripcion TEXT
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS formatos_tablero (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        material_id INTEGER NOT NULL,
                        codigo TEXT NOT NULL UNIQUE,
                        ancho_cm REAL NOT NULL CHECK (ancho_cm > 0),
                        largo_cm REAL NOT NULL CHECK (largo_cm > 0),
                        espesor_mm REAL NOT NULL CHECK (espesor_mm > 0),
                        FOREIGN KEY (material_id)
                            REFERENCES materiales(id)
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS proveedores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL UNIQUE,
                        sitio_web TEXT,
                        telefono TEXT,
                        observaciones TEXT
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS precios_tablero (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        formato_id INTEGER NOT NULL,
                        proveedor_id INTEGER NOT NULL,
                        precio_clp INTEGER NOT NULL CHECK (precio_clp >= 0),
                        fecha_actualizacion TEXT NOT NULL,
                        disponibilidad TEXT NOT NULL DEFAULT 'NO_VERIFICADA',
                        fuente TEXT,
                        FOREIGN KEY (formato_id)
                            REFERENCES formatos_tablero(id),
                        FOREIGN KEY (proveedor_id)
                            REFERENCES proveedores(id)
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS proyectos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        material_id INTEGER NOT NULL,
                        formato_id INTEGER NOT NULL,
                        fecha_creacion TEXT NOT NULL,
                        ancho_corte_mm REAL NOT NULL DEFAULT 3.0
                            CHECK (ancho_corte_mm > 0),
                        FOREIGN KEY (material_id)
                            REFERENCES materiales(id),
                        FOREIGN KEY (formato_id)
                            REFERENCES formatos_tablero(id)
                    )
                """);

                boolean existeColumnaAnchoCorte = false;

                try (ResultSet columnas =
                             statement.executeQuery("PRAGMA table_info(proyectos)")) {

                    while (columnas.next()) {
                        if ("ancho_corte_mm".equals(
                                columnas.getString("name"))) {
                            existeColumnaAnchoCorte = true;
                            break;
                        }
                    }
                }

                if (!existeColumnaAnchoCorte) {
                    statement.execute("""
                        ALTER TABLE proyectos
                        ADD COLUMN ancho_corte_mm REAL NOT NULL DEFAULT 3.0
                        CHECK (ancho_corte_mm > 0)
                    """);
                }

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS piezas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        proyecto_id INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        ancho_cm REAL NOT NULL CHECK (ancho_cm > 0),
                        largo_cm REAL NOT NULL CHECK (largo_cm > 0),
                        cantidad INTEGER NOT NULL CHECK (cantidad > 0),
                        FOREIGN KEY (proyecto_id)
                            REFERENCES proyectos(id)
                            ON DELETE CASCADE
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS cotizaciones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        proyecto_id INTEGER NOT NULL,
                        proveedor_id INTEGER,
                        precio_tablero_clp INTEGER NOT NULL
                            CHECK (precio_tablero_clp >= 0),
                        cantidad_tableros INTEGER NOT NULL
                            CHECK (cantidad_tableros > 0),
                        fecha TEXT NOT NULL,
                        observaciones TEXT,
                        FOREIGN KEY (proyecto_id)
                            REFERENCES proyectos(id),
                        FOREIGN KEY (proveedor_id)
                            REFERENCES proveedores(id)
                    )
                """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS retazos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        proyecto_id INTEGER NOT NULL,
                        numero_tablero INTEGER NOT NULL DEFAULT 1
                            CHECK (numero_tablero >= 1),
                        x_cm REAL NOT NULL DEFAULT 0
                            CHECK (x_cm >= 0),
                        y_cm REAL NOT NULL DEFAULT 0
                            CHECK (y_cm >= 0),
                        ancho_cm REAL NOT NULL CHECK (ancho_cm > 0),
                        largo_cm REAL NOT NULL CHECK (largo_cm > 0),
                        espesor_mm REAL NOT NULL DEFAULT 0
                            CHECK (espesor_mm >= 0),
                        estado TEXT NOT NULL,
                        FOREIGN KEY (proyecto_id)
                            REFERENCES proyectos(id)
                            ON DELETE CASCADE
                    )
                """);

                boolean existeX = false;
                boolean existeY = false;
                boolean existeEspesor = false;
                boolean existeNumeroTablero = false;

                try (ResultSet columnas =
                             statement.executeQuery("PRAGMA table_info(retazos)")) {

                    while (columnas.next()) {

                        String nombreColumna = columnas.getString("name");

                        if ("x_cm".equals(nombreColumna)) {
                            existeX = true;
                        }

                        if ("y_cm".equals(nombreColumna)) {
                            existeY = true;
                        }

                        if ("espesor_mm".equals(nombreColumna)) {
                            existeEspesor = true;
                        }

                        if ("numero_tablero".equals(nombreColumna)) {
                            existeNumeroTablero = true;
                        }
                    }
                }

                if (!existeX) {
                    statement.execute("""
                        ALTER TABLE retazos
                        ADD COLUMN x_cm REAL NOT NULL DEFAULT 0
                        CHECK (x_cm >= 0)
                    """);
                }

                if (!existeY) {
                    statement.execute("""
                        ALTER TABLE retazos
                        ADD COLUMN y_cm REAL NOT NULL DEFAULT 0
                        CHECK (y_cm >= 0)
                    """);
                }

                if (!existeEspesor) {
                    statement.execute("""
                        ALTER TABLE retazos
                        ADD COLUMN espesor_mm REAL NOT NULL DEFAULT 0
                        CHECK (espesor_mm >= 0)
                    """);
                }

                if (!existeNumeroTablero) {
                    statement.execute("""
                        ALTER TABLE retazos
                        ADD COLUMN numero_tablero INTEGER NOT NULL DEFAULT 1
                        CHECK (numero_tablero >= 1)
                    """);
                }

                // Cargar los 10 formatos predefinidos.
                cargarTablerosPredefinidos(conexion);

                conexion.commit();

                System.out.println(
                        "Base de datos TableroLab inicializada correctamente."
                );

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {

                conexion.setAutoCommit(true);
            }
        }
    }

    private static void cargarTablerosPredefinidos(
            Connection conexion
    ) throws SQLException {

        insertarMaterial(conexion, "MELAMINA", "Melamina MDP");
        insertarMaterial(conexion, "MDF", "MDF");
        insertarMaterial(conexion, "TERCIADO", "Terciado estructural");
        insertarMaterial(conexion, "OSB", "OSB");

        int melaminaId = obtenerMaterialId(conexion, "MELAMINA");
        int mdfId = obtenerMaterialId(conexion, "MDF");
        int terciadoId = obtenerMaterialId(conexion, "TERCIADO");
        int osbId = obtenerMaterialId(conexion, "OSB");

        // Melamina MDP
        insertarFormato(conexion, melaminaId,
                "MELAMINA_183_250_15", 183, 250, 15);

        insertarFormato(conexion, melaminaId,
                "MELAMINA_183_250_18", 183, 250, 18);

        // MDF
        insertarFormato(conexion, mdfId,
                "MDF_152_244_3", 152, 244, 3);

        insertarFormato(conexion, mdfId,
                "MDF_152_244_5_5", 152, 244, 5.5);

        insertarFormato(conexion, mdfId,
                "MDF_152_244_9", 152, 244, 9);

        insertarFormato(conexion, mdfId,
                "MDF_152_244_12", 152, 244, 12);

        insertarFormato(conexion, mdfId,
                "MDF_152_244_15", 152, 244, 15);

        insertarFormato(conexion, mdfId,
                "MDF_152_244_18", 152, 244, 18);

        // Terciado estructural
        insertarFormato(conexion, terciadoId,
                "TERCIADO_122_244_15", 122, 244, 15);

        // OSB
        insertarFormato(conexion, osbId,
                "OSB_122_244_9_5", 122, 244, 9.5);
    }

    private static void insertarMaterial(
            Connection conexion,
            String codigo,
            String nombre
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO materiales (codigo, nombre, descripcion)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.setString(2, nombre);
            statement.setString(3, "Material predefinido de TableroLab");
            statement.executeUpdate();
        }
    }

    private static int obtenerMaterialId(
            Connection conexion,
            String codigo
    ) throws SQLException {

        String sql = """
            SELECT id
            FROM materiales
            WHERE codigo = ?
        """;

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, codigo);

            try (ResultSet resultado = statement.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt("id");
                }
            }
        }

        throw new SQLException(
                "No se encontró el material predefinido: " + codigo
        );
    }

    private static void insertarFormato(
            Connection conexion,
            int materialId,
            String codigo,
            double ancho,
            double largo,
            double espesor
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO formatos_tablero (
                material_id,
                codigo,
                ancho_cm,
                largo_cm,
                espesor_mm
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setInt(1, materialId);
            statement.setString(2, codigo);
            statement.setDouble(3, ancho);
            statement.setDouble(4, largo);
            statement.setDouble(5, espesor);

            statement.executeUpdate();
        }
    }
}
