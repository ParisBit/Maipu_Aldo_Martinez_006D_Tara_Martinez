
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangulo implements Geometria {

    private double anchoCm;
    private double altoCm;

    private final Vertice[] vertices = new Vertice[4];
    private final List<ElementoConexion> elementosConexion = new ArrayList<>();

    public Rectangulo(double anchoCm, double altoCm) {
        setAnchoCm(anchoCm);
        setAltoCm(altoCm);

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vertice(0, 0);
        }

        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.PUNTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.PUNTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.PUNTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.PUNTO, 0, 0, 0, 0)
        );

        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.SEGMENTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.SEGMENTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.SEGMENTO, 0, 0, 0, 0)
        );
        elementosConexion.add(
                new ElementoConexion(ElementoConexion.Tipo.SEGMENTO, 0, 0, 0, 0)
        );
    }

    @Override
    public void actualizar(double x, double y, double escala) {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(escala)
                || escala <= 0) {
            throw new IllegalArgumentException(
                    "La posición y la escala deben ser válidas."
            );
        }

        double ancho = anchoCm * escala;
        double alto = altoCm * escala;

        vertices[0].setX(x);
        vertices[0].setY(y);

        vertices[1].setX(x + ancho);
        vertices[1].setY(y);

        vertices[2].setX(x + ancho);
        vertices[2].setY(y + alto);

        vertices[3].setX(x);
        vertices[3].setY(y + alto);

        for (int i = 0; i < 4; i++) {
            Vertice vertice = vertices[i];

            elementosConexion.get(i).actualizar(
                    vertice.getX(),
                    vertice.getY(),
                    vertice.getX(),
                    vertice.getY()
            );
        }

        for (int i = 0; i < 4; i++) {
            Vertice inicio = vertices[i];
            Vertice fin = vertices[(i + 1) % 4];

            elementosConexion.get(i + 4).actualizar(
                    inicio.getX(),
                    inicio.getY(),
                    fin.getX(),
                    fin.getY()
            );
        }
    }

    @Override
    public void dibujar(
            GraphicsContext gc,
            double x,
            double y,
            double escala
    ) {
        if (gc == null) {
            return;
        }

        gc.fillRect(
                x,
                y,
                anchoCm * escala,
                altoCm * escala
        );

        dibujarContorno(gc, x, y, escala, Color.BLACK, 1);
    }

    @Override
    public void dibujarContorno(
            GraphicsContext gc,
            double x,
            double y,
            double escala,
            Color color,
            double grosor
    ) {
        if (gc == null) {
            return;
        }

        gc.setStroke(color != null ? color : Color.BLACK);
        gc.setLineWidth(grosor);

        gc.strokeRect(
                x,
                y,
                anchoCm * escala,
                altoCm * escala
        );
    }

    @Override
    public boolean contienePunto(
            double px,
            double py,
            double x,
            double y,
            double escala
    ) {
        if (!Double.isFinite(px)
                || !Double.isFinite(py)
                || !Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(escala)
                || escala <= 0) {
            return false;
        }

        double ancho = anchoCm * escala;
        double alto = altoCm * escala;

        return px >= x
                && px <= x + ancho
                && py >= y
                && py <= y + alto;
    }

    @Override
    public List<ElementoConexion> obtenerElementosConexion() {
        return Collections.unmodifiableList(elementosConexion);
    }

    public Vertice obtenerVertice(int indice) {
        if (indice < 0 || indice >= vertices.length) {
            throw new IndexOutOfBoundsException(
                    "Índice de vértice inválido: " + indice
            );
        }

        return vertices[indice];
    }

    public double getAnchoCm() {
        return anchoCm;
    }

    public void setAnchoCm(double anchoCm) {
        if (!Double.isFinite(anchoCm) || anchoCm <= 0) {
            throw new IllegalArgumentException(
                    "El ancho debe ser un número finito mayor que cero."
            );
        }

        this.anchoCm = anchoCm;
    }

    public double getAltoCm() {
        return altoCm;
    }

    public void setAltoCm(double altoCm) {
        if (!Double.isFinite(altoCm) || altoCm <= 0) {
            throw new IllegalArgumentException(
                    "El alto debe ser un número finito mayor que cero."
            );
        }

        this.altoCm = altoCm;
    }
}