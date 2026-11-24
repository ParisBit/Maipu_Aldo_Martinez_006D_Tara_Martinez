
package cl.tablerolab.model;

public class Retazo {

    private final double x;
    private final double y;
    private final double ancho;
    private final double largo;
    private final double espesor;
    private final int numeroTablero;

    private final EstadoRetazo estado;

    private static final double DIMENSION_MINIMA = 10.0;

    // Constructor compatible con el código existente.
    public Retazo(
            double x,
            double y,
            double ancho,
            double largo,
            double espesor
    ) {
        this(x, y, ancho, largo, espesor, 1);
    }

    // Constructor con identificación del tablero.
    public Retazo(
            double x,
            double y,
            double ancho,
            double largo,
            double espesor,
            int numeroTablero
    ) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.largo = largo;
        this.espesor = espesor;
        this.numeroTablero = numeroTablero;

        this.estado =
                ancho >= DIMENSION_MINIMA
                        && largo >= DIMENSION_MINIMA
                        ? EstadoRetazo.APROVECHABLE
                        : EstadoRetazo.NO_APROVECHABLE;
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

    public double getEspesor() {
        return espesor;
    }

    public int getNumeroTablero() {
        return numeroTablero;
    }

    public double getArea() {
        return ancho * largo;
    }

    public EstadoRetazo getEstado() {
        return estado;
    }

    public boolean isAprovechable() {
        return estado == EstadoRetazo.APROVECHABLE;
    }
}
