import javafx.scene.paint.Color;

public class Pieza {

    // =========================
    // POSICIÓN
    // =========================

    double x;
    double y;


    // =========================
    // DIMENSIONES
    // =========================

    double anchoCm;
    double altoCm;
    double largoCm;


    // =========================
    // COLOR
    // =========================

    Color color;


    // =========================
    // ESTADOS
    // =========================

    boolean seleccionada;
    boolean enColision;
    boolean cerca;


    // =========================
    // VÉRTICES
    // =========================

    Vertice verticeA;
    Vertice verticeB;
    Vertice verticeC;
    Vertice verticeD;


    // =========================
    // ACTUALIZAR VÉRTICES
    // =========================

    public void actualizarVertices(
            double escala
    ) {

        double anchoPx =
                anchoCm * escala;


        double altoPx =
                altoCm * escala;


        // A = superior izquierda

        verticeA =
                new Vertice(
                        x,
                        y
                );


        // B = superior derecha

        verticeB =
                new Vertice(
                        x + anchoPx,
                        y
                );


        // C = inferior derecha

        verticeC =
                new Vertice(
                        x + anchoPx,
                        y + altoPx
                );


        // D = inferior izquierda

        verticeD =
                new Vertice(
                        x,
                        y + altoPx
                );
    }


    // =========================
    // OBTENER VÉRTICE
    // =========================

    public Vertice obtenerVertice(
            int indice
    ) {

        switch (indice) {

            case 0:
                return verticeA;

            case 1:
                return verticeB;

            case 2:
                return verticeC;

            case 3:
                return verticeD;

            default:
                return null;
        }
    }
}
