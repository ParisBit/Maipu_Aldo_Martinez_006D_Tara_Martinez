public class CalculadoraGeometrica {

    // Calcular el área frontal en cm².
    public static double calcularArea(Pieza pieza) {

        if (pieza == null) {
            throw new IllegalArgumentException(
                    "La pieza no puede ser nula."
            );
        }

        Geometria geometria = pieza.getGeometria();

        if (geometria instanceof Rectangulo) {

            return pieza.getAnchoCm() * pieza.getAltoCm();

        } else if (geometria instanceof Circulo) {

            Circulo circulo = (Circulo) geometria;

            double radio = circulo.getRadioCm();

            return Math.PI * radio * radio;

        } else if (geometria instanceof Poligono) {

            Poligono poligono = (Poligono) geometria;

            return poligono.calcularArea();

        }

        throw new IllegalArgumentException(
                "Tipo de geometría no compatible."
        );
    }

    // Calcular el volumen en cm³.
    public static double calcularVolumen(Pieza pieza) {

        double area = calcularArea(pieza);

        return area * pieza.getLargoCm();
    }

    // Convertir cm² a m².
    public static double convertirCm2AM2(double areaCm2) {

        return areaCm2 / 10000.0;
    }

    // Convertir cm³ a m³.
    public static double convertirCm3AM3(double volumenCm3) {

        return volumenCm3 / 1000000.0;
    }
}