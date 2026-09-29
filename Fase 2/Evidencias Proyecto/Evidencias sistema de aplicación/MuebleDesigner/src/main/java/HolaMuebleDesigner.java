import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class HolaMuebleDesigner extends Application {

    // ==================================================
    // CANVAS
    // ==================================================

    private Canvas canvas;
    private GraphicsContext gc;


    // ==================================================
    // PIEZAS
    // ==================================================

    private ArrayList<Pieza> piezas =
            new ArrayList<>();


    // ==================================================
    // CONEXIONES
    // ==================================================

    private ArrayList<Conexion> conexiones =
            new ArrayList<>();


    // ==================================================
    // ESCALA
    // ==================================================

    private double escala = 50;


    // ==================================================
    // DISTANCIA DE PROXIMIDAD
    // ==================================================

    private double distanciaProximidad = 20;


    // ==================================================
    // CONTADOR DE PIEZAS
    // ==================================================

    private int contadorPiezas = 0;


    // ==================================================
    // PIEZA SELECCIONADA
    // ==================================================

    private Pieza piezaSeleccionada = null;


    // ==================================================
    // PIEZA ARRASTRADA
    // ==================================================

    private Pieza piezaArrastrada = null;


    // ==================================================
    // OFFSET DEL MOUSE
    // ==================================================

    private double offsetX;
    private double offsetY;


    @Override
    public void start(Stage stage) {

        // ==================================================
        // CREAR CANVAS
        // ==================================================

        canvas =
                new Canvas(
                        800,
                        600
                );


        gc =
                canvas.getGraphicsContext2D();


        // ==================================================
        // PANEL DE PROPIEDADES
        // ==================================================

        Label tituloPropiedades =
                new Label(
                        "Propiedades"
                );


        Label anchoLabel =
                new Label(
                        "Ancho:"
                );

        TextField anchoField =
                new TextField();


        Label altoLabel =
                new Label(
                        "Alto:"
                );

        TextField altoField =
                new TextField();


        Label largoLabel =
                new Label(
                        "Largo:"
                );

        TextField largoField =
                new TextField();


        Button aplicarButton =
                new Button(
                        "Aplicar cambios"
                );


        // ==================================================
        // APLICAR CAMBIOS
        // ==================================================

        aplicarButton.setOnAction(event -> {

            if (
                    piezaSeleccionada != null
            ) {

                try {

                    piezaSeleccionada.anchoCm =
                            Double.parseDouble(
                                    anchoField.getText()
                            );


                    piezaSeleccionada.altoCm =
                            Double.parseDouble(
                                    altoField.getText()
                            );


                    piezaSeleccionada.largoCm =
                            Double.parseDouble(
                                    largoField.getText()
                            );


                    dibujarPiezas();


                    System.out.println(
                            "Cambios aplicados"
                    );

                } catch (
                        NumberFormatException e
                ) {

                    System.out.println(
                            "Introduce valores numéricos válidos"
                    );
                }
            }
        });


        // ==================================================
        // CREAR PIEZA
        // ==================================================

        Label tituloCrear =
                new Label(
                        "Crear pieza"
                );


        Label nuevoAnchoLabel =
                new Label(
                        "Ancho:"
                );

        TextField nuevoAnchoField =
                new TextField();


        Label nuevoAltoLabel =
                new Label(
                        "Alto:"
                );

        TextField nuevoAltoField =
                new TextField();


        Label nuevoLargoLabel =
                new Label(
                        "Largo:"
                );

        TextField nuevoLargoField =
                new TextField();


        Button crearButton =
                new Button(
                        "Crear pieza"
                );


        // ==================================================
        // CREAR NUEVA PIEZA
        // ==================================================

        crearButton.setOnAction(event -> {

            try {

                double nuevoAncho =
                        Double.parseDouble(
                                nuevoAnchoField.getText()
                        );


                double nuevoAlto =
                        Double.parseDouble(
                                nuevoAltoField.getText()
                        );


                double nuevoLargo =
                        Double.parseDouble(
                                nuevoLargoField.getText()
                        );


                Pieza nuevaPieza =
                        new Pieza();


                contadorPiezas++;


                nuevaPieza.x =
                        50 +
                                contadorPiezas * 100;


                nuevaPieza.y =
                        50;


                nuevaPieza.anchoCm =
                        nuevoAncho;


                nuevaPieza.altoCm =
                        nuevoAlto;


                nuevaPieza.largoCm =
                        nuevoLargo;


                nuevaPieza.color =
                        Color.GRAY;


                piezas.add(
                        nuevaPieza
                );


                dibujarPiezas();


                System.out.println(
                        "Nueva pieza creada"
                );


            } catch (
                    NumberFormatException e
            ) {

                System.out.println(
                        "Introduce valores numéricos válidos."
                );
            }
        });


        // ==================================================
        // PANEL DE PROPIEDADES
        // ==================================================

        VBox panelPropiedades =
                new VBox(
                        10,
                        tituloPropiedades,
                        anchoLabel,
                        anchoField,
                        altoLabel,
                        altoField,
                        largoLabel,
                        largoField,
                        aplicarButton
                );


        // ==================================================
        // PANEL CREAR
        // ==================================================

        VBox panelCrear =
                new VBox(
                        10,
                        tituloCrear,
                        nuevoAnchoLabel,
                        nuevoAnchoField,
                        nuevoAltoLabel,
                        nuevoAltoField,
                        nuevoLargoLabel,
                        nuevoLargoField,
                        crearButton
                );


        VBox panelDerecho =
                new VBox(
                        30,
                        panelPropiedades,
                        panelCrear
                );


        // ==================================================
        // PRIMERA PIEZA
        // ==================================================

        Pieza pieza1 =
                new Pieza();


        pieza1.x = 100;
        pieza1.y = 200;


        pieza1.anchoCm = 8;
        pieza1.altoCm = 2;
        pieza1.largoCm = 2;


        pieza1.color =
                Color.BROWN;


        piezas.add(
                pieza1
        );


        // ==================================================
        // SEGUNDA PIEZA
        // ==================================================

        Pieza pieza2 =
                new Pieza();


        pieza2.x = 500;
        pieza2.y = 200;


        pieza2.anchoCm = 4;
        pieza2.altoCm = 4;
        pieza2.largoCm = 2;


        pieza2.color =
                Color.RED;


        piezas.add(
                pieza2
        );


        // ==================================================
        // DIBUJAR
        // ==================================================

        dibujarPiezas();


        // ==================================================
        // MOUSE PRESIONADO
        // ==================================================

        canvas.setOnMousePressed(event -> {

            double mouseX =
                    event.getX();

            double mouseY =
                    event.getY();


            // Quitar selección anterior

            for (
                    Pieza pieza : piezas
            ) {

                pieza.seleccionada =
                        false;
            }


            piezaSeleccionada =
                    null;


            piezaArrastrada =
                    null;


            // ==================================================
            // BUSCAR PIEZA
            // ==================================================

            for (
                    Pieza pieza : piezas
            ) {

                double anchoPx =
                        pieza.anchoCm *
                                escala;


                double altoPx =
                        pieza.altoCm *
                                escala;


                if (
                        mouseX >= pieza.x
                                &&
                                mouseX <=
                                        pieza.x + anchoPx
                                &&
                                mouseY >= pieza.y
                                &&
                                mouseY <=
                                        pieza.y + altoPx
                ) {

                    pieza.seleccionada =
                            true;


                    piezaSeleccionada =
                            pieza;


                    piezaArrastrada =
                            pieza;


                    offsetX =
                            mouseX -
                                    pieza.x;


                    offsetY =
                            mouseY -
                                    pieza.y;


                    anchoField.setText(
                            String.valueOf(
                                    pieza.anchoCm
                            )
                    );


                    altoField.setText(
                            String.valueOf(
                                    pieza.altoCm
                            )
                    );


                    largoField.setText(
                            String.valueOf(
                                    pieza.largoCm
                            )
                    );


                    System.out.println(
                            "Pieza seleccionada"
                    );


                    break;
                }
            }


            dibujarPiezas();
        });


        // ==================================================
        // MOUSE ARRASTRADO
        // ==================================================

        canvas.setOnMouseDragged(event -> {

            if (
                    piezaArrastrada != null
            ) {

                // Posición anterior

                double posicionAnteriorX =
                        piezaArrastrada.x;


                double posicionAnteriorY =
                        piezaArrastrada.y;


                // Nueva posición

                piezaArrastrada.x =
                        event.getX() -
                                offsetX;


                piezaArrastrada.y =
                        event.getY() -
                                offsetY;


                // Desplazamiento

                double desplazamientoX =
                        piezaArrastrada.x -
                                posicionAnteriorX;


                double desplazamientoY =
                        piezaArrastrada.y -
                                posicionAnteriorY;


                // Actualizar vértices

                piezaArrastrada.actualizarVertices(
                        escala
                );


                // Mover piezas conectadas

                moverPiezasConectadas(
                        piezaArrastrada,
                        desplazamientoX,
                        desplazamientoY
                );


                // Aplicar imán

                aplicarImanVertices();


                // Dibujar

                dibujarPiezas();
            }
        });


        // ==================================================
        // MOUSE SOLTADO
        // ==================================================

        canvas.setOnMouseReleased(event -> {

            if (
                    piezaArrastrada != null
            ) {

                crearConexionSiCorresponde();
            }


            piezaArrastrada =
                    null;


            System.out.println(
                    "Pieza soltada"
            );
        });


        // ==================================================
        // CREAR VENTANA
        // ==================================================

        HBox root =
                new HBox(
                        20,
                        canvas,
                        panelDerecho
                );


        Scene scene =
                new Scene(
                        root,
                        1000,
                        650
                );


        stage.setTitle(
                "MuebleDesigner"
        );


        stage.setScene(
                scene
        );


        stage.show();
    }


    // ==================================================
    // DIBUJAR PIEZAS
    // ==================================================

    private void dibujarPiezas() {

        gc.clearRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );


        // ==================================================
        // ACTUALIZAR VÉRTICES
        // ==================================================

        for (
                Pieza pieza : piezas
        ) {

            pieza.enColision =
                    false;


            pieza.cerca =
                    false;


            pieza.actualizarVertices(
                    escala
            );


            pieza.verticeA.cerca =
                    false;


            pieza.verticeB.cerca =
                    false;


            pieza.verticeC.cerca =
                    false;


            pieza.verticeD.cerca =
                    false;
        }


        // ==================================================
        // DETECTAR RELACIONES ENTRE PIEZAS
        // ==================================================

        for (
                int i = 0;
                i < piezas.size();
                i++
        ) {

            for (
                    int j = i + 1;
                    j < piezas.size();
                    j++
            ) {

                Pieza piezaA =
                        piezas.get(i);


                Pieza piezaB =
                        piezas.get(j);


                if (
                        hayColision(
                                piezaA,
                                piezaB
                        )
                ) {

                    piezaA.enColision =
                            true;


                    piezaB.enColision =
                            true;

                } else if (
                        estanCerca(
                                piezaA,
                                piezaB
                        )
                ) {

                    piezaA.cerca =
                            true;


                    piezaB.cerca =
                            true;


                    detectarVerticesCercanos(
                            piezaA,
                            piezaB
                    );
                }
            }
        }


        // ==================================================
        // DIBUJAR
        // ==================================================

        for (
                Pieza pieza : piezas
        ) {

            double anchoPx =
                    pieza.anchoCm *
                            escala;


            double altoPx =
                    pieza.altoCm *
                            escala;


            // Relleno

            gc.setFill(
                    pieza.color
            );


            gc.fillRect(
                    pieza.x,
                    pieza.y,
                    anchoPx,
                    altoPx
            );


            // Borde

            if (
                    pieza.enColision
            ) {

                gc.setStroke(
                        Color.RED
                );

                gc.setLineWidth(
                        4
                );

            } else if (
                    pieza.cerca
            ) {

                gc.setStroke(
                        Color.YELLOW
                );

                gc.setLineWidth(
                        4
                );

            } else {

                gc.setStroke(
                        Color.BLACK
                );

                gc.setLineWidth(
                        1
                );
            }


            gc.strokeRect(
                    pieza.x,
                    pieza.y,
                    anchoPx,
                    altoPx
            );


            // Selección

            if (
                    pieza.seleccionada
                            &&
                            !pieza.enColision
                            &&
                            !pieza.cerca
            ) {

                gc.setStroke(
                        Color.BLUE
                );

                gc.setLineWidth(
                        4
                );


                gc.strokeRect(
                        pieza.x,
                        pieza.y,
                        anchoPx,
                        altoPx
                );
            }


            // Vértices

            double radio =
                    5;


            dibujarVertice(
                    pieza.verticeA,
                    radio
            );


            dibujarVertice(
                    pieza.verticeB,
                    radio
            );


            dibujarVertice(
                    pieza.verticeC,
                    radio
            );


            dibujarVertice(
                    pieza.verticeD,
                    radio
            );
        }
    }


    // ==================================================
    // DIBUJAR VÉRTICE
    // ==================================================

    private void dibujarVertice(
            Vertice vertice,
            double radio
    ) {

        if (
                vertice.cerca
        ) {

            gc.setFill(
                    Color.YELLOW
            );

        } else {

            gc.setFill(
                    Color.BLACK
            );
        }


        gc.fillOval(
                vertice.x - radio,
                vertice.y - radio,
                radio * 2,
                radio * 2
        );
    }


    // ==================================================
    // DETECTAR COLISIÓN
    // ==================================================

    private boolean hayColision(
            Pieza piezaA,
            Pieza piezaB
    ) {

        double anchoA =
                piezaA.anchoCm *
                        escala;


        double altoA =
                piezaA.altoCm *
                        escala;


        double anchoB =
                piezaB.anchoCm *
                        escala;


        double altoB =
                piezaB.altoCm *
                        escala;


        return piezaA.x <
                piezaB.x + anchoB

                &&

                piezaA.x + anchoA >
                        piezaB.x

                &&

                piezaA.y <
                        piezaB.y + altoB

                &&

                piezaA.y + altoA >
                        piezaB.y;
    }


    // ==================================================
    // DETECTAR PROXIMIDAD
    // ==================================================

    private boolean estanCerca(
            Pieza piezaA,
            Pieza piezaB
    ) {

        double anchoA =
                piezaA.anchoCm *
                        escala;


        double altoA =
                piezaA.altoCm *
                        escala;


        double anchoB =
                piezaB.anchoCm *
                        escala;


        double altoB =
                piezaB.altoCm *
                        escala;


        double izquierdaA =
                piezaA.x;


        double derechaA =
                piezaA.x + anchoA;


        double arribaA =
                piezaA.y;


        double abajoA =
                piezaA.y + altoA;


        double izquierdaB =
                piezaB.x;


        double derechaB =
                piezaB.x + anchoB;


        double arribaB =
                piezaB.y;


        double abajoB =
                piezaB.y + altoB;


        double distanciaX;


        if (
                derechaA <
                        izquierdaB
        ) {

            distanciaX =
                    izquierdaB -
                            derechaA;

        } else if (
                derechaB <
                        izquierdaA
        ) {

            distanciaX =
                    izquierdaA -
                            derechaB;

        } else {

            distanciaX =
                    0;
        }


        double distanciaY;


        if (
                abajoA <
                        arribaB
        ) {

            distanciaY =
                    arribaB -
                            abajoA;

        } else if (
                abajoB <
                        arribaA
        ) {

            distanciaY =
                    arribaA -
                            abajoB;

        } else {

            distanciaY =
                    0;
        }


        double distancia =
                Math.sqrt(
                        distanciaX *
                                distanciaX
                                +
                                distanciaY *
                                        distanciaY
                );


        return distancia <=
                distanciaProximidad;
    }


    // ==================================================
    // DETECTAR VÉRTICES CERCANOS
    // ==================================================

    private void detectarVerticesCercanos(
            Pieza piezaA,
            Pieza piezaB
    ) {

        comprobarVertices(
                piezaA.verticeA,
                piezaB.verticeA
        );


        comprobarVertices(
                piezaA.verticeA,
                piezaB.verticeB
        );


        comprobarVertices(
                piezaA.verticeA,
                piezaB.verticeC
        );


        comprobarVertices(
                piezaA.verticeA,
                piezaB.verticeD
        );


        comprobarVertices(
                piezaA.verticeB,
                piezaB.verticeA
        );


        comprobarVertices(
                piezaA.verticeB,
                piezaB.verticeB
        );


        comprobarVertices(
                piezaA.verticeB,
                piezaB.verticeC
        );


        comprobarVertices(
                piezaA.verticeB,
                piezaB.verticeD
        );


        comprobarVertices(
                piezaA.verticeC,
                piezaB.verticeA
        );


        comprobarVertices(
                piezaA.verticeC,
                piezaB.verticeB
        );


        comprobarVertices(
                piezaA.verticeC,
                piezaB.verticeC
        );


        comprobarVertices(
                piezaA.verticeC,
                piezaB.verticeD
        );


        comprobarVertices(
                piezaA.verticeD,
                piezaB.verticeA
        );


        comprobarVertices(
                piezaA.verticeD,
                piezaB.verticeB
        );


        comprobarVertices(
                piezaA.verticeD,
                piezaB.verticeC
        );


        comprobarVertices(
                piezaA.verticeD,
                piezaB.verticeD
        );
    }


    // ==================================================
    // COMPROBAR VÉRTICES
    // ==================================================

    private void comprobarVertices(
            Vertice verticeA,
            Vertice verticeB
    ) {

        double distancia =
                distanciaEntreVertices(
                        verticeA,
                        verticeB
                );


        if (
                distancia <=
                        distanciaProximidad
        ) {

            verticeA.cerca =
                    true;


            verticeB.cerca =
                    true;
        }
    }


    // ==================================================
    // DISTANCIA ENTRE VÉRTICES
    // ==================================================

    private double distanciaEntreVertices(
            Vertice verticeA,
            Vertice verticeB
    ) {

        double diferenciaX =
                verticeA.x -
                        verticeB.x;


        double diferenciaY =
                verticeA.y -
                        verticeB.y;


        return Math.sqrt(
                diferenciaX *
                        diferenciaX
                        +
                        diferenciaY *
                                diferenciaY
        );
    }


    // ==================================================
    // IMÁN DE VÉRTICES
    // ==================================================

    private void aplicarImanVertices() {

        if (
                piezaArrastrada == null
        ) {

            return;
        }


        Vertice verticeArrastrado =
                null;


        Vertice verticeObjetivo =
                null;


        double menorDistancia =
                distanciaProximidad;


        for (
                Pieza pieza : piezas
        ) {

            if (
                    pieza ==
                            piezaArrastrada
            ) {

                continue;
            }


            Vertice[] verticesArrastrados = {

                    piezaArrastrada.verticeA,
                    piezaArrastrada.verticeB,
                    piezaArrastrada.verticeC,
                    piezaArrastrada.verticeD
            };


            Vertice[] verticesObjetivo = {

                    pieza.verticeA,
                    pieza.verticeB,
                    pieza.verticeC,
                    pieza.verticeD
            };


            for (
                    Vertice vArrastrado :
                    verticesArrastrados
            ) {

                for (
                        Vertice vObjetivo :
                        verticesObjetivo
                ) {

                    double distancia =
                            distanciaEntreVertices(
                                    vArrastrado,
                                    vObjetivo
                            );


                    if (
                            distancia <
                                    menorDistancia
                    ) {

                        menorDistancia =
                                distancia;


                        verticeArrastrado =
                                vArrastrado;


                        verticeObjetivo =
                                vObjetivo;
                    }
                }
            }
        }


        if (
                verticeArrastrado != null
                        &&
                        verticeObjetivo != null
        ) {

            double desplazamientoX =
                    verticeObjetivo.x -
                            verticeArrastrado.x;


            double desplazamientoY =
                    verticeObjetivo.y -
                            verticeArrastrado.y;


            piezaArrastrada.x +=
                    desplazamientoX;


            piezaArrastrada.y +=
                    desplazamientoY;


            piezaArrastrada.actualizarVertices(
                    escala
            );
        }
    }


    // ==================================================
    // CREAR CONEXIÓN
    // ==================================================

    private void crearConexionSiCorresponde() {

        if (
                piezaArrastrada == null
        ) {

            return;
        }


        double menorDistancia =
                distanciaProximidad;


        Pieza piezaA =
                null;


        Pieza piezaB =
                null;


        int indiceVerticeA =
                -1;


        int indiceVerticeB =
                -1;


        for (
                Pieza pieza : piezas
        ) {

            if (
                    pieza ==
                            piezaArrastrada
            ) {

                continue;
            }


            Vertice[] verticesA = {

                    piezaArrastrada.verticeA,
                    piezaArrastrada.verticeB,
                    piezaArrastrada.verticeC,
                    piezaArrastrada.verticeD
            };


            Vertice[] verticesB = {

                    pieza.verticeA,
                    pieza.verticeB,
                    pieza.verticeC,
                    pieza.verticeD
            };


            for (
                    int i = 0;
                    i < verticesA.length;
                    i++
            ) {

                for (
                        int j = 0;
                        j < verticesB.length;
                        j++
                ) {

                    double distancia =
                            distanciaEntreVertices(
                                    verticesA[i],
                                    verticesB[j]
                            );


                    if (
                            distancia <
                                    menorDistancia
                    ) {

                        menorDistancia =
                                distancia;


                        piezaA =
                                piezaArrastrada;


                        piezaB =
                                pieza;


                        indiceVerticeA =
                                i;


                        indiceVerticeB =
                                j;
                    }
                }
            }
        }


        // ==================================================
        // GUARDAR CONEXIÓN
        // ==================================================

        if (
                piezaA != null
                        &&
                        piezaB != null
                        &&
                        indiceVerticeA != -1
                        &&
                        indiceVerticeB != -1
        ) {

            Conexion nuevaConexion =
                    new Conexion(
                            piezaA,
                            indiceVerticeA,
                            piezaB,
                            indiceVerticeB
                    );


            conexiones.add(
                    nuevaConexion
            );


            System.out.println(
                    "Conexión creada"
            );


            System.out.println(
                    "Total de conexiones: "
                            +
                            conexiones.size()
            );
        }
    }


    // ==================================================
    // MOVER PIEZAS CONECTADAS
    // ==================================================

    private void moverPiezasConectadas(
            Pieza piezaMovida,
            double desplazamientoX,
            double desplazamientoY
    ) {

        for (
                Conexion conexion : conexiones
        ) {

            Pieza otraPieza =
                    null;


            // ==================================================
            // PIEZA A
            // ==================================================

            if (
                    conexion.piezaA ==
                            piezaMovida
            ) {

                otraPieza =
                        conexion.piezaB;
            }


            // ==================================================
            // PIEZA B
            // ==================================================

            else if (
                    conexion.piezaB ==
                            piezaMovida
            ) {

                otraPieza =
                        conexion.piezaA;
            }


            // ==================================================
            // MOVER OTRA PIEZA
            // ==================================================

            if (
                    otraPieza != null
            ) {

                otraPieza.x +=
                        desplazamientoX;


                otraPieza.y +=
                        desplazamientoY;


                otraPieza.actualizarVertices(
                        escala
                );
            }
        }
    }


    // ==================================================
    // MAIN
    // ==================================================

    public static void main(
            String[] args
    ) {

        launch();
    }
}
