package cl.tablerolab.optimizer;

public class EspacioLibre {

    private final double x;
    private final double y;
    private final double ancho;
    private final double largo;

    public EspacioLibre(
            double x,
            double y,
            double ancho,
            double largo
    ) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.largo = largo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAncho() {
        return ancho;
    }

    public double getLargo() {
        return largo;
    }

    public double getArea() {
        return ancho * largo;
    }
}

