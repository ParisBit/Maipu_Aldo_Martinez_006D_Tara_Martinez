
package cl.tablerolab.optimizer;

import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Tablero;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OptimizadorMultiplesTablerosTest {

    private final OptimizadorMultiplesTableros optimizador =
            new OptimizadorMultiplesTableros();

    @Test
    void debeRechazarTableroNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> optimizador.optimizar(null, List.of())
        );
    }

    @Test
    void debeRechazarCantidadDeTablerosMenorAUno() {
        Tablero tablero = new Tablero(100, 100, 15, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizador.optimizar(tablero, List.of(), 0)
        );
    }

    @Test
    void debeRechazarCantidadDeTablerosMayorACinco() {
        Tablero tablero = new Tablero(100, 100, 15, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizador.optimizar(tablero, List.of(), 6)
        );
    }

    @Test
    void debeRetornarResultadoVacioCuandoNoHayPiezas() {
        Tablero tablero = new Tablero(100, 100, 15, 0);

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, List.of());

        assertNotNull(resultado);
        assertEquals(0, resultado.getCantidadTablerosUtilizados());
        assertEquals(0, resultado.getTotalUnidadesColocadas());
        assertEquals(0, resultado.getTotalUnidadesPendientes());
        assertEquals(0, resultado.getTotalUnidadesSolicitadas());
    }

    @Test
    void debeUtilizarVariosTablerosCuandoSeaNecesario() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 3)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(3, resultado.getCantidadTablerosUtilizados());
        assertEquals(3, resultado.getTotalUnidadesColocadas());
        assertEquals(0, resultado.getTotalUnidadesPendientes());
        assertEquals(3, resultado.getTotalUnidadesSolicitadas());
    }

    @Test
    void debeDistribuirLasPiezasSinDuplicarlasNiPerderlas() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 4),
                new Pieza("Pieza B", 5, 5, 2)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(6, resultado.getTotalUnidadesColocadas()
                + resultado.getTotalUnidadesPendientes());

        assertTrue(resultado.getCantidadTablerosUtilizados() <= 5);
    }

    @Test
    void noDebeSuperarCincoTableros() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 7)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(5, resultado.getCantidadTablerosUtilizados());
        assertEquals(5, resultado.getTotalUnidadesColocadas());
        assertEquals(2, resultado.getTotalUnidadesPendientes());
        assertEquals(7, resultado.getTotalUnidadesSolicitadas());
    }

    @Test
    void debeRespetarUnLimitePersonalizadoDeTableros() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 4)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas, 2);

        assertEquals(2, resultado.getCantidadTablerosUtilizados());
        assertEquals(2, resultado.getTotalUnidadesColocadas());
        assertEquals(2, resultado.getTotalUnidadesPendientes());
    }

    @Test
    void debeConservarPiezasQueNoCabenEnNingunTablero() {
        Tablero tablero = new Tablero(20, 20, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza grande", 30, 30, 2)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(0, resultado.getCantidadTablerosUtilizados());
        assertEquals(0, resultado.getTotalUnidadesColocadas());
        assertEquals(2, resultado.getTotalUnidadesPendientes());
        assertEquals(2, resultado.getTotalUnidadesSolicitadas());
    }

    @Test
    void debeDetenerseCuandoUnTableroNoConsigueColocarPiezas() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 1),
                new Pieza("Pieza B", 20, 20, 1)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(1, resultado.getCantidadTablerosUtilizados());
        assertEquals(1, resultado.getTotalUnidadesColocadas());
        assertEquals(1, resultado.getTotalUnidadesPendientes());
    }

    @Test
    void debeConservarCantidadesParcialesDeUnaMismaPieza() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 3)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas, 2);

        assertEquals(2, resultado.getTotalUnidadesColocadas());
        assertEquals(1, resultado.getTotalUnidadesPendientes());

        assertEquals(
                1,
                resultado.getPiezasPendientes().get(0).getCantidad()
        );
    }

    @Test
    void debeDistinguirPiezasConElMismoNombre() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Repetida", 10, 10, 1),
                new Pieza("Repetida", 5, 5, 1)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(2, resultado.getCantidadTablerosUtilizados());
        assertEquals(2, resultado.getTotalUnidadesColocadas());
        assertEquals(0, resultado.getTotalUnidadesPendientes());

        PiezaColocada primera =
                resultado.getPiezasPorTablero().get(0).get(0);

        PiezaColocada segunda =
                resultado.getPiezasPorTablero().get(1).get(0);

        assertEquals(10, primera.getAncho(), 0.000001);
        assertEquals(5, segunda.getAncho(), 0.000001);
    }

    @Test
    void debeMantenerConsistenciaEntreColocadasPendientesYSolicitadas() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 4)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertEquals(
                resultado.getTotalUnidadesSolicitadas(),
                resultado.getTotalUnidadesColocadas()
                        + resultado.getTotalUnidadesPendientes()
        );
    }

    @Test
    void debeEntregarListasNoModificables() {
        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Pieza A", 6, 6, 2)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.getPiezasPorTablero().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.getPiezasPendientes().clear()
        );
    }

    // NUEVAS PRUEBAS DE INTEGRIDAD

    @Test
    void debeConservarLaCantidadIndividualDeCadaPieza() {

        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("A", 6, 6, 3),
                new Pieza("B", 5, 5, 4),
                new Pieza("C", 20, 20, 2)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        Map<String, Integer> solicitadas = contarSolicitadas(piezas);
        Map<String, Integer> obtenidas = contarResultado(resultado);

        assertEquals(solicitadas, obtenidas);
    }

    @Test
    void noDebeDuplicarLaMismaUnidadEntreTableros() {

        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("A", 6, 6, 7)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        Set<Pieza> unidades = new HashSet<>();

        for (List<PiezaColocada> tableroPiezas :
                resultado.getPiezasPorTablero()) {

            for (PiezaColocada colocada : tableroPiezas) {

                assertNotNull(colocada);
                assertNotNull(colocada.getPiezaOriginal());

                assertTrue(
                        unidades.add(colocada.getPiezaOriginal()),
                        "Una misma unidad fue colocada más de una vez."
                );
            }
        }

        assertEquals(
                resultado.getTotalUnidadesColocadas(),
                unidades.size()
        );
    }

    @Test
    void debeConservarExactamenteLasUnidadesColocadasYPendientes() {

        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("A", 6, 6, 7),
                new Pieza("B", 5, 5, 3)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        Map<String, Integer> solicitadas = contarSolicitadas(piezas);
        Map<String, Integer> obtenidas = contarResultado(resultado);

        assertEquals(solicitadas, obtenidas);
    }

    @Test
    void debeDistinguirPiezasConIgualNombreYDimensionesDiferentes() {

        Tablero tablero = new Tablero(10, 10, 15, 0);

        List<Pieza> piezas = List.of(
                new Pieza("Repetida", 10, 10, 2),
                new Pieza("Repetida", 5, 5, 3)
        );

        ResultadoOptimizacion resultado =
                optimizador.optimizar(tablero, piezas);

        Map<String, Integer> solicitadas = contarSolicitadas(piezas);
        Map<String, Integer> obtenidas = contarResultado(resultado);

        assertEquals(solicitadas, obtenidas);
    }

    private Map<String, Integer> contarSolicitadas(List<Pieza> piezas) {

        Map<String, Integer> cantidades = new HashMap<>();

        for (Pieza pieza : piezas) {

            String clave = crearClave(pieza);

            cantidades.merge(
                    clave,
                    pieza.getCantidad(),
                    Integer::sum
            );
        }

        return cantidades;
    }

    private Map<String, Integer> contarResultado(
            ResultadoOptimizacion resultado
    ) {

        Map<String, Integer> cantidades = new HashMap<>();

        for (List<PiezaColocada> tablero :
                resultado.getPiezasPorTablero()) {

            for (PiezaColocada colocada : tablero) {

                assertNotNull(colocada);
                assertNotNull(colocada.getPiezaOriginal());

                cantidades.merge(
                        crearClave(colocada.getPiezaOriginal()),
                        1,
                        Integer::sum
                );
            }
        }

        for (Pieza pendiente : resultado.getPiezasPendientes()) {

            assertNotNull(pendiente);

            cantidades.merge(
                    crearClave(pendiente),
                    pendiente.getCantidad(),
                    Integer::sum
            );
        }

        return cantidades;
    }

    private String crearClave(Pieza pieza) {

        return pieza.getNombre() + "|"
                + pieza.getAncho() + "|"
                + pieza.getLargo();
    }
}
