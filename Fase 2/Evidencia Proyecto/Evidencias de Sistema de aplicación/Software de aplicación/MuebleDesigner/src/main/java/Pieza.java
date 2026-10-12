
import java.util.List;
import javafx.scene.paint.Color;

public class Pieza {

    private double x;
    private double y;

    private double anchoCm;
    private double altoCm;
    private double largoCm;

    private Color color;

    private boolean seleccionada;
    private boolean enColision;
    private boolean cerca;

    private Geometria geometria;

    public Pieza(double x, double y) {
        validarCoordenada(x, "x");
        validarCoordenada(y, "y");

        this.x = x;
        this.y = y;

        this.anchoCm = 8;
        this.altoCm = 2;
        this.largoCm = 2;

        this.color = Color.LIGHTGRAY;

        this.seleccionada = false;
        this.enColision = false;
        this.cerca = false;

        this.geometria = new Rectangulo(anchoCm, altoCm);
    }

    public void actualizarGeometria(double escala) {
        geometria.actualizar(x, y, escala);
    }

    public List<ElementoConexion> obtenerElementosConexion() {
        return geometria.obtenerElementosConexion();
    }

    /**
     * Método de compatibilidad para obtener vértices de rectángulos.
     */
    public Vertice obtenerVertice(int indice) {
        if (geometria instanceof Rectangulo) {
            return ((Rectangulo) geometria).obtenerVertice(indice);
        }

        throw new IllegalStateException(
                "Esta geometría no permite obtener vértices de rectángulo."
        );
    }

    public double getAnchoCm() {
        return anchoCm;
    }

    public void setAnchoCm(double anchoCm) {
        validarDimension(anchoCm, "anchoCm");
        this.anchoCm = anchoCm;

        if (geometria instanceof Rectangulo) {
            ((Rectangulo) geometria).setAnchoCm(anchoCm);
        }
    }

    public double getAltoCm() {
        return altoCm;
    }

    public void setAltoCm(double altoCm) {
        validarDimension(altoCm, "altoCm");
        this.altoCm = altoCm;

        if (geometria instanceof Rectangulo) {
            ((Rectangulo) geometria).setAltoCm(altoCm);
        }
    }

    public double getLargoCm() {
        return largoCm;
    }

    public void setLargoCm(double largoCm) {
        validarDimension(largoCm, "largoCm");
        this.largoCm = largoCm;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        validarCoordenada(x, "x");
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        validarCoordenada(y, "y");
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                    "El color de la pieza no puede ser nulo."
            );
        }

        this.color = color;
    }

    public boolean isSeleccionada() {
        return seleccionada;
    }

    public void setSeleccionada(boolean seleccionada) {
        this.seleccionada = seleccionada;
    }

    public boolean isEnColision() {
        return enColision;
    }

    public void setEnColision(boolean enColision) {
        this.enColision = enColision;
    }

    public boolean isCerca() {
        return cerca;
    }

    public void setCerca(boolean cerca) {
        this.cerca = cerca;
    }

    public Geometria getGeometria() {
        return geometria;
    }

    public void setGeometria(Geometria geometria) {
        if (geometria == null) {
            throw new IllegalArgumentException(
                    "La geometría de la pieza no puede ser nula."
            );
        }

        this.geometria = geometria;
    }

    private void validarDimension(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    nombre + " debe ser un número finito mayor que cero."
            );
        }
    }

    private void validarCoordenada(double valor, String nombre) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    "La coordenada " + nombre + " debe ser finita."
            );
        }
    }
}