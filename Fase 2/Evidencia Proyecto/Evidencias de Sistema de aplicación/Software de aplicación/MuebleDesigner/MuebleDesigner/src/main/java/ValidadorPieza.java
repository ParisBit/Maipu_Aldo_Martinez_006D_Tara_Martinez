
public class ValidadorPieza {

    private final ValidadorDimensiones validadorDimensiones =
            new ValidadorDimensiones();

    public boolean esValida(Pieza pieza) {

        if (pieza == null) {
            return false;
        }

        if (!Double.isFinite(pieza.getX())
                || !Double.isFinite(pieza.getY())) {
            return false;
        }

        if (!validadorDimensiones.sonValidas(
                pieza.getAnchoCm(),
                pieza.getAltoCm(),
                pieza.getLargoCm())) {
            return false;
        }

        if (pieza.getColor() == null
                || pieza.getGeometria() == null) {
            return false;
        }

        return true;
    }
}
