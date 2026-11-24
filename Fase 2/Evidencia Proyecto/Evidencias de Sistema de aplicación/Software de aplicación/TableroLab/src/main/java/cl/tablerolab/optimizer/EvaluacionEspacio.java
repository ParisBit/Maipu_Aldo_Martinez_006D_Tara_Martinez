
package cl.tablerolab.optimizer;

public class EvaluacionEspacio {

    private final EspacioLibre espacio;
    private final double x;
    private final double y;
    private final double anchoPieza;
    private final double largoPieza;
    private final boolean rotada;
    private final double puntuacion;

    public EvaluacionEspacio(
            EspacioLibre espacio,
            double x,
            double y,
            double anchoPieza,
            double largoPieza,
            boolean rotada,
            double puntuacion
    ) {
        this.espacio = espacio;
        this.x = x;
        this.y = y;
        this.anchoPieza = anchoPieza;
        this.largoPieza = largoPieza;
        this.rotada = rotada;
        this.puntuacion = puntuacion;
    }

    public EspacioLibre getEspacio() {
        return espacio;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAnchoPieza() {
        return anchoPieza;
    }

    public double getLargoPieza() {
        return largoPieza;
    }

    public boolean isRotada() {
        return rotada;
    }

    public double getPuntuacion() {
        return puntuacion;
    }
}
