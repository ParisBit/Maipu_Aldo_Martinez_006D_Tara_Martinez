
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorPiezas {

    private final ArrayList<Pieza> piezas = new ArrayList<>();

    private final ValidadorPieza validadorPieza =
            new ValidadorPieza();

    public boolean agregarPieza(Pieza pieza) {

        if (!validadorPieza.esValida(pieza)
                || piezas.contains(pieza)) {
            return false;
        }

        piezas.add(pieza);
        return true;
    }

    public boolean eliminarPieza(
            Pieza pieza,
            GestorConexiones gestorConexiones
    ) {
        if (pieza == null || !piezas.contains(pieza)) {
            return false;
        }

        if (gestorConexiones != null) {
            gestorConexiones.eliminarConexionesDePieza(pieza);
        }

        return piezas.remove(pieza);
    }

    public List<Pieza> obtenerPiezas() {
        return Collections.unmodifiableList(piezas);
    }

    public int obtenerCantidadPiezas() {
        return piezas.size();
    }

    public boolean contienePieza(Pieza pieza) {
        return piezas.contains(pieza);
    }

    public void eliminarTodas(
            GestorConexiones gestorConexiones
    ) {
        if (gestorConexiones != null) {
            for (Pieza pieza : new ArrayList<>(piezas)) {
                gestorConexiones.eliminarConexionesDePieza(pieza);
            }
        }

        piezas.clear();
    }

    /**
     * Mantiene compatibilidad con el método anterior.
     * Para borrar piezas y sus conexiones, se recomienda usar
     * eliminarTodas(GestorConexiones).
     */
    public void eliminarTodas() {
        piezas.clear();
    }
}