public class Conexion {

    // =========================
    // PRIMERA PIEZA
    // =========================

    Pieza piezaA;

    int indiceVerticeA;


    // =========================
    // SEGUNDA PIEZA
    // =========================

    Pieza piezaB;

    int indiceVerticeB;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Conexion(
            Pieza piezaA,
            int indiceVerticeA,
            Pieza piezaB,
            int indiceVerticeB
    ) {

        this.piezaA =
                piezaA;


        this.indiceVerticeA =
                indiceVerticeA;


        this.piezaB =
                piezaB;


        this.indiceVerticeB =
                indiceVerticeB;
    }
}
