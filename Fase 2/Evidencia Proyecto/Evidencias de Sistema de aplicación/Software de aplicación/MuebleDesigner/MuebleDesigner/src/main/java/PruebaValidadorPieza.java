
public class PruebaValidadorPieza {

    public static void main(String[] args) {

        ValidadorPieza validador = new ValidadorPieza();

        Pieza piezaValida = new Pieza(100, 100);

        comprobar(
                "Pieza válida",
                validador.esValida(piezaValida),
                true
        );

        comprobar(
                "Pieza nula",
                validador.esValida(null),
                false
        );

        Pieza piezaAnchoCero = new Pieza(100, 100);
        piezaAnchoCero.setAnchoCm(0);

        comprobar(
                "Ancho igual a cero",
                validador.esValida(piezaAnchoCero),
                false
        );

        Pieza piezaPosicionInvalida =
                new Pieza(Double.NaN, 100);

        comprobar(
                "Posición inválida",
                validador.esValida(piezaPosicionInvalida),
                false
        );
    }

    private static void comprobar(
            String prueba,
            boolean resultadoObtenido,
            boolean resultadoEsperado
    ) {
        if (resultadoObtenido == resultadoEsperado) {
            System.out.println("[OK] " + prueba);
        } else {
            System.out.println("[ERROR] " + prueba
                    + " | Esperado: " + resultadoEsperado
                    + " | Obtenido: " + resultadoObtenido);
        }
    }
}