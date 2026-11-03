
public class ValidadorDimensiones {

    public boolean sonValidas(double ancho, double alto, double largo) {

        return Double.isFinite(ancho)
                && Double.isFinite(alto)
                && Double.isFinite(largo)
                && ancho > 0
                && alto > 0
                && largo > 0;
    }
}