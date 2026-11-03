
public class Vertice {

    private double x;
    private double y;
    private boolean cerca;

    public Vertice(double x, double y) {
        setX(x);
        setY(y);
        this.cerca = false;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        if (!Double.isFinite(x)) {
            throw new IllegalArgumentException(
                    "La coordenada X debe ser un número finito."
            );
        }

        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        if (!Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "La coordenada Y debe ser un número finito."
            );
        }

        this.y = y;
    }

    public boolean isCerca() {
        return cerca;
    }

    public void setCerca(boolean cerca) {
        this.cerca = cerca;
    }
}
