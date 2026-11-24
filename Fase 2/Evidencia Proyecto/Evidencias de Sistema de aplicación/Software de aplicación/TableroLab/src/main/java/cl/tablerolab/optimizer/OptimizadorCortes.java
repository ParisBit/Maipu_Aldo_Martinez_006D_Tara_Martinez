package cl.tablerolab.optimizer;

import cl.tablerolab.model.Tablero;
import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;

import java.util.List;

public interface OptimizadorCortes {

    List<PiezaColocada> optimizar(
            Tablero tablero,
            List<Pieza> piezas
    );
}

