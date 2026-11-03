import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circulo implements Geometria {

    private double radioCm;

    private double centroX;
    private double centroY;

    private final boolean[] puntosCercanos = new boolean[4];

    private final List<ElementoConexion> elementosConexion =
            new ArrayList<>();

    public Circulo(double radioCm) {
        setRadioCm(radioCm);

        // Cuatro puntos cardinales de conexión.
        for (int i = 0; i < 4; i++) {
            elementosConexion.add(
                    new ElementoConexion(
                            ElementoConexion.Tipo.PUNTO,
                            0, 0, 0, 0
                    )
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

        double radio = radioCm * escala;

        centroX = x + radio;
        centroY = y + radio;

        // Punto superior.
        elementosConexion.get(0).actualizar(
                centroX, centroY - radio,
                centroX, centroY - radio
        );

// Punto derecho.
        elementosConexion.get(1).actualizar(
                centroX + radio, centroY,
                centroX + radio, centroY
        );

// Punto inferior.
        elementosConexion.get(2).actualizar(
                centroX, centroY + radio,
                centroX, centroY + radio
        );

// Punto izquierdo.
        elementosConexion.get(3).actualizar(
                centroX - radio, centroY,
                centroX - radio, centroY
        );
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
        if (gc == null) {
            return;
        }

        double diametro = radioCm * 2 * escala;

        gc.setFill(color != null ? color : Color.GRAY);

        gc.fillOval(
                x,
                y,
                diametro,
                diametro
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
        if (gc == null) {
            return;
        }

        double diametro = radioCm * 2 * escala;

        gc.setStroke(color != null ? color : Color.BLACK);
        gc.setLineWidth(grosor);

        gc.strokeOval(
                x,
                y,
                diametro,
                diametro
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

        double radio = radioCm * escala;

        double centroXLocal = x + radio;
        double centroYLocal = y + radio;

        double dx = px - centroXLocal;
        double dy = py - centroYLocal;

        return dx * dx + dy * dy <= radio * radio;
    }

    @Override
    public List<ElementoConexion> obtenerElementosConexion() {
        return Collections.unmodifiableList(elementosConexion);
    }

    public double getRadioCm() {
        return radioCm;
    }

    public void setRadioCm(double radioCm) {
        if (!Double.isFinite(radioCm) || radioCm <= 0) {
            throw new IllegalArgumentException(
                    "El radio debe ser un número finito mayor que cero."
            );
        }

        this.radioCm = radioCm;
    }

    public double getDiametroCm() {
        return radioCm * 2;
    }

    public double getCentroX() {
        return centroX;
    }

    public double getCentroY() {
        return centroY;
    }

    public void marcarPuntoCercano(int indice, boolean cercano) {
        if (indice >= 0 && indice < puntosCercanos.length) {
            puntosCercanos[indice] = cercano;
        }
    }

    public boolean esPuntoCercano(int indice) {
        if (indice < 0 || indice >= puntosCercanos.length) {
            return false;
        }

        return puntosCercanos[indice];
    }

    public void restablecerProximidadPuntos() {
        for (int i = 0; i < puntosCercanos.length; i++) {
            puntosCercanos[i] = false;
        }
    }
}
