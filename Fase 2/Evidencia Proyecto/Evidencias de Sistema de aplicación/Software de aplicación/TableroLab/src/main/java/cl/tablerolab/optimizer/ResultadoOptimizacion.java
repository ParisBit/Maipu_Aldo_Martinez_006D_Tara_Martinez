
package cl.tablerolab.optimizer;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadoOptimizacion {

    private final List<List<PiezaColocada>> piezasPorTablero;
    private final List<Pieza> piezasPendientes;

    public ResultadoOptimizacion(
            List<List<PiezaColocada>> piezasPorTablero,
            List<Pieza> piezasPendientes
    ) {

        List<List<PiezaColocada>> copiaTableros =
                new ArrayList<>();

        if (piezasPorTablero != null) {

            for (List<PiezaColocada> piezas : piezasPorTablero) {

                copiaTableros.add(
                        Collections.unmodifiableList(
                                new ArrayList<>(piezas)
                        )
                );
            }
        }

        this.piezasPorTablero =
                Collections.unmodifiableList(copiaTableros);

        this.piezasPendientes =
                piezasPendientes == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                        new ArrayList<>(piezasPendientes)
                );
    }

    public List<List<PiezaColocada>> getPiezasPorTablero() {
        return piezasPorTablero;
    }

    public List<Pieza> getPiezasPendientes() {
        return piezasPendientes;
    }

    public int getCantidadTablerosUtilizados() {
        return piezasPorTablero.size();
    }

    public int getTotalUnidadesColocadas() {

        int total = 0;

        for (List<PiezaColocada> piezas : piezasPorTablero) {
            total += piezas.size();
        }

        return total;
    }

    public int getTotalUnidadesPendientes() {

        int total = 0;

        for (Pieza pieza : piezasPendientes) {

            if (pieza != null && pieza.getCantidad() > 0) {
                total += pieza.getCantidad();
            }
        }

        return total;
    }

    public int getTotalUnidadesSolicitadas() {

        return getTotalUnidadesColocadas()
                + getTotalUnidadesPendientes();
    }
}
