
package cl.tablerolab.model;

public class CotizacionTablero {

    private String proveedor;
    private String enlaceProveedor;
    private String material;
    private String descripcion;
    private double espesor;
    private double ancho;
    private double largo;
    private double precioUnitario;

    public CotizacionTablero(
            String proveedor,
            String enlaceProveedor,
            String material,
            String descripcion,
            double espesor,
            double ancho,
            double largo,
            double precioUnitario
    ) {
        this.proveedor = proveedor;
        this.enlaceProveedor = enlaceProveedor;
        this.material = material;
        this.descripcion = descripcion;
        this.espesor = espesor;
        this.ancho = ancho;
        this.largo = largo;
        this.precioUnitario = precioUnitario;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getEnlaceProveedor() {
        return enlaceProveedor;
    }

    public String getMaterial() {
        return material;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getEspesor() {
        return espesor;
    }

    public double getAncho() {
        return ancho;
    }

    public double getLargo() {
        return largo;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getAreaTablero() {
        return ancho * largo;
    }
}
