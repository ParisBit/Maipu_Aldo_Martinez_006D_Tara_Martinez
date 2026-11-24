
package cl.tablerolab.service;

import cl.tablerolab.model.CotizacionTablero;

import cl.tablerolab.optimizer.ResultadoOptimizacion;

public class CalculadorCostos {

    public double calcularCostoUtilizado(
            CotizacionTablero cotizacion,
            int tablerosUtilizados
    ) {
        validar(cotizacion, tablerosUtilizados);

        return cotizacion.getPrecioUnitario()
                * tablerosUtilizados;
    }

    public double calcularCostoCompra(
            CotizacionTablero cotizacion,
            int tablerosAComprar
    ) {
        validar(cotizacion, tablerosAComprar);

        return cotizacion.getPrecioUnitario()
                * tablerosAComprar;
    }

    private void validar(
            CotizacionTablero cotizacion,
            int cantidad
    ) {
        if (cotizacion == null) {
            throw new IllegalArgumentException(
                    "Debe existir una cotización."
            );
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de tableros no puede ser negativa."
            );
        }

        if (cotizacion.getPrecioUnitario() < 0) {
            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo."
            );
        }
    }

    public double calcularCostoUtilizado(
            CotizacionTablero cotizacion,
            ResultadoOptimizacion resultado
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "Debe existir un resultado de optimización."
            );
        }

        return calcularCostoUtilizado(
                cotizacion,
                resultado.getCantidadTablerosUtilizados()
        );
    }
}
