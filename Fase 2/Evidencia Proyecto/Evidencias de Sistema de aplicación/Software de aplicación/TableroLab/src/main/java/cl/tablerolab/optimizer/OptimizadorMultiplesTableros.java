
package cl.tablerolab.optimizer;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Tablero;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class OptimizadorMultiplesTableros {

    private static final int MAX_TABLEROS = 5;

    private final OptimizadorCortes optimizador;

    public OptimizadorMultiplesTableros() {
        this(new OptimizadorCortesMaxRects());
    }

    public OptimizadorMultiplesTableros(
            OptimizadorCortes optimizador
    ) {
        if (optimizador == null) {
            throw new IllegalArgumentException(
                    "El optimizador no puede ser nulo."
            );
        }

        this.optimizador = optimizador;
    }

    public ResultadoOptimizacion optimizar(
            Tablero tablero,
            List<Pieza> piezas
    ) {
        return optimizar(tablero, piezas, MAX_TABLEROS);
    }

    public ResultadoOptimizacion optimizar(
            Tablero tablero,
            List<Pieza> piezas,
            int maxTableros
    ) {

        if (tablero == null) {
            throw new IllegalArgumentException(
                    "El tablero no puede ser nulo."
            );
        }

        if (maxTableros < 1 || maxTableros > MAX_TABLEROS) {
            throw new IllegalArgumentException(
                    "La cantidad de tableros debe estar entre 1 y 5."
            );
        }

        List<Pieza> pendientes = new ArrayList<>();

        if (piezas != null) {
            for (Pieza pieza : piezas) {

                if (pieza == null || pieza.getCantidad() <= 0) {
                    continue;
                }

                // Cada entrada recibe una instancia independiente.
                pendientes.add(
                        new Pieza(
                                pieza.getNombre(),
                                pieza.getAncho(),
                                pieza.getLargo(),
                                pieza.getCantidad()
                        )
                );
            }
        }

        List<List<PiezaColocada>> piezasPorTablero =
                new ArrayList<>();

        for (int numeroTablero = 0;
             numeroTablero < maxTableros && !pendientes.isEmpty();
             numeroTablero++) {

            List<PiezaColocada> colocadas =
                    optimizador.optimizar(tablero, pendientes);

            if (colocadas == null || colocadas.isEmpty()) {
                break;
            }

            piezasPorTablero.add(
                    new ArrayList<>(colocadas)
            );

            // Contar cuántas unidades de cada pieza se colocaron.
            Map<Pieza, Integer> cantidadesColocadas =
                    new IdentityHashMap<>();

            for (PiezaColocada colocada : colocadas) {

                if (colocada == null
                        || colocada.getPiezaOriginal() == null) {
                    continue;
                }

                Pieza original = colocada.getPiezaOriginal();

                cantidadesColocadas.merge(
                        original,
                        1,
                        Integer::sum
                );
            }

            // Reconstruir la lista con las cantidades restantes.
            List<Pieza> nuevasPendientes = new ArrayList<>();

            for (Pieza pieza : pendientes) {

                int cantidadColocada =
                        cantidadesColocadas.getOrDefault(pieza, 0);

                int cantidadRestante =
                        pieza.getCantidad() - cantidadColocada;

                if (cantidadRestante > 0) {

                    nuevasPendientes.add(
                            new Pieza(
                                    pieza.getNombre(),
                                    pieza.getAncho(),
                                    pieza.getLargo(),
                                    cantidadRestante
                            )
                    );
                }
            }

            pendientes = nuevasPendientes;
        }

        return new ResultadoOptimizacion(
                piezasPorTablero,
                pendientes
        );
    }
}
