package cl.tablerolab.model;

public class Pieza {

    private String nombre;
    private double ancho;
    private double largo;
    private int cantidad;

    public Pieza(String nombre, double ancho, double largo, int cantidad) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.largo = largo;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public double getAncho() {
        return ancho;
    }

    public double getLargo() {
        return largo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getArea() {
        return ancho * largo;
    }

    public double getAreaTotal() {
        return getArea() * cantidad;
    }

    @Override
    public String toString() {
        return nombre + " - " +
                ancho + " × " + largo +
                " cm | Cantidad: " + cantidad;
    }
}


