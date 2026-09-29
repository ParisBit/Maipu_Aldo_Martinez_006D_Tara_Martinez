public interface Geometria {

    void actualizar(double x, double y, double escala);

    void dibujar(
            javafx.scene.canvas.GraphicsContext gc,
            double x,
            double y,
            double escala
    );

}
