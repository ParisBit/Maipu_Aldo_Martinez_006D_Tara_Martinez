
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class GestorConexiones {

    private final List<Conexion> conexiones = new ArrayList<>();

    private final ValidadorConexion validadorConexion =
            new ValidadorConexion();

    private final GestorPiezas gestorPiezas;

    public GestorConexiones() {
        this(null);
    }

    public GestorConexiones(GestorPiezas gestorPiezas) {
        this.gestorPiezas = gestorPiezas;
    }

    public List<Conexion> getConexiones() {
        return Collections.unmodifiableList(conexiones);
    }

    public boolean agregarConexion(
            Pieza piezaA,
            int indiceElementoA,
            Pieza piezaB,
            int indiceElementoB
    ) {
        if (!validadorConexion.sonValidosLosIndices(
                piezaA,
                indiceElementoA,
                piezaB,
                indiceElementoB
        )) {
            return false;
        }

        if (gestorPiezas != null) {
            if (!validadorConexion.puedeConectar(
                    piezaA,
                    piezaB,
                    gestorPiezas,
                    this
            )) {
                return false;
            }
        } else {
            if (yaEstanConectadas(piezaA, piezaB)) {
                return false;
            }
        }

        conexiones.add(new Conexion(
                piezaA,
                indiceElementoA,
                piezaB,
                indiceElementoB
        ));

        return true;
    }

    public boolean yaEstanConectadas(
            Pieza piezaA,
            Pieza piezaB
    ) {
        for (Conexion conexion : conexiones) {
            if ((conexion.getPiezaA() == piezaA
                    && conexion.getPiezaB() == piezaB)
                    || (conexion.getPiezaA() == piezaB
                    && conexion.getPiezaB() == piezaA)) {
                return true;
            }
        }

        return false;
    }

    public Set<Pieza> obtenerGrupoConectado(Pieza inicio) {
        Set<Pieza> grupo =
                Collections.newSetFromMap(new IdentityHashMap<>());

        if (inicio == null) {
            return grupo;
        }

        Queue<Pieza> pendientes = new ArrayDeque<>();

        grupo.add(inicio);
        pendientes.add(inicio);

        while (!pendientes.isEmpty()) {
            Pieza actual = pendientes.poll();

            for (Conexion conexion : conexiones) {
                Pieza otra = conexion.obtenerOtraPieza(actual);

                if (otra != null && grupo.add(otra)) {
                    pendientes.add(otra);
                }
            }
        }

        return grupo;
    }

    public void moverPiezasConectadas(
            Pieza piezaMovida,
            double dx,
            double dy,
            double escala
    ) {
        if (piezaMovida == null) {
            return;
        }

        Set<Pieza> grupo = obtenerGrupoConectado(piezaMovida);

        for (Pieza pieza : grupo) {
            if (pieza != piezaMovida) {
                pieza.setX(pieza.getX() + dx);
                pieza.setY(pieza.getY() + dy);

                pieza.actualizarGeometria(escala);
            }
        }
    }

    public void moverGrupoConectado(
            Pieza inicio,
            double dx,
            double dy,
            double escala
    ) {
        if (inicio == null) {
            return;
        }

        Set<Pieza> grupo = obtenerGrupoConectado(inicio);

        for (Pieza pieza : grupo) {
            pieza.setX(pieza.getX() + dx);
            pieza.setY(pieza.getY() + dy);

            pieza.actualizarGeometria(escala);
        }
    }

    public boolean eliminarConexion(Conexion conexion) {
        return conexiones.remove(conexion);
    }

    public int separarPieza(Pieza pieza) {
        if (pieza == null) {
            return 0;
        }

        int conexionesAntes = conexiones.size();

        conexiones.removeIf(conexion -> conexion.conecta(pieza));

        return conexionesAntes - conexiones.size();
    }

    public int eliminarConexionesDePieza(Pieza pieza) {
        return separarPieza(pieza);
    }

    public void separarTodas() {
        conexiones.clear();
    }
}

