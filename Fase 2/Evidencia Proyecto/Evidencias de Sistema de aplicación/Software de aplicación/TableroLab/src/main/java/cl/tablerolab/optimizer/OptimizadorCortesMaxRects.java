package cl.tablerolab.optimizer;

import cl.tablerolab.model.Tablero;
import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;

import java.util.ArrayList;
import java.util.List;

import cl.tablerolab.service.ValidadorTablero;

public class OptimizadorCortesMaxRects implements OptimizadorCortes {

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

        if (validador.validarConfiguracion(tablero) != null) {
            return resultado;
        }

        double anchoTablero = tablero.getAncho();
        double largoTablero = tablero.getLargo();

        // Convertimos el ancho de corte de mm a cm
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

        List<EspacioLibre> espaciosLibres = new ArrayList<>();

        espaciosLibres.add(
                new EspacioLibre(
                        0,
                        0,
                        anchoTablero,
                        largoTablero
                )
        );

        while (!piezasOrdenadas.isEmpty()) {

            EvaluacionEspacio mejorEvaluacionGlobal = null;
            int indiceMejorPieza = -1;

            double mejorPuntuacionGlobal =
                    Double.POSITIVE_INFINITY;

            // Evaluamos todas las piezas pendientes
            for (int i = 0; i < piezasOrdenadas.size(); i++) {

                Pieza piezaActual = piezasOrdenadas.get(i);

                EvaluacionEspacio evaluacion =
                        buscarMejorEspacio(
                                espaciosLibres,
                                piezaActual,
                                resultado,
                                anchoCorte
                        );

                if (evaluacion != null) {

                    double puntuacionActual =
                            calcularPuntuacionDistribucion(
                                    evaluacion,
                                    resultado,
                                    anchoCorte
                            );

                    if (puntuacionActual < mejorPuntuacionGlobal) {

                        mejorPuntuacionGlobal = puntuacionActual;
                        mejorEvaluacionGlobal = evaluacion;
                        indiceMejorPieza = i;
                    }
                }
            }

            // Ninguna pieza pendiente cabe en los espacios disponibles
            if (mejorEvaluacionGlobal == null) {
                break;
            }

            // Retiramos la pieza seleccionada de las pendientes
            Pieza pieza = piezasOrdenadas.remove(indiceMejorPieza);

            double x = mejorEvaluacionGlobal.getX();
            double y = mejorEvaluacionGlobal.getY();

            resultado.add(
                    new PiezaColocada(
                            pieza,
                            x,
                            y,
                            mejorEvaluacionGlobal.getAnchoPieza(),
                            mejorEvaluacionGlobal.getLargoPieza(),
                            mejorEvaluacionGlobal.isRotada()
                    )
            );

            EspacioLibre piezaOcupada = new EspacioLibre(
                    x,
                    y,
                    mejorEvaluacionGlobal.getAnchoPieza(),
                    mejorEvaluacionGlobal.getLargoPieza()
            );

            dividirEspacios(
                    espaciosLibres,
                    piezaOcupada,
                    mejorEvaluacionGlobal.getAnchoPieza(),
                    mejorEvaluacionGlobal.getLargoPieza(),
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

        double sobranteAncho =
                espacio.getAncho() - ancho;

        double sobranteLargo =
                espacio.getLargo() - largo;

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

        double piezaX = ocupado.getX();
        double piezaY = ocupado.getY();

        double piezaDerecha = piezaX + anchoPieza;
        double piezaAbajo = piezaY + largoPieza;

        List<EspacioLibre> nuevosEspacios = new ArrayList<>();

        for (EspacioLibre espacio : espaciosLibres) {

            double espacioDerecho =
                    espacio.getX() + espacio.getAncho();

            double espacioAbajo =
                    espacio.getY() + espacio.getLargo();

            double tolerancia = 0.000000001;

            boolean solapamientoHorizontal =
                    piezaX < espacioDerecho - tolerancia &&
                            piezaDerecha > espacio.getX() + tolerancia;

            boolean solapamientoVertical =
                    piezaY < espacioAbajo - tolerancia &&
                            piezaAbajo > espacio.getY() + tolerancia;

            boolean contactoHorizontal =
                    (
                            Math.abs(piezaDerecha - espacio.getX()) <= tolerancia ||
                                    Math.abs(piezaX - espacioDerecho) <= tolerancia
                    ) && solapamientoVertical;

            boolean contactoVertical =
                    (
                            Math.abs(piezaAbajo - espacio.getY()) <= tolerancia ||
                                    Math.abs(piezaY - espacioAbajo) <= tolerancia
                    ) && solapamientoHorizontal;

            boolean intersecta =
                    (solapamientoHorizontal && solapamientoVertical)
                            || contactoHorizontal
                            || contactoVertical;

            if (!intersecta) {
                nuevosEspacios.add(espacio);
                continue;
            }

            // Espacio superior: reservamos el ancho de corte.
            double altoSuperior =
                    piezaY - espacio.getY() - anchoCorte;

            if (altoSuperior > 0) {
                nuevosEspacios.add(
                        new EspacioLibre(
                                espacio.getX(),
                                espacio.getY(),
                                espacio.getAncho(),
                                altoSuperior
                        )
                );
            }

            // Espacio inferior: reservamos el ancho de corte.
            double altoInferior =
                    espacioAbajo - piezaAbajo - anchoCorte;

            if (altoInferior > 0) {
                nuevosEspacios.add(
                        new EspacioLibre(
                                espacio.getX(),
                                piezaAbajo + anchoCorte,
                                espacio.getAncho(),
                                altoInferior
                        )
                );
            }

            // Espacio izquierdo: reservamos el ancho de corte.
            double anchoIzquierdo =
                    piezaX - espacio.getX() - anchoCorte;

            if (anchoIzquierdo > 0) {
                nuevosEspacios.add(
                        new EspacioLibre(
                                espacio.getX(),
                                espacio.getY(),
                                anchoIzquierdo,
                                espacio.getLargo()
                        )
                );
            }

            // Espacio derecho: reservamos el ancho de corte.
            double anchoDerecho =
                    espacioDerecho - piezaDerecha - anchoCorte;

            if (anchoDerecho > 0) {
                nuevosEspacios.add(
                        new EspacioLibre(
                                piezaDerecha + anchoCorte,
                                espacio.getY(),
                                anchoDerecho,
                                espacio.getLargo()
                        )
                );
            }
        }

        espaciosLibres.clear();
        espaciosLibres.addAll(nuevosEspacios);

        depurarEspacios(espaciosLibres);

        System.out.println("Espacios libres después de colocar una pieza:");

        for (EspacioLibre espacio : espaciosLibres) {
            System.out.println(
                    "X: " + espacio.getX() +
                            " | Y: " + espacio.getY() +
                            " | Ancho: " + espacio.getAncho() +
                            " | Largo: " + espacio.getLargo()
            );
        }
    }

    private void depurarEspacios(
            List<EspacioLibre> espaciosLibres
    ) {

        for (int i = 0; i < espaciosLibres.size(); i++) {

            EspacioLibre espacioA = espaciosLibres.get(i);

            for (int j = i + 1; j < espaciosLibres.size(); j++) {

                EspacioLibre espacioB = espaciosLibres.get(j);

                boolean aContieneB =
                        espacioA.getX() <= espacioB.getX() &&
                                espacioA.getY() <= espacioB.getY() &&
                                espacioA.getX() + espacioA.getAncho() >=
                                        espacioB.getX() + espacioB.getAncho() &&
                                espacioA.getY() + espacioA.getLargo() >=
                                        espacioB.getY() + espacioB.getLargo();

                boolean bContieneA =
                        espacioB.getX() <= espacioA.getX() &&
                                espacioB.getY() <= espacioA.getY() &&
                                espacioB.getX() + espacioB.getAncho() >=
                                        espacioA.getX() + espacioA.getAncho() &&
                                espacioB.getY() + espacioB.getLargo() >=
                                        espacioA.getY() + espacioA.getLargo();

                if (aContieneB) {
                    espaciosLibres.remove(j);
                    j--;
                } else if (bContieneA) {
                    espaciosLibres.remove(i);
                    i--;
                    break;
                }
            }
        }
    }


    private EvaluacionEspacio buscarMejorEspacio(
            List<EspacioLibre> espaciosLibres,
            Pieza pieza,
            List<PiezaColocada> piezasColocadas,
            double anchoCorte
    ) {

        EvaluacionEspacio mejorEvaluacion = null;

        double mejorPuntuacion =
                Double.POSITIVE_INFINITY;

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
                    respetaSeparacion(
                            normal.getX(),
                            normal.getY(),
                            normal.getAnchoPieza(),
                            normal.getLargoPieza(),
                            piezasColocadas,
                            anchoCorte
                    )) {

                double puntuacion =
                        calcularPuntuacionDistribucion(
                                normal,
                                piezasColocadas,
                                anchoCorte
                        );

                if (puntuacion < mejorPuntuacion) {
                    mejorPuntuacion = puntuacion;
                    mejorEvaluacion = normal;
                }
            }

            if (girada != null &&
                    respetaSeparacion(
                            girada.getX(),
                            girada.getY(),
                            girada.getAnchoPieza(),
                            girada.getLargoPieza(),
                            piezasColocadas,
                            anchoCorte
                    )) {

                double puntuacion =
                        calcularPuntuacionDistribucion(
                                girada,
                                piezasColocadas,
                                anchoCorte
                        );

                if (puntuacion < mejorPuntuacion) {
                    mejorPuntuacion = puntuacion;
                    mejorEvaluacion = girada;
                }
            }
        }

