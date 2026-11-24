
package cl.tablerolab.model;

public class ResumenCostos {

    private final int tablerosUtilizados;
    private final int tablerosAComprar;
    private final double precioUnitario;
    private final double costoUtilizado;
    private final double costoCompra;
    private final double diferencia;

    public ResumenCostos(
            int tablerosUtilizados,
            int tablerosAComprar,
            double precioUnitario
    ) {

        if (tablerosUtilizados < 0 || tablerosAComprar < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de tableros no puede ser negativa."
            );
        }

        if (precioUnitario < 0) {
            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo."
            );
        }

        if (tablerosAComprar < tablerosUtilizados) {
            throw new IllegalArgumentException(
                    "La cantidad a comprar no puede ser inferior "
                            + "a los tableros utilizados."
            );
        }

        this.tablerosUtilizados = tablerosUtilizados;
        this.tablerosAComprar = tablerosAComprar;
        this.precioUnitario = precioUnitario;

        this.costoUtilizado =
                tablerosUtilizados * precioUnitario;

        this.costoCompra =
                tablerosAComprar * precioUnitario;

        this.diferencia =
                costoCompra - costoUtilizado;
    }

    public int getTablerosUtilizados() {
        return tablerosUtilizados;
    }

    public int getTablerosAComprar() {
        return tablerosAComprar;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getCostoUtilizado() {
        return costoUtilizado;
    }

    public double getCostoCompra() {
        return costoCompra;
    }

    public double getDiferencia() {
        return diferencia;
    }
}
