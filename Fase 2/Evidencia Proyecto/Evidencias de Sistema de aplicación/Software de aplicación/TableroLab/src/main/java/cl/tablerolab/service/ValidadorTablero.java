
package cl.tablerolab.service;

import cl.tablerolab.model.Tablero;

public class ValidadorTablero {

    public String validarConfiguracion(Tablero tablero) {

        if (tablero == null) {
            return "El tablero no puede ser nulo.";
        }

        if (!Double.isFinite(tablero.getAncho()) ||
                !Double.isFinite(tablero.getLargo()) ||
                tablero.getAncho() <= 0 ||
                tablero.getLargo() <= 0) {
            return "Las dimensiones del tablero deben ser válidas y mayores que cero.";
        }

        if (!Double.isFinite(tablero.getEspesor()) ||
                tablero.getEspesor() <= 0) {
            return "El espesor debe ser válido y mayor que cero.";
        }

        if (!Double.isFinite(tablero.getAnchoCorte()) ||
                tablero.getAnchoCorte() < 0) {
            return "El ancho de corte debe ser válido y no negativo.";
        }

        return null;
    }

    public boolean piezaCabe(
            double anchoTablero,
            double largoTablero,
            double anchoPieza,
            double largoPieza
    ) {
        if (!Double.isFinite(anchoTablero) ||
                !Double.isFinite(largoTablero) ||
                !Double.isFinite(anchoPieza) ||
                !Double.isFinite(largoPieza) ||
                anchoTablero <= 0 ||
                largoTablero <= 0 ||
                anchoPieza <= 0 ||
                largoPieza <= 0) {
            return false;
        }

        boolean cabeNormal =
                anchoPieza <= anchoTablero &&
                        largoPieza <= largoTablero;

        boolean cabeGirado =
                largoPieza <= anchoTablero &&
                        anchoPieza <= largoTablero;

        return cabeNormal || cabeGirado;
    }
}
