
import java.util.List;

public class DetectorColisiones {

    private double escala;

    public DetectorColisiones(double escala) {
        setEscala(escala);
    }

    public void setEscala(double escala) {
        if (!Double.isFinite(escala) || escala <= 0) {
            throw new IllegalArgumentException(
                    "La escala debe ser un número finito mayor que cero."
            );
        }

        this.escala = escala;
    }

    public boolean hayColision(Pieza a, Pieza b) {
        if (a == null || b == null || a == b) {
            return false;
        }

        Geometria geometriaA = a.getGeometria();
        Geometria geometriaB = b.getGeometria();

        if (geometriaA == null || geometriaB == null) {
            return false;
        }

        a.actualizarGeometria(escala);
        b.actualizarGeometria(escala);

        // DETECCIÓN ENTRE CÍRCULOS

        if (geometriaA instanceof Circulo
                && geometriaB instanceof Circulo) {

            Circulo circuloA = (Circulo) geometriaA;
            Circulo circuloB = (Circulo) geometriaB;

            double dx = circuloA.getCentroX() - circuloB.getCentroX();
            double dy = circuloA.getCentroY() - circuloB.getCentroY();

            double distanciaCuadrada = dx * dx + dy * dy;

            double radioA = circuloA.getRadioCm() * escala;
            double radioB = circuloB.getRadioCm() * escala;

            double sumaRadios = radioA + radioB;

            return distanciaCuadrada < sumaRadios * sumaRadios;


        }

        // DETECCIÓN ENTRE CÍRCULO Y RECTÁNGULO

        if (geometriaA instanceof Circulo
                && geometriaB instanceof Rectangulo) {

            return circuloColisionaRectangulo(
                    (Circulo) geometriaA,
                    (Rectangulo) geometriaB,
                    b
            );
        }

        if (geometriaA instanceof Rectangulo
                && geometriaB instanceof Circulo) {

            return circuloColisionaRectangulo(
                    (Circulo) geometriaB,
                    (Rectangulo) geometriaA,
                    a
            );
        }

        List<ElementoConexion> elementosA =
                geometriaA.obtenerElementosConexion();

        List<ElementoConexion> elementosB =
                geometriaB.obtenerElementosConexion();

        // Comprueba si algún vértice de A está dentro de B.
        for (ElementoConexion elemento : elementosA) {
            if (elemento.getTipo() == ElementoConexion.Tipo.PUNTO
                    && geometriaB.contienePunto(
                    elemento.getX1(),
                    elemento.getY1(),
                    b.getX(),
                    b.getY(),
                    escala)) {
                return true;
            }
        }

        // Comprueba si algún vértice de B está dentro de A.
        for (ElementoConexion elemento : elementosB) {
            if (elemento.getTipo() == ElementoConexion.Tipo.PUNTO
                    && geometriaA.contienePunto(
                    elemento.getX1(),
                    elemento.getY1(),
                    a.getX(),
                    a.getY(),
                    escala)) {
                return true;
            }
        }

        // Comprueba si se cruzan segmentos de ambas geometrías.
        for (ElementoConexion elementoA : elementosA) {
            if (elementoA.getTipo() != ElementoConexion.Tipo.SEGMENTO) {
                continue;
            }

            for (ElementoConexion elementoB : elementosB) {
                if (elementoB.getTipo() != ElementoConexion.Tipo.SEGMENTO) {
                    continue;
                }

                if (segmentosSeCruzan(elementoA, elementoB)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean segmentosSeCruzan(
            ElementoConexion a,
            ElementoConexion b
    ) {
        double o1 = orientacion(
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2(),
                b.getX1(), b.getY1()
        );

        double o2 = orientacion(
                a.getX1(), a.getY1(),
                a.getX2(), a.getY2(),
                b.getX2(), b.getY2()
        );

        double o3 = orientacion(
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2(),
                a.getX1(), a.getY1()
        );

        double o4 = orientacion(
                b.getX1(), b.getY1(),
                b.getX2(), b.getY2(),
                a.getX2(), a.getY2()
        );

        if (((o1 > 0 && o2 < 0) || (o1 < 0 && o2 > 0))
                && ((o3 > 0 && o4 < 0) || (o3 < 0 && o4 > 0))) {
            return true;
        }

        return o1 == 0 && estaEnSegmento(a, b.getX1(), b.getY1())
                || o2 == 0 && estaEnSegmento(a, b.getX2(), b.getY2())
                || o3 == 0 && estaEnSegmento(b, a.getX1(), a.getY1())
                || o4 == 0 && estaEnSegmento(b, a.getX2(), a.getY2());
    }

    private double orientacion(
            double x1, double y1,
            double x2, double y2,
            double x3, double y3
    ) {
        return (x2 - x1) * (y3 - y1)
                - (y2 - y1) * (x3 - x1);
    }

    private boolean estaEnSegmento(
            ElementoConexion segmento,
            double x,
            double y
    ) {
        return x >= Math.min(segmento.getX1(), segmento.getX2())
                && x <= Math.max(segmento.getX1(), segmento.getX2())
                && y >= Math.min(segmento.getY1(), segmento.getY2())
                && y <= Math.max(segmento.getY1(), segmento.getY2());
    }

    private boolean circuloColisionaRectangulo(
            Circulo circulo,
            Rectangulo rectangulo,
            Pieza piezaRectangulo
    ) {
        double centroX = circulo.getCentroX();
        double centroY = circulo.getCentroY();
        double radio = circulo.getRadioCm() * escala;

        double izquierda = piezaRectangulo.getX();
        double derecha = izquierda + rectangulo.getAnchoCm() * escala;

        double arriba = piezaRectangulo.getY();
        double abajo = arriba + rectangulo.getAltoCm() * escala;

        // Encontrar el punto más cercano del rectángulo al círculo.
        double puntoX = Math.max(izquierda,
                Math.min(centroX, derecha));

        double puntoY = Math.max(arriba,
                Math.min(centroY, abajo));

        double dx = centroX - puntoX;
        double dy = centroY - puntoY;

        return dx * dx + dy * dy < radio * radio;
    }
}