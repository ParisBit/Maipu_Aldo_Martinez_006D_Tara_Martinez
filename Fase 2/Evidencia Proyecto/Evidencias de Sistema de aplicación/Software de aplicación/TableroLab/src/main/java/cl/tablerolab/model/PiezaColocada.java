    package cl.tablerolab.model;

public class PiezaColocada {

    private final Pieza piezaOriginal;

    private final double x;
    private final double y;

    private final double ancho;
    private final double largo;

    private final boolean rotada;

    public PiezaColocada(
            Pieza piezaOriginal,
            double x,
            double y,
            double ancho,
            double largo,
            boolean rotada
    ) {
        this.piezaOriginal = piezaOriginal;
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.largo = largo;
        this.rotada = rotada;
    }

    public Pieza getPiezaOriginal() {
        return piezaOriginal;
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

    public boolean isRotada() {
        return rotada;
    }
}