        return mejorEvaluacion;
    }




    private double calcularPuntuacionDistribucion(
            EvaluacionEspacio evaluacion,
            List<PiezaColocada> piezasColocadas,
            double anchoCorte
    ) {

        double puntuacion = evaluacion.getPuntuacion();

        double tolerancia = 0.000000001;

        double x = evaluacion.getX();
        double y = evaluacion.getY();

        double derecha = x + evaluacion.getAnchoPieza();
        double abajo = y + evaluacion.getLargoPieza();

        boolean alineacionHorizontal = false;
        boolean alineacionVertical = false;

        boolean contactoHorizontal = false;
        boolean contactoVertical = false;

        double mejorDistancia = Double.POSITIVE_INFINITY;

        for (PiezaColocada existente : piezasColocadas) {

            double existenteDerecha =
                    existente.getX() + existente.getAncho();

            double existenteAbajo =
                    existente.getY() + existente.getLargo();

            boolean solapamientoHorizontal =
                    x < existenteDerecha - tolerancia &&
                            derecha > existente.getX() + tolerancia;

            boolean solapamientoVertical =
                    y < existenteAbajo - tolerancia &&
                            abajo > existente.getY() + tolerancia;

            double distanciaHorizontal =
                    Math.min(
                            Math.abs(x - existenteDerecha - anchoCorte),
                            Math.abs(existente.getX() - derecha - anchoCorte)
                    );

            double distanciaVertical =
                    Math.min(
                            Math.abs(y - existenteAbajo - anchoCorte),
                            Math.abs(existente.getY() - abajo - anchoCorte)
                    );

            if (solapamientoVertical) {
                mejorDistancia = Math.min(
                        mejorDistancia,
                        distanciaHorizontal
                );
            }

            if (solapamientoHorizontal) {
                mejorDistancia = Math.min(
                        mejorDistancia,
                        distanciaVertical
                );
            }

            // Alineación horizontal: piezas que comparten una misma fila.
            if (Math.abs(y - existente.getY()) <= tolerancia ||
                    Math.abs(abajo - existenteAbajo) <= tolerancia) {

                alineacionHorizontal = true;
            }

            // Alineación vertical: piezas que comparten una misma columna.
            if (Math.abs(x - existente.getX()) <= tolerancia ||
                    Math.abs(derecha - existenteDerecha) <= tolerancia) {

                alineacionVertical = true;
            }

            // Contacto horizontal con separación de corte.
            if (solapamientoVertical &&
                    (Math.abs(x - existenteDerecha - anchoCorte) <= tolerancia ||
                            Math.abs(existente.getX() - derecha - anchoCorte) <= tolerancia)) {

                contactoHorizontal = true;
            }

            // Contacto vertical con separación de corte.
            if (solapamientoHorizontal &&
                    (Math.abs(y - existenteAbajo - anchoCorte) <= tolerancia ||
                            Math.abs(existente.getY() - abajo - anchoCorte) <= tolerancia)) {

                contactoVertical = true;
            }
        }

        if (alineacionHorizontal) {
            puntuacion -= 0.25;
        }

        if (alineacionVertical) {
            puntuacion -= 0.25;
        }

        if (contactoHorizontal) {
            puntuacion -= 0.50;
        }

        if (contactoVertical) {
            puntuacion -= 0.50;
        }

// Favorecemos la proximidad entre piezas,
// sin desplazar el criterio principal de MaxRects.
        if (Double.isFinite(mejorDistancia)) {
            puntuacion += mejorDistancia * 0.10;
        }

        return puntuacion;
    }



    private boolean respetaSeparacion(
            double x,
            double y,
            double ancho,
            double largo,
            List<PiezaColocada> piezasColocadas,
            double anchoCorte
    ) {

        double tolerancia = 0.000000001;

        double derechaNueva = x + ancho;
        double abajoNueva = y + largo;

        for (PiezaColocada colocada : piezasColocadas) {

            double derechaExistente =
                    colocada.getX() + colocada.getAncho();

            double abajoExistente =
                    colocada.getY() + colocada.getLargo();

            // Comprobamos si existe superposición horizontal y vertical.
            boolean superposicionHorizontal =
                    x < derechaExistente - tolerancia &&
                            derechaNueva > colocada.getX() + tolerancia;

            boolean superposicionVertical =
                    y < abajoExistente - tolerancia &&
                            abajoNueva > colocada.getY() + tolerancia;

            // No permitimos superposición entre piezas.
            if (superposicionHorizontal && superposicionVertical) {
                return false;
            }

            // Comprobamos separación vertical cuando las piezas
            // se proyectan una sobre otra en el eje horizontal.
            if (superposicionHorizontal) {

                double separacionVertical;

                if (y >= abajoExistente) {
                    separacionVertical = y - abajoExistente;
                } else {
                    separacionVertical = colocada.getY() - abajoNueva;
                }

                if (separacionVertical < anchoCorte - tolerancia) {
                    return false;
                }
            }

            // Comprobamos separación horizontal cuando las piezas
            // se proyectan una sobre otra en el eje vertical.
            if (superposicionVertical) {

                double separacionHorizontal;

                if (x >= derechaExistente) {
                    separacionHorizontal = x - derechaExistente;
                } else {
                    separacionHorizontal = colocada.getX() - derechaNueva;
                }

                if (separacionHorizontal < anchoCorte - tolerancia) {
                    return false;
                }
            }
        }

        return true;
    }


}

