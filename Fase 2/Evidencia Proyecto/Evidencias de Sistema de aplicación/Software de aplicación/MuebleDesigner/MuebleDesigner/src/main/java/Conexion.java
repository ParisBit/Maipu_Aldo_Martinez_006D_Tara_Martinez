
public class Conexion {

    private Pieza piezaA;
    private int indiceElementoA;

    private Pieza piezaB;
    private int indiceElementoB;

    public Conexion(
            Pieza piezaA,
            int indiceElementoA,
            Pieza piezaB,
            int indiceElementoB
    ) {
        this.piezaA = piezaA;
        this.indiceElementoA = indiceElementoA;
        this.piezaB = piezaB;
        this.indiceElementoB = indiceElementoB;
    }

    public Pieza getPiezaA() {
        return piezaA;
    }

    public Pieza getPiezaB() {
        return piezaB;
    }

    public int getIndiceElementoA() {
        return indiceElementoA;
    }

    public int getIndiceElementoB() {
        return indiceElementoB;
    }

    public boolean conecta(Pieza pieza) {
        return piezaA == pieza || piezaB == pieza;
    }

    public Pieza obtenerOtraPieza(Pieza pieza) {
        if (piezaA == pieza) {
            return piezaB;
        }

        if (piezaB == pieza) {
            return piezaA;
        }

        return null;
    }
}