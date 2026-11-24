
package cl.tablerolab.optimizer;

import cl.tablerolab.model.Tablero;
import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.service.ValidadorTablero;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OptimizadorCortesSimple implements OptimizadorCortes {

    private final ValidadorTablero validador =
            new ValidadorTablero();

    @Override
    public List<PiezaColocada> optimizar(
            Tablero tablero,
            List<Pieza> piezas
    ) {

        List<PiezaColocada> resultado = new ArrayList<>();

        if (tablero == null || piezas == null) {
            return resultado;
        }

        if (!Double.isFinite(tablero.getAncho()) ||
                !Double.isFinite(tablero.getLargo()) ||
                !Double.isFinite(tablero.getAnchoCorte()) ||
                tablero.getAncho() <= 0 ||
                tablero.getLargo() <= 0 ||
                tablero.getAnchoCorte() < 0) {

            return resultado;
        }

        double anchoTablero = tablero.getAncho();
        double largoTablero = tablero.getLargo();
        double anchoCorte = tablero.getAnchoCorte() / 10.0;

        List<Pieza> piezasOrdenadas = new ArrayList<>();

        for (Pieza pieza : piezas) {

            if (pieza == null ||
                    pieza.getCantidad() <= 0 ||
                    !Double.isFinite(pieza.getAncho()) ||
                    !Double.isFinite(pieza.getLargo()) ||
                    pieza.getAncho() <= 0 ||
                    pieza.getLargo() <= 0) {

                continue;
            }

            if (!validador.piezaCabe(
                    anchoTablero,
                    largoTablero,
                    pieza.getAncho(),
                    pieza.getLargo()
            )) {
                continue;
            }

            for (int i = 0; i < pieza.getCantidad(); i++) {
                piezasOrdenadas.add(pieza);
            }
        }

        piezasOrdenadas.sort(
                Comparator.comparingDouble(Pieza::getArea).reversed()
        );

        List<EspacioLibre> espaciosLibres = new ArrayList<>();

        espaciosLibres.add(
                new EspacioLibre(
                        0,
                        0,
                        anchoTablero,
                        largoTablero
                )
        );

        for (Pieza pieza : piezasOrdenadas) {

            EvaluacionEspacio mejorEvaluacion = null;

            for (EspacioLibre espacio : espaciosLibres) {

                EvaluacionEspacio normal = evaluar(
                        espacio,
                        pieza.getAncho(),
                        pieza.getLargo(),
                        false
                );

                EvaluacionEspacio girada = evaluar(
                        espacio,
                        pieza.getLargo(),
                        pieza.getAncho(),
                        true
                );

                if (normal != null &&
                        (mejorEvaluacion == null ||
                                normal.getPuntuacion() <
                                        mejorEvaluacion.getPuntuacion())) {

                    mejorEvaluacion = normal;
                }

                if (girada != null &&
                        (mejorEvaluacion == null ||
                                girada.getPuntuacion() <
                                        mejorEvaluacion.getPuntuacion())) {

                    mejorEvaluacion = girada;
                }
            }

            if (mejorEvaluacion == null) {
                continue;
            }

            EspacioLibre espacio = mejorEvaluacion.getEspacio();

            resultado.add(
                    new PiezaColocada(
                            pieza,
                            espacio.getX(),
                            espacio.getY(),
                            mejorEvaluacion.getAnchoPieza(),
                            mejorEvaluacion.getLargoPieza(),
                            mejorEvaluacion.isRotada()
                    )
            );

            dividirEspacios(
                    espaciosLibres,
                    espacio,
                    mejorEvaluacion.getAnchoPieza(),
                    mejorEvaluacion.getLargoPieza(),
                    anchoCorte
            );
        }

        return resultado;
    }

    private EvaluacionEspacio evaluar(
            EspacioLibre espacio,
            double ancho,
            double largo,
            boolean rotada
    ) {

        if (ancho > espacio.getAncho() ||
                largo > espacio.getLargo()) {
            return null;
        }

        double sobranteAncho = espacio.getAncho() - ancho;
        double sobranteLargo = espacio.getLargo() - largo;

        double puntuacion = Math.min(
                sobranteAncho,
                sobranteLargo
        );

        return new EvaluacionEspacio(
                espacio,
                espacio.getX(),
                espacio.getY(),
                ancho,
                largo,
                rotada,
                puntuacion
        );
    }

    private void dividirEspacios(
            List<EspacioLibre> espaciosLibres,
            EspacioLibre ocupado,
            double anchoPieza,
            double largoPieza,
            double anchoCorte
    ) {

        espaciosLibres.remove(ocupado);

        double x = ocupado.getX();
        double y = ocupado.getY();

        double anchoRestante =
                ocupado.getAncho() - anchoPieza - anchoCorte;

        double largoRestante =
                ocupado.getLargo() - largoPieza - anchoCorte;

        if (anchoRestante > 0) {

            espaciosLibres.add(
                    new EspacioLibre(
                            x + anchoPieza + anchoCorte,
                            y,
                            anchoRestante,
                            ocupado.getLargo()
                    )
            );
        }

        if (largoRestante > 0) {

            espaciosLibres.add(
                    new EspacioLibre(
                            x,
                            y + largoPieza + anchoCorte,
                            anchoPieza,
                            largoRestante
                    )
            );
        }
    }
}


