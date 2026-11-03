
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Poligono implements Geometria {

    // Vértices definidos en coordenadas locales.
    private final List<Vertice> vertices = new ArrayList<>();

    // Puntos de conexión del polígono.
    private final List<ElementoConexion> elementosConexion =
            new ArrayList<>();

    // Posición del polígono en el lienzo.
    private double x;
    private double y;

    // Escala de dibujo.
    private double escala = 1.0;

    public Poligono(List<Vertice> vertices) {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException(
                    "Un polígono necesita al menos tres vértices."
            );
        }

        for (Vertice vertice : vertices) {
            if (vertice == null) {
                throw new IllegalArgumentException(
                        "Los vértices no pueden ser nulos."
                );
            }

            this.vertices.add(
                    new Vertice(vertice.getX(), vertice.getY())
            );

            elementosConexion.add(
                    new ElementoConexion(
                            ElementoConexion.Tipo.PUNTO,
                            0, 0, 0, 0
                    )
            );
        }
        if (calcularArea() < 0.000001) {
            throw new IllegalArgumentException(
                    "El polígono no tiene una superficie válida."
            );
        }

        if (tieneAutointersecciones()) {
            throw new IllegalArgumentException(
                    "El polígono no puede tener lados que se crucen."
            );
        }
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

        this.x = x;
        this.y = y;
        this.escala = escala;

        for (int i = 0; i < vertices.size(); i++) {
            Vertice vertice = vertices.get(i);

            double px = x + vertice.getX() * escala;
            double py = y + vertice.getY() * escala;

            elementosConexion.get(i).actualizar(
                    px, py, px, py
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
        dibujar(gc, x, y, escala, Color.GRAY);
    }

    @Override
    public void dibujar(
            GraphicsContext gc,
            double x,
            double y,
            double escala,
            Color color
    ) {
        if (gc == null || !Double.isFinite(escala) || escala <= 0) {
            return;
        }

        double[] puntosX = new double[vertices.size()];
        double[] puntosY = new double[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            puntosX[i] = x + vertices.get(i).getX() * escala;
            puntosY[i] = y + vertices.get(i).getY() * escala;
        }

        gc.setFill(color != null ? color : Color.GRAY);

        gc.fillPolygon(
                puntosX,
                puntosY,
                vertices.size()
        );
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
        if (gc == null || !Double.isFinite(escala) || escala <= 0
                || !Double.isFinite(grosor) || grosor <= 0) {
            return;
        }

        double[] puntosX = new double[vertices.size()];
        double[] puntosY = new double[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            puntosX[i] = x + vertices.get(i).getX() * escala;
            puntosY[i] = y + vertices.get(i).getY() * escala;
        }

        gc.setStroke(color != null ? color : Color.BLACK);
        gc.setLineWidth(grosor);

        gc.strokePolygon(
                puntosX,
                puntosY,
                vertices.size()
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

        boolean dentro = false;

        for (int i = 0, j = vertices.size() - 1;
             i < vertices.size();
             j = i++) {

            double xi = x + vertices.get(i).getX() * escala;
            double yi = y + vertices.get(i).getY() * escala;

            double xj = x + vertices.get(j).getX() * escala;
            double yj = y + vertices.get(j).getY() * escala;

            boolean intersecta =
                    ((yi > py) != (yj > py))
                            && (px < (xj - xi) * (py - yi)
                            / (yj - yi) + xi);

            if (intersecta) {
                dentro = !dentro;
            }
        }

        return dentro;
    }

    @Override
    public List<ElementoConexion> obtenerElementosConexion() {
        return Collections.unmodifiableList(elementosConexion);
    }

    public List<Vertice> obtenerVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public int cantidadVertices() {
        return vertices.size();
    }

    // Calcular el área del polígono mediante la fórmula de Shoelace.
    public double calcularArea() {

        double suma = 0;

        for (int i = 0; i < vertices.size(); i++) {

            Vertice actual = vertices.get(i);

            Vertice siguiente = vertices.get(
                    (i + 1) % vertices.size()
            );

            suma += actual.getX() * siguiente.getY()
                    - siguiente.getX() * actual.getY();
        }

        return Math.abs(suma) / 2.0;

    }

    // Determinar si dos segmentos se intersectan.
    private boolean segmentosSeIntersectan(
            Vertice a,
            Vertice b,
            Vertice c,
            Vertice d
    ) {

        double orientacion1 =
                (b.getX() - a.getX()) * (c.getY() - a.getY())
                        - (b.getY() - a.getY()) * (c.getX() - a.getX());

        double orientacion2 =
                (b.getX() - a.getX()) * (d.getY() - a.getY())
                        - (b.getY() - a.getY()) * (d.getX() - a.getX());

        double orientacion3 =
                (d.getX() - c.getX()) * (a.getY() - c.getY())
                        - (d.getY() - c.getY()) * (a.getX() - c.getX());

        double orientacion4 =
                (d.getX() - c.getX()) * (b.getY() - c.getY())
                        - (d.getY() - c.getY()) * (b.getX() - c.getX());

        return ((orientacion1 > 0 && orientacion2 < 0)
                || (orientacion1 < 0 && orientacion2 > 0))
                &&
                ((orientacion3 > 0 && orientacion4 < 0)
                        || (orientacion3 < 0 && orientacion4 > 0));
    }

    // Detectar autointersecciones del polígono.
    private boolean tieneAutointersecciones() {

        int cantidad = vertices.size();

        for (int i = 0; i < cantidad; i++) {

            Vertice a = vertices.get(i);
            Vertice b = vertices.get((i + 1) % cantidad);

            for (int j = i + 1; j < cantidad; j++) {

                // Ignorar segmentos consecutivos.
                if (j == i + 1) {
                    continue;
                }

                // Ignorar el primer y último segmento,
                // porque comparten un vértice.
                if (i == 0 && j == cantidad - 1) {
                    continue;
                }

                Vertice c = vertices.get(j);
                Vertice d = vertices.get((j + 1) % cantidad);

                if (segmentosSeIntersectan(a, b, c, d)) {
                    return true;
                }
            }
        }

        return false;
    }


}

