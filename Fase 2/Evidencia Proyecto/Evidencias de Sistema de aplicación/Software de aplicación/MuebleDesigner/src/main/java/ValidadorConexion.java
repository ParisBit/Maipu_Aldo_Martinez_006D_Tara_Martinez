
public class ValidadorConexion {

    public boolean sonValidas(
            Pieza piezaA,
            Pieza piezaB
    ) {
        if (piezaA == null || piezaB == null) {
            return false;
        }

        if (piezaA == piezaB) {
            return false;
        }

        return true;
    }

    public boolean sonValidosLosIndices(
            Pieza piezaA,
            int indiceElementoA,
            Pieza piezaB,
            int indiceElementoB
    ) {
        if (!sonValidas(piezaA, piezaB)) {
            return false;
        }

        if (indiceElementoA < 0 || indiceElementoB < 0) {
            return false;
        }

        if (indiceElementoA >=
                piezaA.obtenerElementosConexion().size()) {
            return false;
        }

        if (indiceElementoB >=
                piezaB.obtenerElementosConexion().size()) {
            return false;
        }

        return true;
    }

    public boolean puedeConectar(
            Pieza piezaA,
            Pieza piezaB,
            GestorPiezas gestorPiezas,
            GestorConexiones gestorConexiones
    ) {
        if (!sonValidas(piezaA, piezaB)) {
            return false;
        }

        if (gestorPiezas == null || gestorConexiones == null) {
            return false;
        }

        if (!gestorPiezas.contienePieza(piezaA)
                || !gestorPiezas.contienePieza(piezaB)) {
            return false;
        }

        if (gestorConexiones.yaEstanConectadas(
                piezaA, piezaB)) {
            return false;
        }

        return true;
    }
}