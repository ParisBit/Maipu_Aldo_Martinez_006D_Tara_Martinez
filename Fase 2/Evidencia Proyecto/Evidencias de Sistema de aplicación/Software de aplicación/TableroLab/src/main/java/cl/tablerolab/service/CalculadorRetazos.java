
package cl.tablerolab.service;

import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Retazo;
import cl.tablerolab.model.Tablero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CalculadorRetazos {

    private static final double TOLERANCIA = 0.000000001;


    public List<Retazo> calcularRetazos(
            Tablero tablero,
            List<PiezaColocada> piezas
    ) {
        return calcularRetazos(tablero, piezas, 1);
    }

    public List<Retazo> calcularRetazos(
            Tablero tablero,
            List<PiezaColocada> piezas,
            int numeroTablero
    ) {
        List<Retazo> retazos = new ArrayList<>();

        ValidadorTablero validador = new ValidadorTablero();

        if (tablero == null || piezas == null
                || numeroTablero < 1
                || validador.validarConfiguracion(tablero) != null) {
            return retazos;
        }

        double anchoTablero = tablero.getAncho();
        double largoTablero = tablero.getLargo();
        double espesor = tablero.getEspesor();

        // El ancho de corte se almacena en milímetros.
        // Las coordenadas trabajan en centímetros.
        double kerf = tablero.getAnchoCorte() / 10.0;

        List<Rectangulo> piezasValidas = validarPiezas(
                piezas,
                anchoTablero,
                largoTablero
        );

        if (piezasValidas == null) {
            return retazos;
        }

        List<Rectangulo> cortes = calcularFranjasCorte(
                piezasValidas,
                kerf,
                anchoTablero,
                largoTablero
        );

        if (cortes == null || !validarCortes(cortes, piezasValidas)) {
            return retazos;
        }

        List<Rectangulo> espaciosLibres = calcularEspaciosLibres(
                anchoTablero,
                largoTablero,
                piezasValidas,
                cortes
        );

        for (Rectangulo espacio : espaciosLibres) {
            if (espacio.ancho > TOLERANCIA
                    && espacio.largo > TOLERANCIA) {

                retazos.add(new Retazo(
                        espacio.x,
                        espacio.y,
                        espacio.ancho,
                        espacio.largo,
                        espesor,
                        numeroTablero
                ));
            }
        }

        return retazos;
    }


    private List<Rectangulo> validarPiezas(
            List<PiezaColocada> piezas,
            double anchoTablero,
            double largoTablero
    ) {
        List<Rectangulo> resultado = new ArrayList<>();

        for (PiezaColocada pieza : piezas) {

            if (pieza == null) {
                continue;
            }

            double x = pieza.getX();
            double y = pieza.getY();
            double ancho = pieza.getAncho();
            double largo = pieza.getLargo();

            if (!Double.isFinite(x)
                    || !Double.isFinite(y)
                    || !Double.isFinite(ancho)
                    || !Double.isFinite(largo)
                    || ancho <= 0
                    || largo <= 0) {
                return null;
            }

            if (x < -TOLERANCIA
                    || y < -TOLERANCIA
                    || x + ancho > anchoTablero + TOLERANCIA
                    || y + largo > largoTablero + TOLERANCIA) {
                return null;
            }

            Rectangulo nueva = new Rectangulo(
                    x, y, ancho, largo
            );

            for (Rectangulo existente : resultado) {
                if (seSuperponen(nueva, existente)) {
                    return null;
                }
            }

            resultado.add(nueva);
        }

        return resultado;
    }

    private List<Rectangulo> calcularFranjasCorte(
            List<Rectangulo> piezas,
            double kerf,
            double anchoTablero,
            double largoTablero
    ) {
        List<Rectangulo> cortes = new ArrayList<>();

        if (kerf <= TOLERANCIA) {
            return cortes;
        }

        for (Rectangulo pieza : piezas) {

            double derecha = pieza.x + pieza.ancho;
            double inferior = pieza.y + pieza.largo;

            // Corte izquierdo.
            if (pieza.x > TOLERANCIA) {
                if (!agregarCorte(
                        cortes, pieza.x - kerf, pieza.y,
                        kerf, pieza.largo,
                        anchoTablero, largoTablero
                )) {
                    return null;
                }
            }

            // Corte derecho.
            if (derecha < anchoTablero - TOLERANCIA) {
                if (!agregarCorte(
                        cortes, derecha, pieza.y,
                        kerf, pieza.largo,
                        anchoTablero, largoTablero
                )) {
                    return null;
                }
            }

            // Corte superior.
            if (pieza.y > TOLERANCIA) {
                if (!agregarCorte(
                        cortes, pieza.x, pieza.y - kerf,
                        pieza.ancho, kerf,
                        anchoTablero, largoTablero
                )) {
                    return null;
                }
            }

            // Corte inferior.
            if (inferior < largoTablero - TOLERANCIA) {
                if (!agregarCorte(
                        cortes, pieza.x, inferior,
                        pieza.ancho, kerf,
                        anchoTablero, largoTablero
                )) {
                    return null;
                }
            }
        }

        return cortes;
    }





    private boolean agregarCorte(
            List<Rectangulo> cortes,
            double x,
            double y,
            double ancho,
            double largo,
            double anchoTablero,
            double largoTablero
    ) {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(ancho)
                || !Double.isFinite(largo)
                || ancho <= TOLERANCIA
                || largo <= TOLERANCIA) {
            return false;
        }

        if (x < -TOLERANCIA
                || y < -TOLERANCIA
                || x + ancho > anchoTablero + TOLERANCIA
                || y + largo > largoTablero + TOLERANCIA) {
            return false;
        }

        cortes.add(new Rectangulo(x, y, ancho, largo));

        return true;
    }

    private boolean validarCortes(
            List<Rectangulo> cortes,
            List<Rectangulo> piezas
    ) {
        for (Rectangulo corte : cortes) {
            for (Rectangulo pieza : piezas) {
                if (seSuperponen(corte, pieza)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<Rectangulo> calcularEspaciosLibres(
            double anchoTablero,
            double largoTablero,
            List<Rectangulo> piezas,
            List<Rectangulo> cortes
    ) {
        TreeSet<Double> coordenadasX = new TreeSet<>();
        TreeSet<Double> coordenadasY = new TreeSet<>();

        coordenadasX.add(0.0);
        coordenadasX.add(anchoTablero);
        coordenadasY.add(0.0);
        coordenadasY.add(largoTablero);

        agregarCoordenadas(
                coordenadasX,
                coordenadasY,
                piezas
        );

        agregarCoordenadas(
                coordenadasX,
                coordenadasY,
                cortes
        );

        List<Double> xs = new ArrayList<>(coordenadasX);
        List<Double> ys = new ArrayList<>(coordenadasY);

        List<Rectangulo> resultado = new ArrayList<>();

        Map<Long, Rectangulo> activos = new HashMap<>();

        for (int fila = 0; fila < ys.size() - 1; fila++) {

            double y = ys.get(fila);
            double siguienteY = ys.get(fila + 1);

            double centroY = (y + siguienteY) / 2.0;

            Set<Long> presentes = new HashSet<>();

            int columna = 0;

            while (columna < xs.size() - 1) {

                double centroX = (
                        xs.get(columna) + xs.get(columna + 1)
                ) / 2.0;

                if (!esEspacioLibre(
                        centroX,
                        centroY,
                        piezas,
                        cortes
                )) {
                    columna++;
                    continue;
                }

                int inicio = columna;

                while (columna < xs.size() - 1) {

                    double centro = (
                            xs.get(columna) + xs.get(columna + 1)
                    ) / 2.0;

                    if (!esEspacioLibre(
                            centro,
                            centroY,
                            piezas,
                            cortes
                    )) {
                        break;
                    }

                    columna++;
                }

                int fin = columna;

                long clave = ((long) inicio << 32)
                        | (fin & 0xffffffffL);

                presentes.add(clave);

                Rectangulo activo = activos.get(clave);

                if (activo == null) {
                    activos.put(
                            clave,
                            new Rectangulo(
                                    xs.get(inicio),
                                    y,
                                    xs.get(fin) - xs.get(inicio),
                                    siguienteY - y
                            )
                    );
                } else {
                    activo.largo += siguienteY - y;
                }
            }

            // Finalizar rectángulos que ya no continúan
            // en la siguiente franja horizontal.
            List<Long> finalizados = new ArrayList<>();

            for (Map.Entry<Long, Rectangulo> entrada
                    : activos.entrySet()) {

                if (!presentes.contains(entrada.getKey())) {
                    resultado.add(entrada.getValue());
                    finalizados.add(entrada.getKey());
                }
            }

            for (Long clave : finalizados) {
                activos.remove(clave);
            }
        }

        resultado.addAll(activos.values());

        return resultado;
    }

    private void agregarCoordenadas(
            Set<Double> xs,
            Set<Double> ys,
            List<Rectangulo> rectangulos
    ) {
        for (Rectangulo rectangulo : rectangulos) {
            xs.add(rectangulo.x);
            xs.add(rectangulo.x + rectangulo.ancho);

            ys.add(rectangulo.y);
            ys.add(rectangulo.y + rectangulo.largo);
        }
    }

    private boolean esEspacioLibre(
            double x,
            double y,
            List<Rectangulo> piezas,
            List<Rectangulo> cortes
    ) {
        for (Rectangulo pieza : piezas) {
            if (contiene(pieza, x, y)) {
                return false;
            }
        }

        for (Rectangulo corte : cortes) {
            if (contiene(corte, x, y)) {
                return false;
            }
        }

        return true;
    }

    private boolean contiene(
            Rectangulo rectangulo,
            double x,
            double y
    ) {
        return x > rectangulo.x - TOLERANCIA
                && x < rectangulo.x + rectangulo.ancho + TOLERANCIA
                && y > rectangulo.y - TOLERANCIA
                && y < rectangulo.y + rectangulo.largo + TOLERANCIA;
    }

    private boolean seSuperponen(
            Rectangulo a,
            Rectangulo b
    ) {
        double interseccionX = Math.min(
                a.x + a.ancho,
                b.x + b.ancho
        ) - Math.max(a.x, b.x);

        double interseccionY = Math.min(
                a.y + a.largo,
                b.y + b.largo
        ) - Math.max(a.y, b.y);

        return interseccionX > TOLERANCIA
                && interseccionY > TOLERANCIA;
    }

    private static class Rectangulo {

        private final double x;
        private final double y;
        private final double ancho;
        private double largo;

        private Rectangulo(
                double x,
                double y,
                double ancho,
                double largo
        ) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.largo = largo;
        }
    }
}
