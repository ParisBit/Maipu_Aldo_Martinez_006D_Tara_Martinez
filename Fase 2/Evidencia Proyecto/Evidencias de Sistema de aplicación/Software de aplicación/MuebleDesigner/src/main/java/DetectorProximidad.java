
import java.util.List;

public class DetectorProximidad {

    private double distanciaProximidad;

    public DetectorProximidad(double distanciaProximidad) {
        setDistanciaProximidad(distanciaProximidad);
    }

    public double getDistanciaProximidad() {
        return distanciaProximidad;
    }

    public void setDistanciaProximidad(double distanciaProximidad) {
        if (!Double.isFinite(distanciaProximidad)
                || distanciaProximidad < 0) {
            throw new IllegalArgumentException(
                    "La distancia de proximidad debe ser finita y no negativa."
            );
        }

        this.distanciaProximidad = distanciaProximidad;
    }

    /**
     * Comprueba si los límites rectangulares de dos piezas están cerca.
     * Se conserva para mantener la compatibilidad con el código actual.
     */
    public boolean estanCerca(Pieza a, Pieza b, double escala) {

        if (a == null || b == null || a == b
                || !Double.isFinite(escala) || escala <= 0) {
            return false;
        }

        double izquierdaA = a.getX();
        double superiorA = a.getY();
        double derechaA = izquierdaA + a.getAnchoCm() * escala;
        double abajoA = superiorA + a.getAltoCm() * escala;

        double izquierdaB = b.getX();
        double superiorB = b.getY();
        double derechaB = izquierdaB + b.getAnchoCm() * escala;
        double abajoB = superiorB + b.getAltoCm() * escala;

        double distanciaX;

        if (derechaA < izquierdaB) {
            distanciaX = izquierdaB - derechaA;
        } else if (derechaB < izquierdaA) {
            distanciaX = izquierdaA - derechaB;
        } else {
            distanciaX = 0;
        }

        double distanciaY;

        if (abajoA < superiorB) {
            distanciaY = superiorB - abajoA;
        } else if (abajoB < superiorA) {
            distanciaY = superiorA - abajoB;
        } else {
            distanciaY = 0;
        }

        return Math.hypot(distanciaX, distanciaY)
                <= distanciaProximidad;
    }

    /**
     * Calcula la distancia entre dos vértices.
     */
    public double distanciaEntreVertices(Vertice a, Vertice b) {

        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    /**
     * Comprueba si dos vértices están suficientemente cerca.
     */
    public boolean verticesEstanCerca(Vertice a, Vertice b) {
        return distanciaEntreVertices(a, b) <= distanciaProximidad;
    }

    /**
     * Calcula la distancia entre dos elementos de conexión.
     *
     * Los elementos pueden representar puntos o segmentos.
     * Devuelve cero cuando los segmentos se cruzan o se tocan.
     */
    public double distanciaEntreElementos(
            ElementoConexion a,
            ElementoConexion b
    ) {

        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        boolean aEsPunto =
                a.getTipo() == ElementoConexion.Tipo.PUNTO;

        boolean bEsPunto =
                b.getTipo() == ElementoConexion.Tipo.PUNTO;

        if (aEsPunto && bEsPunto) {
            return Math.hypot(
                    a.getX1() - b.getX1(),
                    a.getY1() - b.getY1()
            );
        }

        if (aEsPunto) {
            return distanciaPuntoSegmento(
                    a.getX1(), a.getY1(),
                    b.getX1(), b.getY1(),
                    b.getX2(), b.getY2()
            );
        }

        if (bEsPunto) {
            return distanciaPuntoSegmento(
                    b.getX1(), b.getY1(),
                    a.getX1(), a.getY1(),
                    a.getX2(), a.getY2()
            );
        }

        return distanciaSegmentoSegmento(a, b);
    }

    /**
     * Comprueba si dos elementos de conexión están suficientemente cerca.
     */
    public boolean elementosEstanCerca(
            ElementoConexion a,
            ElementoConexion b
    ) {
        return distanciaEntreElementos(a, b) <= distanciaProximidad;
    }

    /**
     * Comprueba si algún elemento de dos piezas está suficientemente cerca.
     */
    public boolean piezasEstanCercaPorElementos(
            Pieza a,
            Pieza b
    ) {

        if (a == null || b == null || a == b) {
            return false;
        }

        List<ElementoConexion> elementosA =
                a.obtenerElementosConexion();

        List<ElementoConexion> elementosB =
                b.obtenerElementosConexion();

        for (ElementoConexion elementoA : elementosA) {
            for (ElementoConexion elementoB : elementosB) {

                if (elementosEstanCerca(elementoA, elementoB)) {
                    return true;
                }
            }
        }

        return false;
    }

    private double distanciaPuntoSegmento(
            double px,
            double py,
            double x1,
            double y1,
            double x2,
            double y2
    ) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double longitudCuadrada = dx * dx + dy * dy;

        if (longitudCuadrada == 0) {
            return Math.hypot(px - x1, py - y1);
        }

        double t = ((px - x1) * dx + (py - y1) * dy)
                / longitudCuadrada;

        t = Math.max(0, Math.min(1, t));

        double puntoX = x1 + t * dx;
        double puntoY = y1 + t * dy;

        return Math.hypot(px - puntoX, py - puntoY);
    }

    private double distanciaSegmentoSegmento(
            ElementoConexion a,
            ElementoConexion b
    ) {

        if (segmentosSeCruzan(a, b)) {
            return 0;
        }

        double distancia1 = distanciaPuntoSegmento(
                a.getX1(), a.getY1(),
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2()
        );

        double distancia2 = distanciaPuntoSegmento(
                a.getX2(), a.getY2(),
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2()
        );

        double distancia3 = distanciaPuntoSegmento(
                b.getX1(), b.getY1(),
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2()
        );

        double distancia4 = distanciaPuntoSegmento(
                b.getX2(), b.getY2(),
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2()
        );

        return Math.min(
                Math.min(distancia1, distancia2),
                Math.min(distancia3, distancia4)
        );
    }

    private boolean segmentosSeCruzan(
            ElementoConexion a,
            ElementoConexion b
    ) {

        double orientacion1 = orientacion(
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2(),
                b.getX1(), b.getY1()
        );

        double orientacion2 = orientacion(
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2(),
                b.getX2(), b.getY2()
        );

        double orientacion3 = orientacion(
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2(),
                a.getX1(), a.getY1()
        );

        double orientacion4 = orientacion(
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2(),
                a.getX2(), a.getY2()
        );

        return orientacion1 * orientacion2 <= 0
                && orientacion3 * orientacion4 <= 0
                && seSuperponenEnEje(
                a.getX1(), a.getX2(),
                b.getX1(), b.getX2()
        )
                && seSuperponenEnEje(
                a.getY1(), a.getY2(),
                b.getY1(), b.getY2()
        );
    }

    private double orientacion(
            double ax,
            double ay,
            double bx,
            double by,
            double cx,
            double cy
    ) {
        return (bx - ax) * (cy - ay)
                - (by - ay) * (cx - ax);
    }

    private boolean seSuperponenEnEje(
            double a1,
            double a2,
            double b1,
            double b2
    ) {
        return Math.max(Math.min(a1, a2), Math.min(b1, b2))
                <= Math.min(Math.max(a1, a2), Math.max(b1, b2));
    }
}