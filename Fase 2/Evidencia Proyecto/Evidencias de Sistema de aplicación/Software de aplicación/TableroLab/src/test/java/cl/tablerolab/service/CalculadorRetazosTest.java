
package cl.tablerolab.service;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Retazo;
import cl.tablerolab.model.Tablero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorRetazosTest {

    private static final double TOLERANCIA = 0.000001;

    private final CalculadorRetazos calculador = new CalculadorRetazos();

    @Test
    void debeCalcularCortesDeUnaPiezaEnUnaEsquina() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza", 40, 40, 0, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 24);
    }

    @Test
    void debeCalcularCortesDeUnaPiezaEnElCentro() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza", 40, 40, 30, 30)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 48);
    }

    @Test
    void debeContabilizarUnaSolaVezElCorteCompartidoEntreDosPiezas() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0, 0),
                colocar("Pieza B", 40, 40, 40.3, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 48);
    }

    @Test
    void debeCalcularCortesDeTresPiezasAlineadas() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 30, 40, 0, 0),
                colocar("Pieza B", 30, 40, 30.3, 0),
                colocar("Pieza C", 30, 40, 60.6, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 63);
    }

    @Test
    void debeCalcularCortesDeCuatroPiezasAlineadas() {
        Tablero tablero = crearTablero(130, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 30, 40, 0, 0),
                colocar("Pieza B", 30, 40, 30.3, 0),
                colocar("Pieza C", 30, 40, 60.6, 0),
                colocar("Pieza D", 30, 40, 90.9, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 84);
    }

    @Test
    void debeConservarElBalanceCuandoElAnchoDeCorteEsCero() {
        Tablero tablero = crearTablero(100, 100, 0);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza", 40, 40, 30, 30)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 0);
    }

    @Test
    void debeRetornarElTableroCompletoCuandoNoHayPiezas() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<Retazo> retazos = calculador.calcularRetazos(tablero, List.of());

        assertEquals(1, retazos.size());
        assertEquals(10000, sumarAreaRetazos(retazos), TOLERANCIA);
    }

    @Test
    void debeRetornarListaVaciaCuandoHayPiezasSuperpuestas() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0, 0),
                colocar("Pieza B", 40, 40, 20, 20)
        );

        assertTrue(calculador.calcularRetazos(tablero, piezas).isEmpty());
    }

    @Test
    void debeRetornarListaVaciaCuandoUnaPiezaSaleDelTablero() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza", 40, 40, 70, 70)
        );

        assertTrue(calculador.calcularRetazos(tablero, piezas).isEmpty());
    }

    @Test
    void debeRetornarListaVaciaCuandoElTableroEsNulo() {
        assertTrue(calculador.calcularRetazos(null, List.of()).isEmpty());
    }

    @Test
    void debeRetornarListaVaciaCuandoLaListaDePiezasEsNula() {
        assertTrue(calculador.calcularRetazos(
                crearTablero(100, 100, 3), null
        ).isEmpty());
    }

    @Test
    void todosLosRetazosDebenEstarDentroDelTablero() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0, 0),
                colocar("Pieza B", 40, 40, 40.3, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        for (Retazo retazo : retazos) {
            assertTrue(retazo.getX() >= -TOLERANCIA);
            assertTrue(retazo.getY() >= -TOLERANCIA);

            assertTrue(retazo.getX() + retazo.getAncho()
                    <= tablero.getAncho() + TOLERANCIA);

            assertTrue(retazo.getY() + retazo.getLargo()
                    <= tablero.getLargo() + TOLERANCIA);
        }
    }

    @Test
    void losRetazosNoDebenSuperponerse() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0, 0),
                colocar("Pieza B", 40, 40, 40.3, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        for (int i = 0; i < retazos.size(); i++) {
            for (int j = i + 1; j < retazos.size(); j++) {
                assertFalse(
                        seSuperponen(retazos.get(i), retazos.get(j)),
                        "Se detectaron retazos superpuestos"
                );
            }
        }
    }

    @Test
    void debeManejarDistribucionIrregularDePiezas() {
        Tablero tablero = crearTablero(120, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 30, 20, 0, 0),
                colocar("Pieza B", 25, 35, 30.3, 0),
                colocar("Pieza C", 30, 25, 0, 20.3)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        assertFalse(retazos.isEmpty());

        verificarBalance(
                tablero,
                piezas,
                retazos,
                tablero.getArea()
                        - sumarAreaPiezas(piezas)
                        - sumarAreaRetazos(retazos)
        );

        for (Retazo retazo : retazos) {
            for (PiezaColocada pieza : piezas) {
                assertFalse(
                        retazoSeSuperponeConPieza(retazo, pieza),
                        "Retazo: x=" + retazo.getX()
                                + ", y=" + retazo.getY()
                                + ", ancho=" + retazo.getAncho()
                                + ", largo=" + retazo.getLargo()
                                + ". Pieza: x=" + pieza.getX()
                                + ", y=" + pieza.getY()
                                + ", ancho=" + pieza.getAncho()
                                + ", largo=" + pieza.getLargo()
                );
            }
        }

        for (int i = 0; i < retazos.size(); i++) {
            for (int j = i + 1; j < retazos.size(); j++) {
                assertFalse(
                        seSuperponen(retazos.get(i), retazos.get(j)),
                        "Se detectaron retazos superpuestos"
                );
            }
        }
    }

    @Test
    void debeContabilizarUnaSolaVezLaInterseccionDeCortes() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0, 0),
                colocar("Pieza B", 40, 40, 40.3, 0),
                colocar("Pieza C", 40, 40, 0, 40.3),
                colocar("Pieza D", 40, 40, 40.3, 40.3)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        verificarBalance(tablero, piezas, retazos, 96);
    }

    private void verificarBalance(
            Tablero tablero,
            List<PiezaColocada> piezas,
            List<Retazo> retazos,
            double areaCortesEsperada
    ) {
        assertFalse(retazos.isEmpty());

        double areaPiezas = sumarAreaPiezas(piezas);
        double areaRetazos = sumarAreaRetazos(retazos);

        double areaCortes = tablero.getArea() - areaPiezas - areaRetazos;

        assertEquals(
                areaCortesEsperada,
                areaCortes,
                TOLERANCIA,
                "El área de cortes no coincide con la esperada"
        );

        assertEquals(
                tablero.getArea(),
                areaPiezas + areaCortes + areaRetazos,
                TOLERANCIA,
                "No se conserva el balance de superficies"
        );
    }

    private double sumarAreaPiezas(List<PiezaColocada> piezas) {
        return piezas.stream()
                .mapToDouble(p -> p.getAncho() * p.getLargo())
                .sum();
    }

    private double sumarAreaRetazos(List<Retazo> retazos) {
        return retazos.stream()
                .mapToDouble(Retazo::getArea)
                .sum();
    }

    private boolean seSuperponen(Retazo a, Retazo b) {
        return a.getX() < b.getX() + b.getAncho() - TOLERANCIA
                && a.getX() + a.getAncho() > b.getX() + TOLERANCIA
                && a.getY() < b.getY() + b.getLargo() - TOLERANCIA
                && a.getY() + a.getLargo() > b.getY() + TOLERANCIA;
    }

    private boolean retazoSeSuperponeConPieza(
            Retazo retazo,
            PiezaColocada pieza
    ) {
        return retazo.getX() < pieza.getX() + pieza.getAncho() - TOLERANCIA
                && retazo.getX() + retazo.getAncho() > pieza.getX() + TOLERANCIA
                && retazo.getY() < pieza.getY() + pieza.getLargo() - TOLERANCIA
                && retazo.getY() + retazo.getLargo() > pieza.getY() + TOLERANCIA;
    }

    private Tablero crearTablero(double ancho, double largo, double anchoCorte) {
        return new Tablero(ancho, largo, 18, anchoCorte);
    }

    private PiezaColocada colocar(
            String nombre,
            double ancho,
            double largo,
            double x,
            double y
    ) {
        Pieza pieza = new Pieza(nombre, ancho, largo, 1);

        return new PiezaColocada(
                pieza, x, y, ancho, largo, false
        );
    }

    @Test
    void debeRechazarPiezaCuandoElCorteNoCabeEnElBorde() {
        Tablero tablero = crearTablero(100, 100, 3);

        List<PiezaColocada> piezas = List.of(
                colocar("Pieza A", 40, 40, 0.1, 0)
        );

        List<Retazo> retazos = calculador.calcularRetazos(tablero, piezas);

        assertTrue(
                retazos.isEmpty(),
                "Debe rechazar la distribución porque el corte izquierdo sale del tablero"
        );
    }

}
