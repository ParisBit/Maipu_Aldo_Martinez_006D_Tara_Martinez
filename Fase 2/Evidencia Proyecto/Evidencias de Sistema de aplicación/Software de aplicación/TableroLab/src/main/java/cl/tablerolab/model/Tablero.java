    package cl.tablerolab.model;

    public class Tablero {

        private final double ancho;
        private final double largo;
        private final double espesor;
        private final double anchoCorte;

        public Tablero(
                double ancho,
                double largo,
                double espesor,
                double anchoCorte
        ) {
            this.ancho = ancho;
            this.largo = largo;
            this.espesor = espesor;
            this.anchoCorte = anchoCorte;
        }

        public double getAncho() {
            return ancho;
        }

        public double getLargo() {
            return largo;
        }

        public double getEspesor() {
            return espesor;
        }

        public double getAnchoCorte() {
            return anchoCorte;
        }

        public double getArea() {
            return ancho * largo;
        }
    }

