import java.util.ArrayList;
import java.util.List;

public class PruebaCalculadora {

    public static void main(String[] args) {

        // PRUEBA 1: RECTÁNGULO
        Pieza rectangulo = new Pieza(0, 0);
        rectangulo.setAnchoCm(8);
        rectangulo.setAltoCm(2);
        rectangulo.setLargoCm(2);

        System.out.println("RECTÁNGULO");
        System.out.println("Área: "
                + CalculadoraGeometrica.calcularArea(rectangulo)
                + " cm²");
        System.out.println("Volumen: "
                + CalculadoraGeometrica.calcularVolumen(rectangulo)
                + " cm³");


        // PRUEBA 2: CÍRCULO
        Pieza circulo = new Pieza(0, 0);
        circulo.setGeometria(new Circulo(5));
        circulo.setLargoCm(2);

        System.out.println("\nCÍRCULO");
        System.out.println("Área: "
                + CalculadoraGeometrica.calcularArea(circulo)
                + " cm²");
        System.out.println("Volumen: "
                + CalculadoraGeometrica.calcularVolumen(circulo)
                + " cm³");


        // PRUEBA 3: POLÍGONO
        List<Vertice> vertices = new ArrayList<>();

        vertices.add(new Vertice(0, 0));
        vertices.add(new Vertice(4, 0));
        vertices.add(new Vertice(4, 4));
        vertices.add(new Vertice(0, 4));

        Pieza poligono = new Pieza(0, 0);
        poligono.setGeometria(new Poligono(vertices));
        poligono.setLargoCm(2);

        System.out.println("\nPOLÍGONO");
        System.out.println("Área: "
                + CalculadoraGeometrica.calcularArea(poligono)
                + " cm²");
        System.out.println("Volumen: "
                + CalculadoraGeometrica.calcularVolumen(poligono)
                + " cm³");
    }
}