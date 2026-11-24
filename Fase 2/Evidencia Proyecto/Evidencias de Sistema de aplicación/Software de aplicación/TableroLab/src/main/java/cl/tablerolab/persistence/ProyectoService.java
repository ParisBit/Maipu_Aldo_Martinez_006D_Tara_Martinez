
package cl.tablerolab.persistence;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.Retazo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProyectoService {

    private final ProyectoDAO proyectoDAO;
    private final PiezaDAO piezaDAO;
    private final RetazoDAO retazoDAO;

    private final CotizacionDAO cotizacionDAO;

    public ProyectoService() {
        this.proyectoDAO = new ProyectoDAO();
        this.piezaDAO = new PiezaDAO();
        this.retazoDAO = new RetazoDAO();
        this.cotizacionDAO = new CotizacionDAO();
    }

    public int guardarProyectoCompleto(
            String nombre,
            int materialId,
            int formatoId,
            double anchoCorteMm,
            List<Pieza> piezas
    ) throws SQLException {

        return guardarProyectoCompleto(
                nombre,
                materialId,
                formatoId,
                anchoCorteMm,
                piezas,
                List.of()
        );
    }

    public int guardarProyectoCompleto(
            String nombre,
            int materialId,
            int formatoId,
            double anchoCorteMm,
            List<Pieza> piezas,
            List<Retazo> retazos
    ) throws SQLException {

        if (piezas == null) {
            throw new SQLException(
                    "La lista de piezas no puede ser nula."
            );
        }

        if (retazos == null) {
            throw new SQLException(
                    "La lista de retazos no puede ser nula."
            );
        }

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            boolean autoCommitOriginal = conexion.getAutoCommit();

            try {

                conexion.setAutoCommit(false);

                int proyectoId = proyectoDAO.guardar(
                        conexion,
                        nombre,
                        materialId,
                        formatoId,
                        anchoCorteMm
                );

                piezaDAO.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        piezas
                );

                retazoDAO.reemplazarPorProyecto(
                        conexion,
                        proyectoId,
                        retazos
                );

                conexion.commit();

                return proyectoId;

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {

                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public List<String> listarProyectos() throws SQLException {
        return proyectoDAO.listar();
    }

    public List<String> obtenerDatosProyecto(int proyectoId)
            throws SQLException {
        return proyectoDAO.obtenerDatosPorId(proyectoId);
    }

    public List<Pieza> listarPiezasProyecto(int proyectoId)
            throws SQLException {
        return piezaDAO.listarObjetosPorProyecto(proyectoId);
    }

    public List<Retazo> listarRetazosProyecto(int proyectoId)
            throws SQLException {
        return retazoDAO.listarObjetosPorProyecto(proyectoId);
    }

    public List<Retazo> listarRetazosProyectoYTablero(
            int proyectoId,
            int numeroTablero
    ) throws SQLException {
        return retazoDAO.listarObjetosPorProyectoYTablero(
                proyectoId,
                numeroTablero
        );
    }

    public void eliminarProyecto(int proyectoId) throws SQLException {

        if (proyectoId <= 0) {
            throw new SQLException("El ID del proyecto no es válido.");
        }

        try (Connection conexion = ConexionSQLite.obtenerConexion()) {

            boolean autoCommitOriginal = conexion.getAutoCommit();

            try {
                conexion.setAutoCommit(false);

                cotizacionDAO.eliminarPorProyecto(
                        conexion,
                        proyectoId
                );

                piezaDAO.eliminarPorProyecto(
                        conexion,
                        proyectoId
                );

                retazoDAO.eliminarPorProyecto(
                        conexion,
                        proyectoId
                );

                proyectoDAO.eliminar(
                        conexion,
                        proyectoId
                );

                conexion.commit();

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }
}
