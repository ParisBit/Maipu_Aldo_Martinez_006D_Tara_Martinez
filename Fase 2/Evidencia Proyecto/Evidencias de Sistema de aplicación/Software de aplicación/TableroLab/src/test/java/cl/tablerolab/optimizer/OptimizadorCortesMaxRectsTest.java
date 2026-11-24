package cl.tablerolab.optimizer;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Tablero;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class OptimizadorCortesMaxRectsTest {

    private final OptimizadorCortesMaxRects optimizador =
            new OptimizadorCortesMaxRects();

    // ==========================================
    // GRUPO 1: VALIDACIÓN DE ENTRADAS
    // ==========================================

    @Test
    void debeRetornarListaVaciaSiTableroEsNulo() {

        List<PiezaColocada> resultado =
                optimizador.optimizar(null, List.of());

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarListaVaciaSiElAnchoDelTableroNoEsValido() {

        Tablero tablero = new Tablero(0, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    @Test
    void debeRetornarListaVaciaSiElLargoDelTableroNoEsValido() {

        Tablero tablero = new Tablero(100, -10, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    @Test
    void debeRetornarListaVaciaSiElAnchoDeCorteEsNegativo() {

        Tablero tablero = new Tablero(100, 100, 15, -3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    @Test
    void debeRetornarListaVaciaSiElAnchoDeCorteEsNaN() {

        Tablero tablero = new Tablero(100, 100, 15, Double.NaN);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    @Test
    void debeRetornarListaVaciaSiElAnchoDelTableroEsNaN() {

        Tablero tablero = new Tablero(
                Double.NaN, 100, 15, 3
        );

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    @Test
    void debeRetornarListaVaciaSiNoHayPiezas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, List.of());

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarListaVaciaSiElLargoDelTableroEsInfinito() {

        Tablero tablero = new Tablero(
                100, Double.POSITIVE_INFINITY, 15, 3
        );

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(
                optimizador.optimizar(tablero, piezas).isEmpty()
        );
    }

    // ==========================================
    // GRUPO 2: COLOCACIÓN DE PIEZAS
    // ==========================================

    @Test
    void debeColocarUnaPiezaEnElTablero() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 40, 40, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
    }

    @Test
    void debeColocarTodasLasUnidadesSolicitadasCuandoHayEspacio() {

        Tablero tablero = new Tablero(200, 200, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 3),
                new Pieza("Pieza B", 40, 40, 2)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(5, resultado.size());
    }

    @Test
    void debeIgnorarPiezaConAnchoInfinito() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", Double.POSITIVE_INFINITY, 30, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals("Pieza Valida", resultado.get(0).getPiezaOriginal().getNombre());
    }

    @Test
    void debeColocarUnaPiezaQueOcupaTodoElTablero() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza completa", 100, 100, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());

        assertEquals(0, resultado.get(0).getX(), 0.000001);
        assertEquals(0, resultado.get(0).getY(), 0.000001);
    }

    @Test
    void debeIgnorarPiezaConLargoInfinito() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", 30, Double.POSITIVE_INFINITY, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezasQueNoCabenEnElTablero() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza grande", 120, 120, 1),
                new Pieza("Pieza válida", 40, 40, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());

        assertEquals(
                "Pieza válida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezaConAnchoNaN() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", Double.NaN, 30, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezaConLargoNaN() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", 30, Double.NaN, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezaConAnchoNegativo() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", -10, 30, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezaConLargoNegativo() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", 30, -10, 1),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }


    @Test
    void debeIgnorarPiezaConCantidadCero() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Sin Cantidad", 30, 30, 0),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeIgnorarPiezaConCantidadNegativa() {
        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza Invalida", 30, 30, -2),
                new Pieza("Pieza Valida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals(
                "Pieza Valida",
                resultado.get(0).getPiezaOriginal().getNombre()
        );
    }

    @Test
    void debeRetornarListaVaciaSiElAnchoDelTableroEsInfinito() {
        Tablero tablero = new Tablero(
                Double.POSITIVE_INFINITY, 100, 15, 3
        );

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 30, 30, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

    @Test
    void debeColocarTodasLasPiezasAunqueExistanPiezasInvalidas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza válida A", 30, 30, 1),
                new Pieza("Pieza inválida", 0, 40, 1),
                new Pieza("Pieza válida B", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
    }

    @Test
    void debeRespetarAnchoDeCorteEntreDosPiezas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 40, 40, 1),
                new Pieza("Pieza B", 40, 40, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());

        PiezaColocada piezaA = resultado.get(0);
        PiezaColocada piezaB = resultado.get(1);

        double anchoCorte = 0.3;
        double tolerancia = 0.000001;

        boolean separacionHorizontal =
                (piezaA.getX() + piezaA.getAncho() <= piezaB.getX() &&
                        piezaB.getX() - (piezaA.getX() + piezaA.getAncho())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getX() + piezaB.getAncho() <= piezaA.getX() &&
                                piezaA.getX() - (piezaB.getX() + piezaB.getAncho())
                                        >= anchoCorte - tolerancia);

        boolean separacionVertical =
                (piezaA.getY() + piezaA.getLargo() <= piezaB.getY() &&
                        piezaB.getY() - (piezaA.getY() + piezaA.getLargo())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getY() + piezaB.getLargo() <= piezaA.getY() &&
                                piezaA.getY() - (piezaB.getY() + piezaB.getLargo())
                                        >= anchoCorte - tolerancia);

        assertTrue(
                separacionHorizontal || separacionVertical,
                "Las piezas no respetan los 3 mm de ancho de corte"
        );
    }

    @Test
    void debeRespetarAnchoDeCorteDeUnMilimetro() {

        Tablero tablero = new Tablero(100, 100, 15, 1);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 40, 40, 1),
                new Pieza("Pieza B", 40, 40, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());

        PiezaColocada piezaA = resultado.get(0);
        PiezaColocada piezaB = resultado.get(1);

        double anchoCorte = 0.1;
        double tolerancia = 0.000001;

        boolean separacionHorizontal =
                (piezaA.getX() + piezaA.getAncho() <= piezaB.getX() &&
                        piezaB.getX() - (piezaA.getX() + piezaA.getAncho())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getX() + piezaB.getAncho() <= piezaA.getX() &&
                                piezaA.getX() - (piezaB.getX() + piezaB.getAncho())
                                        >= anchoCorte - tolerancia);

        boolean separacionVertical =
                (piezaA.getY() + piezaA.getLargo() <= piezaB.getY() &&
                        piezaB.getY() - (piezaA.getY() + piezaA.getLargo())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getY() + piezaB.getLargo() <= piezaA.getY() &&
                                piezaA.getY() - (piezaB.getY() + piezaB.getLargo())
                                        >= anchoCorte - tolerancia);

        assertTrue(
                separacionHorizontal || separacionVertical,
                "Las piezas no respetan 1 mm de ancho de corte"
        );
    }

    @Test
    void debeRespetarAnchoDeCorteDeCincoMilimetros() {

        Tablero tablero = new Tablero(100, 100, 15, 5);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 40, 40, 1),
                new Pieza("Pieza B", 40, 40, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());

        PiezaColocada piezaA = resultado.get(0);
        PiezaColocada piezaB = resultado.get(1);

        double anchoCorte = 0.5;
        double tolerancia = 0.000001;

        boolean separacionHorizontal =
                (piezaA.getX() + piezaA.getAncho() <= piezaB.getX() &&
                        piezaB.getX() - (piezaA.getX() + piezaA.getAncho())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getX() + piezaB.getAncho() <= piezaA.getX() &&
                                piezaA.getX() - (piezaB.getX() + piezaB.getAncho())
                                        >= anchoCorte - tolerancia);

        boolean separacionVertical =
                (piezaA.getY() + piezaA.getLargo() <= piezaB.getY() &&
                        piezaB.getY() - (piezaA.getY() + piezaA.getLargo())
                                >= anchoCorte - tolerancia)
                        ||
                        (piezaB.getY() + piezaB.getLargo() <= piezaA.getY() &&
                                piezaA.getY() - (piezaB.getY() + piezaB.getLargo())
                                        >= anchoCorte - tolerancia);

        assertTrue(
                separacionHorizontal || separacionVertical,
                "Las piezas no respetan 5 mm de ancho de corte"
        );
    }

    @Test
    void debeRespetarAnchoDeCorteConPiezasDeDistintasDimensiones() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 60, 30, 1),
                new Pieza("Pieza B", 30, 50, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());

        double anchoCorte = 0.3;
        double tolerancia = 0.000001;

        for (int i = 0; i < resultado.size(); i++) {

            PiezaColocada piezaA = resultado.get(i);

            for (int j = i + 1; j < resultado.size(); j++) {

                PiezaColocada piezaB = resultado.get(j);

                boolean superposicionHorizontal =
                        piezaA.getX() < piezaB.getX() + piezaB.getAncho() &&
                                piezaA.getX() + piezaA.getAncho() > piezaB.getX();

                boolean superposicionVertical =
                        piezaA.getY() < piezaB.getY() + piezaB.getLargo() &&
                                piezaA.getY() + piezaA.getLargo() > piezaB.getY();

                assertFalse(
                        superposicionHorizontal && superposicionVertical,
                        "Las piezas se superponen"
                );

                if (superposicionHorizontal) {

                    double separacion = Math.abs(
                            piezaA.getY() - piezaB.getY()
                    );

                    if (piezaA.getY() < piezaB.getY()) {
                        separacion = piezaB.getY()
                                - (piezaA.getY() + piezaA.getLargo());
                    } else {
                        separacion = piezaA.getY()
                                - (piezaB.getY() + piezaB.getLargo());
                    }

                    assertTrue(
                            separacion >= anchoCorte - tolerancia,
                            "No se respeta el ancho de corte vertical"
                    );
                }

                if (superposicionVertical) {

                    double separacion;

                    if (piezaA.getX() < piezaB.getX()) {
                        separacion = piezaB.getX()
                                - (piezaA.getX() + piezaA.getAncho());
                    } else {
                        separacion = piezaA.getX()
                                - (piezaB.getX() + piezaB.getAncho());
                    }

                    assertTrue(
                            separacion >= anchoCorte - tolerancia,
                            "No se respeta el ancho de corte horizontal"
                    );
                }
            }
        }
    }


    // PRUEBA 15: Rotación de una pieza para que pueda caber.
    @Test
    void debeRotarUnaPiezaCuandoSeaNecesario() {

        Tablero tablero = new Tablero(30, 20, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Rotada", 20, 30, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isRotada());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 16: Kerf respetado cuando una pieza está rotada.
    @Test
    void debeRespetarKerfConPiezasRotadas() {

        Tablero tablero = new Tablero(60, 20, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 30, 1),
                new Pieza("Pieza B", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(PiezaColocada::isRotada));
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 17: Piezas cuadradas.
    @Test
    void debeColocarPiezasCuadradas() {

        Tablero tablero = new Tablero(40, 40, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Cuadrada", 20, 20, 4)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(4, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 18: Piezas con dimensiones muy diferentes.
    @Test
    void debeColocarPiezasDeDimensionesMuyDiferentes() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Horizontal", 80, 10, 1),
                new Pieza("Vertical", 10, 80, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 19: Ninguna pieza cabe en el tablero.
    @Test
    void noDebeColocarPiezasCuandoTodasSonDemasiadoGrandes() {

        Tablero tablero = new Tablero(20, 20, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Grande A", 21, 5, 1),
                new Pieza("Grande B", 5, 21, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertTrue(resultado.isEmpty());
    }

    // PRUEBA 20: Dos piezas con el espacio exacto para el kerf.
    @Test
    void debeColocarPiezasCuandoElEspacioEsExactoConKerf() {

        Tablero tablero = new Tablero(20.3, 10, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 10, 10, 1),
                new Pieza("Pieza B", 10, 10, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 21: Kerf de 10 mm.
    @Test
    void debeRespetarKerfDeDiezMilimetros() {

        Tablero tablero = new Tablero(21, 10, 15, 10);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 10, 10, 1),
                new Pieza("Pieza B", 10, 10, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 22: Kerf de 20 mm.
    @Test
    void debeRespetarKerfDeVeinteMilimetros() {

        Tablero tablero = new Tablero(22, 10, 15, 20);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 10, 10, 1),
                new Pieza("Pieza B", 10, 10, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 23: Muchas piezas pequeñas.
    @Test
    void debeColocarMuchasPiezasPequenas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pequeña", 5, 5, 40)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(40, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 24: Varias piezas repetidas.
    @Test
    void debeColocarVariasPiezasRepetidas() {

        Tablero tablero = new Tablero(70, 20, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Repetida", 20, 20, 3)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(3, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 25: Combinación de piezas grandes y pequeñas.
    @Test
    void debeProcesarMezclaDePiezasGrandesYPequenas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Grande", 60, 60, 1),
                new Pieza("Mediana", 40, 40, 1),
                new Pieza("Pequeña", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertFalse(resultado.isEmpty());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 26: Tablero estrecho.
    @Test
    void debeTrabajarConTableroEstrecho() {

        Tablero tablero = new Tablero(10, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Estrecha", 10, 20, 4)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(4, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 27: Tablero alargado.
    @Test
    void debeTrabajarConTableroAlargado() {

        Tablero tablero = new Tablero(100, 10, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Alargada", 20, 10, 4)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(4, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 28: Evitar superposiciones entre todas las piezas.
    @Test
    void noDebeExistirSuperposicionEntreLasPiezas() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza", 30, 20, 8)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertTrue(resultado.size() > 1);
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 29: Comprobar límites del tablero con kerf.
    @Test
    void debeRespetarLimitesDelTableroConKerf() {

        Tablero tablero = new Tablero(50, 40, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza", 25, 20, 4)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertFalse(resultado.isEmpty());
        assertDistribucionValida(tablero, resultado);
    }

    // PRUEBA 30: Comprobar que se procesa correctamente la cantidad solicitada.
    @Test
    void debeProcesarCorrectamenteLaCantidadSolicitada() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Lote", 10, 10, 12)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(12, resultado.size());
        assertDistribucionValida(tablero, resultado);
    }

    // MÉTODO AUXILIAR: Verifica límites, superposición y ancho de corte.
    private void assertDistribucionValida(
            Tablero tablero,
            List<PiezaColocada> piezas
    ) {

        double tolerancia = 0.000001;
        double kerf = tablero.getAnchoCorte() / 10.0;

        for (PiezaColocada pieza : piezas) {

            assertTrue(pieza.getX() >= -tolerancia);
            assertTrue(pieza.getY() >= -tolerancia);

            assertTrue(
                    pieza.getX() + pieza.getAncho()
                            <= tablero.getAncho() + tolerancia
            );

            assertTrue(
                    pieza.getY() + pieza.getLargo()
                            <= tablero.getLargo() + tolerancia
            );
        }

        for (int i = 0; i < piezas.size(); i++) {

            PiezaColocada a = piezas.get(i);

            for (int j = i + 1; j < piezas.size(); j++) {

                PiezaColocada b = piezas.get(j);

                double derechaA = a.getX() + a.getAncho();
                double abajoA = a.getY() + a.getLargo();

                double derechaB = b.getX() + b.getAncho();
                double abajoB = b.getY() + b.getLargo();

                boolean solapamientoHorizontal =
                        a.getX() < derechaB - tolerancia
                                && b.getX() < derechaA - tolerancia;

                boolean solapamientoVertical =
                        a.getY() < abajoB - tolerancia
                                && b.getY() < abajoA - tolerancia;

                assertFalse(
                        solapamientoHorizontal && solapamientoVertical,
                        "Se detectó superposición entre dos piezas."
                );

                if (solapamientoHorizontal) {

                    double separacionVertical = Math.max(
                            b.getY() - abajoA,
                            a.getY() - abajoB
                    );

                    assertTrue(
                            separacionVertical + tolerancia >= kerf,
                            "No se respetó el ancho de corte vertical."
                    );
                }

                if (solapamientoVertical) {

                    double separacionHorizontal = Math.max(
                            b.getX() - derechaA,
                            a.getX() - derechaB
                    );

                    assertTrue(
                            separacionHorizontal + tolerancia >= kerf,
                            "No se respetó el ancho de corte horizontal."
                    );
                }
            }
        }
    }

    // ==========================================
// GRUPO 3: VALIDACIÓN DEL ESPESOR
// ==========================================

    @Test
    void debeRechazarEspesorCero() {

        Tablero tablero = new Tablero(100, 100, 0, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 20, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

    @Test
    void debeRechazarEspesorNegativo() {

        Tablero tablero = new Tablero(100, 100, -15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 20, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

    @Test
    void debeRechazarEspesorNaN() {

        Tablero tablero = new Tablero(100, 100, Double.NaN, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 20, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

    @Test
    void debeRechazarEspesorInfinito() {

        Tablero tablero = new Tablero(
                100, 100, Double.POSITIVE_INFINITY, 3
        );

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 20, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

// ==========================================
// GRUPO 4: CASOS LÍMITE
// ==========================================

    @Test
    void debeRetornarListaVaciaSiElAnchoDeCorteEsInfinito() {

        Tablero tablero = new Tablero(
                100, 100, 15, Double.POSITIVE_INFINITY
        );

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 20, 20, 1)
        );

        assertTrue(optimizador.optimizar(tablero, piezas).isEmpty());
    }

    @Test
    void debeIgnorarPiezaConAnchoCero() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Inválida", 0, 20, 1),
                new Pieza("Válida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals("Válida", resultado.get(0).getPiezaOriginal().getNombre());
    }

    @Test
    void debeIgnorarPiezaConLargoCero() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Inválida", 20, 0, 1),
                new Pieza("Válida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals("Válida", resultado.get(0).getPiezaOriginal().getNombre());
    }

    @Test
    void debeIgnorarPiezaConAnchoNegativoInfinito() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Inválida", Double.NEGATIVE_INFINITY, 20, 1),
                new Pieza("Válida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals("Válida", resultado.get(0).getPiezaOriginal().getNombre());
    }

    @Test
    void debeIgnorarPiezaConLargoNegativoInfinito() {

        Tablero tablero = new Tablero(100, 100, 15, 3);

        List<Pieza> piezas = List.of(
                new Pieza("Inválida", 20, Double.NEGATIVE_INFINITY, 1),
                new Pieza("Válida", 20, 20, 1)
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.size());
        assertEquals("Válida", resultado.get(0).getPiezaOriginal().getNombre());
    }

// ==========================================
// GRUPO 5: CONSERVACIÓN DE CANTIDADES POR TIPO
// ==========================================

    @Test
    void debeConservarLaCantidadSolicitadaDeCadaTipoDePieza() {

        Tablero tablero = new Tablero(200, 200, 15, 3);

        Pieza piezaA = new Pieza("Repisa", 30, 20, 4);
        Pieza piezaB = new Pieza("Repisa", 40, 20, 3);
        Pieza piezaC = new Pieza("Costado", 50, 30, 2);

        List<Pieza> solicitadas = List.of(
                piezaA,
                piezaB,
                piezaC
        );

        List<PiezaColocada> resultado =
                optimizador.optimizar(tablero, solicitadas);

        Map<String, Integer> cantidadesSolicitadas = Map.of(
                "Repisa|30.0|20.0", 4,
                "Repisa|40.0|20.0", 3,
                "Costado|50.0|30.0", 2
        );

        Map<String, Integer> cantidadesColocadas = new java.util.HashMap<>();

        for (PiezaColocada colocada : resultado) {

            Pieza original = colocada.getPiezaOriginal();

            String clave = original.getNombre()
                    + "|" + original.getAncho()
                    + "|" + original.getLargo();

            cantidadesColocadas.merge(clave, 1, Integer::sum);
        }

        assertEquals(cantidadesSolicitadas, cantidadesColocadas);
        assertDistribucionValida(tablero, resultado);
    }

}