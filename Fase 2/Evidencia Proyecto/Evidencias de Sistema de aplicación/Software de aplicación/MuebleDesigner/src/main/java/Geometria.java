
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Geometria {

    void actualizar(double x, double y, double escala);

    void dibujar(
            GraphicsContext gc,
            double x,
            double y,
            double escala
    );

    default void dibujar(
            GraphicsContext gc,
            double x,
            double y,
            double escala,
            Color color
    ) {
        dibujar(gc, x, y, escala);
    }

    default void dibujarContorno(
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

        dibujar(gc, x, y, escala);
    }

    /**
     * Indica si un punto del lienzo está dentro de esta geometría.
     * Cada geometría debe implementar su propia comprobación.
     */
    default boolean contienePunto(
            double px,
            double py,
            double x,
            double y,
            double escala
    ) {
        return false;
    }

    List<ElementoConexion> obtenerElementosConexion();
}