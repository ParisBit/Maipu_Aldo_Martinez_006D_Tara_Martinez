
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DibujadorPiezas {

    public void dibujarPieza(
            GraphicsContext gc,
            Pieza pieza,
            double escala
    ) {

        if (gc == null
                || pieza == null
                || pieza.getGeometria() == null) {
            return;
        }

        Geometria geometria = pieza.getGeometria();

        // Dibujar el cuerpo de la figura.
        geometria.dibujar(
                gc,
                pieza.getX(),
                pieza.getY(),
                escala,
                pieza.getColor()
        );

        // Determinar el color y grosor del contorno.
        Color colorContorno;
        double grosorContorno;

        if (pieza.isEnColision()) {
            colorContorno = Color.RED;
            grosorContorno = 4;

        } else if (pieza.isCerca()) {
            colorContorno = Color.YELLOW;
            grosorContorno = 4;

        } else if (pieza.isSeleccionada()) {
            colorContorno = Color.BLUE;
            grosorContorno = 4;

        } else {
            colorContorno = Color.BLACK;
            grosorContorno = 1;
        }

        // Dibujar el contorno mediante la geometría.
        geometria.dibujarContorno(
                gc,
                pieza.getX(),
                pieza.getY(),
                escala,
                colorContorno,
                grosorContorno
        );

        // Dibujar los puntos de conexión.
        List<ElementoConexion> elementos =
                pieza.obtenerElementosConexion();

        for (ElementoConexion elemento : elementos) {

            if (elemento == null
                    || elemento.getTipo()
                    != ElementoConexion.Tipo.PUNTO) {
                continue;
            }

            double x = elemento.getX1();
            double y = elemento.getY1();
            double radio = 5;

            gc.setFill(Color.BLACK);

            gc.fillOval(
                    x - radio,
                    y - radio,
                    radio * 2,
                    radio * 2
            );
        }
    }
}