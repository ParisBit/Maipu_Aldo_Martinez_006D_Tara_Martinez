
public class Vertice {

    double x;
    double y;

    boolean cerca;

    public Vertice(double x, double y) {
        this.x = x;
        this.y = y;
        this.cerca = false;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isCerca() {
        return cerca;
    }

    public void setCerca(boolean cerca) {
        this.cerca = cerca;
    }
}