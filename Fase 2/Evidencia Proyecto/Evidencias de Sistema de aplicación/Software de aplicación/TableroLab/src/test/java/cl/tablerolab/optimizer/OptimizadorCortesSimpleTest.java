    package cl.tablerolab.optimizer;

    import cl.tablerolab.model.Pieza;
    import cl.tablerolab.model.Tablero;
    import cl.tablerolab.model.PiezaColocada;

    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;

    class OptimizadorCortesSimpleTest {

        private final OptimizadorCortesSimple optimizador =
                new OptimizadorCortesSimple();

        @Test
        void debeRetornarListaVaciaSiTableroEsNulo() {

            List<PiezaColocada> resultado =
                    optimizador.optimizar(null, List.of());

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeRetornarListaVaciaSiNoHayPiezas() {

            Tablero tablero = new Tablero(
                    183, 250, 15, 3
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, List.of());

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeColocarTodasLasPiezasCuandoHayEspacio() {

            Tablero tablero = new Tablero(
                    183, 250, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Lateral", 40, 80, 2),
                    new Pieza("Base", 60, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(3, resultado.size());
        }

        @Test
        void ningunaPiezaDebeExcederLosLimitesDelTablero() {

            Tablero tablero = new Tablero(
                    183, 250, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 80, 60, 2),
                    new Pieza("Pieza B", 50, 40, 2)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            for (PiezaColocada pieza : resultado) {

                assertTrue(pieza.getX() >= 0);
                assertTrue(pieza.getY() >= 0);

                assertTrue(
                        pieza.getX() + pieza.getAncho()
                                <= tablero.getAncho()
                );

                assertTrue(
                        pieza.getY() + pieza.getLargo()
                                <= tablero.getLargo()
                );
            }
        }

        @Test
        void debeRegistrarCuandoUnaPiezaEsRotada() {

            Tablero tablero = new Tablero(100, 100, 15, 3);

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 30, 100, 1),
                    new Pieza("Pieza girada", 90, 30, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());

            PiezaColocada segundaPieza = resultado.get(1);

            assertTrue(segundaPieza.isRotada());
            assertEquals(30, segundaPieza.getAncho());
            assertEquals(90, segundaPieza.getLargo());
        }

        @Test
        void lasPiezasNoDebenSuperponerse() {

            Tablero tablero = new Tablero(
                    183, 250, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 80, 60, 2),
                    new Pieza("Pieza B", 50, 40, 2)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            for (int i = 0; i < resultado.size(); i++) {

                PiezaColocada a = resultado.get(i);

                for (int j = i + 1; j < resultado.size(); j++) {

                    PiezaColocada b = resultado.get(j);

                    boolean seSuperponen =
                            a.getX() < b.getX() + b.getAncho() &&
                                    a.getX() + a.getAncho() > b.getX() &&
                                    a.getY() < b.getY() + b.getLargo() &&
                                    a.getY() + a.getLargo() > b.getY();

                    assertFalse(
                            seSuperponen,
                            "Se detectó una superposición entre dos piezas."
                    );
                }
            }
        }


        @Test
        void debeAprovecharEspaciosDisponiblesDelTablero() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 0
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 60, 1),
                    new Pieza("Pieza B", 40, 40, 1),
                    new Pieza("Pieza C", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(3, resultado.size());

            for (PiezaColocada pieza : resultado) {

                assertTrue(pieza.getX() >= 0);
                assertTrue(pieza.getY() >= 0);

                assertTrue(
                        pieza.getX() + pieza.getAncho() <= 100
                );

                assertTrue(
                        pieza.getY() + pieza.getLargo() <= 100
                );
            }
        }


        @Test
        void lasPiezasNoDebenSuperponerseConEspaciosLibres() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 0
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 60, 1),
                    new Pieza("Pieza B", 40, 40, 1),
                    new Pieza("Pieza C", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            for (int i = 0; i < resultado.size(); i++) {

                PiezaColocada a = resultado.get(i);

                for (int j = i + 1; j < resultado.size(); j++) {

                    PiezaColocada b = resultado.get(j);

                    boolean seSuperponen =
                            a.getX() < b.getX() + b.getAncho() &&
                                    a.getX() + a.getAncho() > b.getX() &&
                                    a.getY() < b.getY() + b.getLargo() &&
                                    a.getY() + a.getLargo() > b.getY();

                    assertFalse(
                            seSuperponen,
                            "Se detectó una superposición entre dos piezas."
                    );
                }
            }
        }

        @Test
        void debeRespetarElAnchoDeCorteEntrePiezas() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 60, 1),
                    new Pieza("Pieza B", 39.7, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());

            PiezaColocada a = resultado.get(0);
            PiezaColocada b = resultado.get(1);

            double separacionHorizontal = Math.max(
                    b.getX() - (a.getX() + a.getAncho()),
                    a.getX() - (b.getX() + b.getAncho())
            );

            double separacionVertical = Math.max(
                    b.getY() - (a.getY() + a.getLargo()),
                    a.getY() - (b.getY() + b.getLargo())
            );

            assertTrue(
                    separacionHorizontal >= 0.3 - 0.000001 ||
                            separacionVertical >= 0.3 - 0.000001,
                    "Las piezas deben respetar los 3 mm de ancho de corte."
            );
        }

        @Test
        void debeIgnorarPiezasConDimensionesInvalidas() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza válida", 40, 40, 1),
                    new Pieza("Ancho cero", 0, 40, 1),
                    new Pieza("Largo negativo", 40, -10, 1)
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
        void debeIgnorarPiezasNulasDentroDeLaLista() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = java.util.Arrays.asList(
                    new Pieza("Pieza válida", 40, 40, 1),
                    null,
                    new Pieza("Otra pieza válida", 30, 30, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());
        }

        @Test
        void debeRetornarListaVaciaSiLasDimensionesDelTableroNoSonFinitas() {

            Tablero tableroNaN = new Tablero(
                    Double.NaN, 100, 15, 3
            );

            Tablero tableroInfinito = new Tablero(
                    100, Double.POSITIVE_INFINITY, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 40, 40, 1)
            );

            assertTrue(
                    optimizador.optimizar(tableroNaN, piezas).isEmpty()
            );

            assertTrue(
                    optimizador.optimizar(tableroInfinito, piezas).isEmpty()
            );
        }

        @Test
        void debeRetornarListaVaciaSiElAnchoDeCorteEsNegativo() {

            Tablero tablero = new Tablero(
                    100, 100, 15, -3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeRetornarListaVaciaSiElAnchoDeCorteNoEsFinito() {

            Tablero tablero = new Tablero(
                    100, 100, 15, Double.POSITIVE_INFINITY
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeRetornarListaVaciaSiElAnchoDeCorteEsNaN() {

            Tablero tablero = new Tablero(
                    100, 100, 15, Double.NaN
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeIgnorarPiezasConAnchoInfinito() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Invalida", Double.POSITIVE_INFINITY, 40, 1),
                    new Pieza("Pieza Valida", 30, 30, 1)
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
        void debeIgnorarPiezasConLargoInfinito() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Invalida", 40, Double.POSITIVE_INFINITY, 1),
                    new Pieza("Pieza Valida", 30, 30, 1)
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
        void debeIgnorarPiezasConAnchoNaN() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Invalida", Double.NaN, 40, 1),
                    new Pieza("Pieza Valida", 30, 30, 1)
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
        void debeIgnorarPiezasConLargoNaN() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Invalida", 40, Double.NaN, 1),
                    new Pieza("Pieza Valida", 30, 30, 1)
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
        void noDebeColocarMasPiezasQueLasSolicitadas() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 20, 20, 2)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());

            assertTrue(
                    resultado.stream().allMatch(
                            p -> p.getPiezaOriginal().getNombre().equals("Pieza A")
                    )
            );
        }

        @Test
        void debeIgnorarPiezasConCantidadCero() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 30, 30, 0)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeIgnorarPiezasConCantidadNegativa() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 30, 30, -2)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertTrue(resultado.isEmpty());
        }

        @Test
        void ningunaPiezaDebeQuedarFueraDelTablero() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 40, 2),
                    new Pieza("Pieza B", 30, 30, 2),
                    new Pieza("Pieza C", 20, 20, 2)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            for (PiezaColocada colocada : resultado) {

                assertTrue(colocada.getX() >= 0);
                assertTrue(colocada.getY() >= 0);

                assertTrue(
                        colocada.getX() + colocada.getAncho() <=
                                tablero.getAncho() + 0.000001
                );

                assertTrue(
                        colocada.getY() + colocada.getLargo() <=
                                tablero.getLargo() + 0.000001
                );
            }
        }

        @Test
        void debeIgnorarPiezasQueNoCabenEnElTablero() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Grande", 120, 120, 1),
                    new Pieza("Pieza Valida", 40, 40, 1)
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
        void debeRetornarListaVaciaCuandoLaListaDePiezasEstaVacia() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of();

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        void debeIntercambiarLasDimensionesCuandoUnaPiezaEsRotada() {

            Tablero tablero = new Tablero(
                    100, 60, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 40, 80, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(1, resultado.size());

            PiezaColocada colocada = resultado.get(0);

            assertTrue(colocada.isRotada());
            assertEquals(80, colocada.getAncho(), 0.000001);
            assertEquals(40, colocada.getLargo(), 0.000001);
        }

        @Test
        void dosPiezasNoDebenTenerElMismoOrigen() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 30, 30, 1),
                    new Pieza("Pieza B", 30, 30, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());

            PiezaColocada primera = resultado.get(0);
            PiezaColocada segunda = resultado.get(1);

            assertFalse(
                    primera.getX() == segunda.getX() &&
                            primera.getY() == segunda.getY()
            );
        }

        @Test
        void debeColocarUnaPiezaQueOcupaTodoElTablero() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza Completa", 100, 100, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(1, resultado.size());

            PiezaColocada colocada = resultado.get(0);

            assertEquals(0, colocada.getX(), 0.000001);
            assertEquals(0, colocada.getY(), 0.000001);
            assertEquals(100, colocada.getAncho(), 0.000001);
            assertEquals(100, colocada.getLargo(), 0.000001);
        }

        @Test
        void debeColocarDosPiezasCuandoElAnchoMasElCorteOcupanElTablero() {

            Tablero tablero = new Tablero(
                    100, 50, 15, 3
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 50, 1),
                    new Pieza("Pieza B", 39.7, 50, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(2, resultado.size());

            assertTrue(
                    resultado.get(0).getX() +
                            resultado.get(0).getAncho() <= 60.000001
            );

            assertTrue(
                    resultado.get(1).getX() >= 60.3 - 0.000001
            );
        }

        @Test
        void debeAprovecharEspaciosLibresDeDistintasRegiones() {

            Tablero tablero = new Tablero(
                    100, 100, 15, 0
            );

            List<Pieza> piezas = List.of(
                    new Pieza("Pieza A", 60, 60, 1),
                    new Pieza("Pieza B", 40, 40, 1),
                    new Pieza("Pieza C", 40, 40, 1)
            );

            List<PiezaColocada> resultado =
                    optimizador.optimizar(tablero, piezas);

            assertEquals(3, resultado.size());
        }

    }

