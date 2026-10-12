
public class ElementoConexion {

    public enum Tipo {
        PUNTO,
        SEGMENTO
    }

    private final Tipo tipo;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public ElementoConexion(
            Tipo tipo,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo de elemento no puede ser nulo."
            );
        }

        validarCoordenadas(x1, y1, x2, y2);

        this.tipo = tipo;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public void actualizar(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        validarCoordenadas(x1, y1, x2, y2);

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    private void validarCoordenadas(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        if (!Double.isFinite(x1)
                || !Double.isFinite(y1)
                || !Double.isFinite(x2)
                || !Double.isFinite(y2)) {

            throw new IllegalArgumentException(
                    "Las coordenadas deben ser números finitos."
            );
        }
    }
}