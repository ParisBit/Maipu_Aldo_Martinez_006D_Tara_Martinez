
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;



public class HolaMuebleDesigner extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    private final GestorPiezas gestorPiezas = new GestorPiezas();

    private final GestorConexiones gestorConexiones =
            new GestorConexiones(gestorPiezas);

    private final DibujadorPiezas dibujadorPiezas =
            new DibujadorPiezas();

    private final ValidadorDimensiones validadorDimensiones =
            new ValidadorDimensiones();

    private DetectorColisiones detectorColisiones;
    private DetectorProximidad detectorProximidad;

    private double escala = 50;
    private double zoom = 1.0;
    private final double zoomMinimo = 0.5;
    private final double zoomMaximo = 2.0;
    private double distanciaProximidad = 20;

    private int contadorPiezas = 0;

    private Pieza piezaSeleccionada = null;
    private Pieza piezaArrastrada = null;

    // EDITOR INTERACTIVO DE POLÍGONOS

    private boolean modoDibujoPoligono = false;

    private final List<Vertice> verticesTemporales =
            new ArrayList<>();

    private double offsetX;
    private double offsetY;

    private TextField anchoField;
    private TextField altoField;
    private TextField largoField;

    private Button zoomActualButton;

    @Override
    public void start(Stage stage) {

        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();

        detectorColisiones = new DetectorColisiones(escala);
        detectorProximidad =
                new DetectorProximidad(distanciaProximidad);

        // PANEL DE PROPIEDADES

        Label tituloPropiedades = new Label("Propiedades");

        anchoField = new TextField();
        altoField = new TextField();
        largoField = new TextField();

        Button aplicarButton = new Button("Aplicar cambios");
        aplicarButton.setOnAction(event -> aplicarCambios());

        VBox panelPropiedades = new VBox(
                10,
                tituloPropiedades,
                new Label("Ancho (cm):"),
                anchoField,
                new Label("Alto (cm):"),
                altoField,
                new Label("Largo (cm):"),
                largoField,
                aplicarButton
        );

        // PANEL PARA CREAR PIEZAS

        Label tituloCrear = new Label("Crear pieza");

        Label etiquetaTipoFigura = new Label("Tipo de figura:");

        javafx.scene.control.ComboBox<String> selectorFigura =
                new javafx.scene.control.ComboBox<>();

        selectorFigura.getItems().addAll(
                "Rectángulo",
                "Círculo",
                "Polígono"
        );

        selectorFigura.setValue("Rectángulo");
        selectorFigura.setMaxWidth(Double.MAX_VALUE);

        TextField nuevoAnchoField = new TextField("8");
        TextField nuevoAltoField = new TextField("2");
        TextField nuevoLargoField = new TextField("2");

        Button crearButton = new Button("Crear pieza");

        crearButton.setOnAction(event -> {
            try {
                double ancho =
                        Double.parseDouble(nuevoAnchoField.getText());
                double alto =
                        Double.parseDouble(nuevoAltoField.getText());
                double largo =
                        Double.parseDouble(nuevoLargoField.getText());

                if (!validadorDimensiones.sonValidas(
                        ancho, alto, largo)) {
                    System.out.println(
                            "Las dimensiones deben ser números finitos "
                                    + "mayores que cero."
                    );
                    return;
                }

                contadorPiezas++;

                double x = 50 + contadorPiezas * 100;
                double y = 50;

                Pieza nuevaPieza = new Pieza(x, y);

                nuevaPieza.setLargoCm(largo);
                nuevaPieza.setColor(Color.GRAY);

                String tipoFigura = selectorFigura.getValue();

                switch (tipoFigura) {
                    case "Rectángulo":
                        nuevaPieza.setAnchoCm(ancho);
                        nuevaPieza.setAltoCm(alto);

                        nuevaPieza.setGeometria(
                                new Rectangulo(ancho, alto)
                        );
                        break;

                    case "Círculo":
                        double radio = ancho / 2.0;

                        nuevaPieza.setGeometria(
                                new Circulo(radio)
                        );
                        break;

                    case "Polígono":
                        nuevaPieza.setGeometria(
                                new Poligono(List.of(
                                        new Vertice(0, 0),
                                        new Vertice(ancho, 0),
                                        new Vertice(ancho * 0.75, alto),
                                        new Vertice(ancho * 0.20, alto * 0.65)
                                ))
                        );
                        break;

                    default:
                        System.out.println("Selecciona una figura válida.");
                        return;
                }

                actualizarGeometriaPieza(nuevaPieza);
                gestorPiezas.agregarPieza(nuevaPieza);


                dibujarPiezas();

                System.out.println("Nueva pieza creada");

            } catch (NumberFormatException e) {
                System.out.println(
                        "Introduce dimensiones numéricas válidas."
                );
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "No se pudieron aplicar las dimensiones: "
                                + e.getMessage()
                );
            }
        });

        VBox panelCrear = new VBox(
                10,
                tituloCrear,
                etiquetaTipoFigura,
                selectorFigura,
                new Label("Ancho (cm):"),
                nuevoAnchoField,
                new Label("Alto (cm):"),
                nuevoAltoField,
                new Label("Largo (cm):"),
                nuevoLargoField,
                crearButton
        );

        // PANEL DE CONEXIONES

        Label tituloConexiones = new Label("Conexiones");

        Button iniciarPoligonoButton =
                new Button("Iniciar polígono");

        Button finalizarPoligonoButton =
                new Button("Finalizar polígono");

        Button cancelarPoligonoButton =
                new Button("Cancelar dibujo");


        cancelarPoligonoButton.setOnAction(event -> {

            if (!modoDibujoPoligono) {
                System.out.println("No hay ningún dibujo en curso.");
                return;
            }

            modoDibujoPoligono = false;
            verticesTemporales.clear();

            dibujarPiezas();

            System.out.println("Dibujo de polígono cancelado.");
        });

        finalizarPoligonoButton.setOnAction(event -> {

            if (!modoDibujoPoligono) {
                System.out.println("Primero inicia el dibujo.");
                return;
            }

            if (verticesTemporales.size() < 3) {
                System.out.println(
                        "Un polígono necesita al menos 3 vértices."
                );
                return;
            }

            try {

                Pieza nuevaPieza = new Pieza(0, 0);

                nuevaPieza.setColor(Color.GRAY);
                nuevaPieza.setLargoCm(2);


                // Calcular la posición de referencia.
                double minimoX = Double.MAX_VALUE;
                double minimoY = Double.MAX_VALUE;

                for (Vertice vertice : verticesTemporales) {
                    minimoX = Math.min(minimoX, vertice.getX());
                    minimoY = Math.min(minimoY, vertice.getY());
                }

                // Trasladar los vértices a coordenadas locales.
                List<Vertice> verticesLocales = new ArrayList<>();

                for (Vertice vertice : verticesTemporales) {
                    verticesLocales.add(
                            new Vertice(
                                    (vertice.getX() - minimoX) / escala,
                                    (vertice.getY() - minimoY) / escala
                            )
                    );
                }

                nuevaPieza.setGeometria(
                        new Poligono(verticesLocales)
                );

                nuevaPieza.setX(minimoX);
                nuevaPieza.setY(minimoY);

                actualizarGeometriaPieza(nuevaPieza);
                gestorPiezas.agregarPieza(nuevaPieza);

                contadorPiezas++;

                System.out.println("Polígono creado correctamente.");

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "No se pudo crear el polígono: " + e.getMessage()
                );

            } finally {
                modoDibujoPoligono = false;
                verticesTemporales.clear();
                dibujarPiezas();
            }
        });

        iniciarPoligonoButton.setOnAction(event -> {
            modoDibujoPoligono = true;
            verticesTemporales.clear();

            piezaSeleccionada = null;
            piezaArrastrada = null;

            for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
                pieza.setSeleccionada(false);
            }

            dibujarPiezas();

            System.out.println(
                    "Modo de dibujo de polígono activado."
            );
        });

        Button separarPiezaButton = new Button("Separar pieza");
        Button desconectarTodoButton =
                new Button("Desconectar todo");
        Button eliminarPiezaButton =
                new Button("Eliminar pieza");

        separarPiezaButton.setOnAction(event -> {
            if (piezaSeleccionada == null) {
                System.out.println(
                        "Selecciona una pieza primero."
                );
                return;
            }

            int eliminadas =
                    gestorConexiones.separarPieza(piezaSeleccionada);

            dibujarPiezas();

            System.out.println(
                    "Conexiones eliminadas de la pieza: " + eliminadas
            );
        });

        desconectarTodoButton.setOnAction(event -> {
            int anteriores =
                    gestorConexiones.getConexiones().size();

            gestorConexiones.separarTodas();
            dibujarPiezas();

            System.out.println(
                    "Conexiones eliminadas: " + anteriores
            );
        });

        eliminarPiezaButton.setOnAction(
                event -> eliminarPiezaSeleccionada()
        );

        VBox panelConexiones = new VBox(
                10,
                tituloConexiones,
                separarPiezaButton,
                desconectarTodoButton,
                eliminarPiezaButton,
                iniciarPoligonoButton,
                finalizarPoligonoButton,
                cancelarPoligonoButton
        );

        // ESTILO DE LOS TÍTULOS

        String estiloTitulo =
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;";

        tituloPropiedades.setStyle(estiloTitulo);
        tituloCrear.setStyle(estiloTitulo);
        tituloConexiones.setStyle(estiloTitulo);

        // ESTILO Y DISTRIBUCIÓN DE LOS PANELES

        panelPropiedades.setPadding(new Insets(0, 0, 15, 0));
        panelCrear.setPadding(new Insets(0, 0, 15, 0));
        panelConexiones.setPadding(new Insets(0, 0, 15, 0));

        VBox panelDerecho = new VBox(
                25,
                panelPropiedades,
                panelCrear,
                panelConexiones
        );

        panelDerecho.setPadding(new Insets(15));
        panelDerecho.setSpacing(20);
        panelDerecho.setMinWidth(220);
        panelDerecho.setPrefWidth(280);
        panelDerecho.setStyle("-fx-background-color: #2D2D30;");

        // AJUSTAR CAMPOS Y BOTONES

        anchoField.setMaxWidth(Double.MAX_VALUE);
        altoField.setMaxWidth(Double.MAX_VALUE);
        largoField.setMaxWidth(Double.MAX_VALUE);

        nuevoAnchoField.setMaxWidth(Double.MAX_VALUE);
        nuevoAltoField.setMaxWidth(Double.MAX_VALUE);
        nuevoLargoField.setMaxWidth(Double.MAX_VALUE);

        for (Node nodo : panelDerecho.getChildren()) {
            if (nodo instanceof VBox seccion) {
                for (Node control : seccion.getChildren()) {
                    if (control instanceof Button boton) {
                        boton.setMaxWidth(Double.MAX_VALUE);
                        boton.setWrapText(true);
                    }

                    if (control instanceof Label etiqueta) {
                        etiqueta.setWrapText(true);
                        etiqueta.setMaxWidth(Double.MAX_VALUE);
                        etiqueta.setStyle("-fx-text-fill: white;");
                    }

                    if (control instanceof TextField campo) {
                        campo.setMaxWidth(Double.MAX_VALUE);
                    }
                }
            }
        }

        crearButton.setMaxWidth(Double.MAX_VALUE);
        crearButton.setWrapText(true);

        aplicarButton.setMaxWidth(Double.MAX_VALUE);
        aplicarButton.setWrapText(true);

        // DESPLAZAMIENTO VERTICAL DEL PANEL LATERAL

        ScrollPane desplazamientoPanel = new ScrollPane(panelDerecho);
        desplazamientoPanel.setFitToWidth(true);
        desplazamientoPanel.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        desplazamientoPanel.setStyle(
                "-fx-background: #2D2D30;"
                        + "-fx-background-color: #2D2D30;"
        );

        // PIEZAS INICIALES

        Pieza pieza1 = new Pieza(100, 200);
        pieza1.setAnchoCm(8);
        pieza1.setAltoCm(2);
        pieza1.setLargoCm(2);
        pieza1.setColor(Color.BROWN);

        actualizarGeometriaPieza(pieza1);
        gestorPiezas.agregarPieza(pieza1);

        Pieza pieza2 = new Pieza(500, 200);
        pieza2.setAnchoCm(4);
        pieza2.setAltoCm(4);
        pieza2.setLargoCm(2);
        pieza2.setColor(Color.RED);

        actualizarGeometriaPieza(pieza2);
        gestorPiezas.agregarPieza(pieza2);

        // BARRA SUPERIOR Y CONTROLES DE ZOOM

        Label tituloAplicacion = new Label("MuebleDesigner");

        tituloAplicacion.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;"
        );

        Button alejarButton = new Button("−");
        zoomActualButton = new Button("100%");
        Button acercarButton = new Button("+");

        alejarButton.setOnAction(event -> cambiarZoom(-0.1));
        acercarButton.setOnAction(event -> cambiarZoom(0.1));

        zoomActualButton.setOnAction(event -> {
            zoom = 1.0;
            dibujarPiezas();
            actualizarBotonZoom(zoomActualButton);
        });

        HBox controlesZoom = new HBox(
                8,
                alejarButton,
                zoomActualButton,
                acercarButton
        );

        controlesZoom.setAlignment(Pos.CENTER);

        HBox barraSuperior = new HBox(
                20,
                tituloAplicacion,
                controlesZoom
        );

        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setPadding(new Insets(15));
        barraSuperior.setStyle(
                "-fx-background-color: #252526;"
        );

        // ÁREA DE TRABAJO ADAPTABLE

        StackPane areaTrabajo = new StackPane(canvas);
        areaTrabajo.setMinWidth(0);
        areaTrabajo.setStyle(
                "-fx-background-color: #1E1E1E;"
        );

        canvas.widthProperty().bind(areaTrabajo.widthProperty());
        canvas.heightProperty().bind(areaTrabajo.heightProperty());

        canvas.widthProperty().addListener(
                (obs, anterior, nuevo) -> dibujarPiezas()
        );

        canvas.heightProperty().addListener(
                (obs, anterior, nuevo) -> dibujarPiezas()
        );

        // SEPARADOR REDIMENSIONABLE

        SplitPane contenidoPrincipal = new SplitPane();
        contenidoPrincipal.getItems().addAll(
                areaTrabajo,
                desplazamientoPanel
        );
        contenidoPrincipal.setDividerPositions(0.75);

        // SELECCIÓN DE PIEZAS

        canvas.setOnMousePressed(event -> {

            if (modoDibujoPoligono) {

                double mouseX =
                        (event.getX() - canvas.getWidth() / 2.0) / zoom
                                + canvas.getWidth() / 2.0;

                double mouseY =
                        (event.getY() - canvas.getHeight() / 2.0) / zoom
                                + canvas.getHeight() / 2.0;

                // Evitar vértices consecutivos repetidos.
                if (!verticesTemporales.isEmpty()) {

                    Vertice ultimo = verticesTemporales.get(
                            verticesTemporales.size() - 1
                    );

                    double distancia = Math.hypot(
                            mouseX - ultimo.getX(),
                            mouseY - ultimo.getY()
                    );

                    if (distancia < 5) {
                        System.out.println(
                                "El vértice está demasiado cerca del anterior."
                        );
                        return;
                    }
                }

                verticesTemporales.add(
                        new Vertice(mouseX, mouseY)
                );

                dibujarPiezas();

                System.out.println(
                        "Vértice agregado: "
                                + verticesTemporales.size()
                );

                return;
            }

            double mouseX =
                    (event.getX() - canvas.getWidth() / 2.0) / zoom
                            + canvas.getWidth() / 2.0;

            double mouseY =
                    (event.getY() - canvas.getHeight() / 2.0) / zoom
                            + canvas.getHeight() / 2.0;

            for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
                pieza.setSeleccionada(false);
            }

            piezaSeleccionada = null;
            piezaArrastrada = null;

            for (int i =
                 gestorPiezas.obtenerCantidadPiezas() - 1;
                 i >= 0; i--) {

                Pieza pieza =
                        gestorPiezas.obtenerPiezas().get(i);

                Geometria geometria = pieza.getGeometria();

                boolean dentro = geometria != null
                        && geometria.contienePunto(
                        mouseX,
                        mouseY,
                        pieza.getX(),
                        pieza.getY(),
                        escala
                );

                if (dentro) {
                    pieza.setSeleccionada(true);
                    piezaSeleccionada = pieza;
                    piezaArrastrada = pieza;

                    offsetX = mouseX - pieza.getX();
                    offsetY = mouseY - pieza.getY();

                    anchoField.setText(
                            String.valueOf(pieza.getAnchoCm())
                    );
                    altoField.setText(
                            String.valueOf(pieza.getAltoCm())
                    );
                    largoField.setText(
                            String.valueOf(pieza.getLargoCm())
                    );

                    System.out.println("Pieza seleccionada");
                    break;
                }
            }

            dibujarPiezas();
        });

        // ARRASTRE DE PIEZAS

        canvas.setOnMouseDragged(event -> {
            if (piezaArrastrada == null) {
                return;
            }

            double anteriorX = piezaArrastrada.getX();
            double anteriorY = piezaArrastrada.getY();

            double mouseX =
                    (event.getX() - canvas.getWidth() / 2.0) / zoom
                            + canvas.getWidth() / 2.0;

            double mouseY =
                    (event.getY() - canvas.getHeight() / 2.0) / zoom
                            + canvas.getHeight() / 2.0;

            piezaArrastrada.setX(mouseX - offsetX);
            piezaArrastrada.setY(mouseY - offsetY);

            double dx = piezaArrastrada.getX() - anteriorX;
            double dy = piezaArrastrada.getY() - anteriorY;

            actualizarGeometriaPieza(piezaArrastrada);

            gestorConexiones.moverPiezasConectadas(
                    piezaArrastrada,
                    dx,
                    dy,
                    escala
            );

            aplicarImanVertices();
            dibujarPiezas();
        });

        // SOLTAR PIEZAS

        canvas.setOnMouseReleased(event -> {
            if (piezaArrastrada != null) {
                crearConexionSiCorresponde();
                dibujarPiezas();
            }

            piezaArrastrada = null;

            System.out.println("Pieza soltada");
        });

        // CONTENEDOR PRINCIPAL

        BorderPane root = new BorderPane();
        root.setTop(barraSuperior);
        root.setCenter(contenidoPrincipal);
        root.setStyle("-fx-background-color: #1E1E1E;");

        Scene scene = new Scene(root, 1200, 700);

        stage.setTitle("MuebleDesigner");
        stage.setScene(scene);
        stage.show();

        dibujarPiezas();
    }

    // ELIMINAR LA PIEZA SELECCIONADA

    private void eliminarPiezaSeleccionada() {
        if (piezaSeleccionada == null) {
            System.out.println(
                    "Selecciona una pieza para eliminar."
            );
            return;
        }

        Pieza piezaEliminada = piezaSeleccionada;

        int conexionesAntes =
                gestorConexiones.getConexiones().size();

        boolean eliminada = gestorPiezas.eliminarPieza(
                piezaEliminada,
                gestorConexiones
        );

        if (!eliminada) {
            System.out.println("No se pudo eliminar la pieza.");
            return;
        }

        int conexionesEliminadas =
                conexionesAntes
                        - gestorConexiones.getConexiones().size();

        piezaEliminada.setSeleccionada(false);

        piezaSeleccionada = null;
        piezaArrastrada = null;

        anchoField.clear();
        altoField.clear();
        largoField.clear();

        dibujarPiezas();

        System.out.println("Pieza eliminada.");
        System.out.println(
                "Conexiones eliminadas: " + conexionesEliminadas
        );
        System.out.println(
                "Piezas restantes: "
                        + gestorPiezas.obtenerCantidadPiezas()
        );
    }

    // APLICAR CAMBIOS DE DIMENSIONES

    private void aplicarCambios() {
        if (piezaSeleccionada == null) {
            System.out.println("Selecciona una pieza primero.");
            return;
        }

        try {
            double ancho =
                    Double.parseDouble(anchoField.getText());
            double alto =
                    Double.parseDouble(altoField.getText());
            double largo =
                    Double.parseDouble(largoField.getText());

            if (!validadorDimensiones.sonValidas(
                    ancho, alto, largo)) {
                System.out.println(
                        "Las dimensiones deben ser números finitos "
                                + "mayores que cero."
                );
                return;
            }

            piezaSeleccionada.setAnchoCm(ancho);
            piezaSeleccionada.setAltoCm(alto);
            piezaSeleccionada.setLargoCm(largo);

            actualizarGeometriaPieza(piezaSeleccionada);
            dibujarPiezas();

            System.out.println("Cambios aplicados");

        } catch (NumberFormatException e) {
            System.out.println(
                    "Introduce valores numéricos válidos."
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "No se pudieron aplicar los cambios: "
                            + e.getMessage()
            );
        }
    }

    // ACTUALIZAR GEOMETRÍA


    private void actualizarGeometriaPieza(Pieza pieza) {
        Geometria geometria = pieza.getGeometria();

        if (geometria instanceof Rectangulo) {
            Rectangulo rectangulo = (Rectangulo) geometria;

            rectangulo.setAnchoCm(pieza.getAnchoCm());
            rectangulo.setAltoCm(pieza.getAltoCm());

        } else if (geometria instanceof Circulo) {
            Circulo circulo = (Circulo) geometria;

            circulo.setRadioCm(pieza.getAnchoCm() / 2.0);
        }

        pieza.actualizarGeometria(escala);
    }


    // DIBUJAR Y DETECTAR COLISIONES Y PROXIMIDAD

    private void dibujarPiezas() {
        if (gc == null || canvas == null) {
            return;
        }

        double anchoCanvas = canvas.getWidth();
        double altoCanvas = canvas.getHeight();

        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, anchoCanvas, altoCanvas);

        // FONDO

        gc.setFill(Color.web("#1E1E1E"));
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        // CUADRÍCULA

        gc.setStroke(Color.web("#353538"));
        gc.setLineWidth(1);

        double separacion = 25;

        for (double x = 0; x <= anchoCanvas; x += separacion) {
            gc.strokeLine(x, 0, x, altoCanvas);
        }

        for (double y = 0; y <= altoCanvas; y += separacion) {
            gc.strokeLine(0, y, anchoCanvas, y);
        }

        // APLICAR ZOOM A LAS PIEZAS
        gc.save();

        gc.translate(
                canvas.getWidth() / 2.0,
                canvas.getHeight() / 2.0
        );

        gc.scale(zoom, zoom);

        gc.translate(
                -canvas.getWidth() / 2.0,
                -canvas.getHeight() / 2.0
        );

        // ACTUALIZAR GEOMETRÍA Y ESTADOS

        for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
            pieza.setEnColision(false);
            pieza.setCerca(false);

            actualizarGeometriaPieza(pieza);

            if (pieza.getGeometria() instanceof Rectangulo) {
                ((Rectangulo) pieza.getGeometria())
                        .restablecerProximidadVertices();

            } else if (pieza.getGeometria() instanceof Circulo) {
                ((Circulo) pieza.getGeometria())
                        .restablecerProximidadPuntos();

            } else if (pieza.getGeometria() instanceof Poligono) {
                for (Vertice vertice :
                        ((Poligono) pieza.getGeometria()).obtenerVertices()) {

                    vertice.setCerca(false);
                }
            }
        }

        // DETECTAR COLISIONES Y PROXIMIDAD

        for (int i = 0;
             i < gestorPiezas.obtenerCantidadPiezas(); i++) {

            for (int j = i + 1;
                 j < gestorPiezas.obtenerCantidadPiezas(); j++) {

                Pieza piezaA =
                        gestorPiezas.obtenerPiezas().get(i);

                Pieza piezaB =
                        gestorPiezas.obtenerPiezas().get(j);

                if (detectorColisiones.hayColision(
                        piezaA, piezaB)) {

                    piezaA.setEnColision(true);
                    piezaB.setEnColision(true);

                } else if (detectorProximidad
                        .piezasEstanCercaPorElementos(
                                piezaA, piezaB)) {

                    piezaA.setCerca(true);
                    piezaB.setCerca(true);

                    detectarVerticesCercanos(piezaA, piezaB);
                }
            }
        }

        // DIBUJAR PIEZAS

        // DIBUJAR PIEZAS

        for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
            dibujadorPiezas.dibujarPieza(
                    gc, pieza, escala
            );
        }

// DIBUJAR POLÍGONO TEMPORAL

        if (modoDibujoPoligono && !verticesTemporales.isEmpty()) {

            gc.setStroke(Color.YELLOW);
            gc.setFill(Color.YELLOW);
            gc.setLineWidth(2);

            for (int i = 0; i < verticesTemporales.size(); i++) {

                Vertice vertice = verticesTemporales.get(i);

                double x = vertice.getX();
                double y = vertice.getY();

                // Dibujar el vértice.
                gc.fillOval(x - 4, y - 4, 8, 8);

                // Dibujar el segmento hacia el vértice anterior.
                if (i > 0) {

                    Vertice anterior = verticesTemporales.get(i - 1);

                    gc.strokeLine(
                            anterior.getX(),
                            anterior.getY(),
                            x,
                            y
                    );
                }
            }

            // Previsualizar el cierre del polígono.
            if (verticesTemporales.size() >= 3) {

                Vertice primero = verticesTemporales.get(0);
                Vertice ultimo = verticesTemporales.get(
                        verticesTemporales.size() - 1
                );

                gc.setLineDashes(5, 5);

                gc.strokeLine(
                        ultimo.getX(),
                        ultimo.getY(),
                        primero.getX(),
                        primero.getY()
                );

                gc.setLineDashes(null);
            }
        }

        gc.restore();

    }

    // DETECTAR VÉRTICES CERCANOS

    private void detectarVerticesCercanos(Pieza a, Pieza b) {

        List<ElementoConexion> elementosA =
                a.obtenerElementosConexion();

        List<ElementoConexion> elementosB =
                b.obtenerElementosConexion();

        for (int i = 0; i < elementosA.size(); i++) {
            for (int j = 0; j < elementosB.size(); j++) {

                ElementoConexion elementoA = elementosA.get(i);
                ElementoConexion elementoB = elementosB.get(j);

                if (detectorProximidad.elementosEstanCerca(
                        elementoA, elementoB)) {

                    marcarElementoCercano(a, i);
                    marcarElementoCercano(b, j);
                }
            }
        }
    }

    private void marcarElementoCercano(Pieza pieza, int indice) {

        ElementoConexion elemento =
                pieza.obtenerElementosConexion().get(indice);

        if (elemento.getTipo() != ElementoConexion.Tipo.PUNTO) {
            return;
        }

        if (pieza.getGeometria() instanceof Rectangulo
                && indice < 4) {

            ((Rectangulo) pieza.getGeometria())
                    .marcarVerticeCercano(indice, true);

        } else if (pieza.getGeometria() instanceof Circulo) {

            ((Circulo) pieza.getGeometria())
                    .marcarPuntoCercano(indice, true);

        } else if (pieza.getGeometria() instanceof Poligono) {

            ((Poligono) pieza.getGeometria())
                    .obtenerVertices()
                    .get(indice)
                    .setCerca(true);
        }
    }

    private void comprobarVertices(
            Vertice a,
            Vertice b,
            Rectangulo rectanguloA,
            int indiceA,
            Rectangulo rectanguloB,
            int indiceB) {

        if (detectorProximidad.verticesEstanCerca(a, b)) {
            rectanguloA.marcarVerticeCercano(indiceA, true);
            rectanguloB.marcarVerticeCercano(indiceB, true);
        }
    }

    // IMÁN DE VÉRTICES

    private void aplicarImanVertices() {
        if (piezaArrastrada == null) return;

        ElementoConexion elementoMovido = null;
        ElementoConexion elementoObjetivo = null;

        double menorDistancia =
                detectorProximidad.getDistanciaProximidad();

        for (Pieza otra : gestorPiezas.obtenerPiezas()) {
            if (otra == piezaArrastrada) {
                continue;
            }

            List<ElementoConexion> elementosA =
                    piezaArrastrada.obtenerElementosConexion();

            List<ElementoConexion> elementosB =
                    otra.obtenerElementosConexion();

            for (ElementoConexion a : elementosA) {
                if (a.getTipo() != ElementoConexion.Tipo.PUNTO) {
                    continue;
                }

                for (ElementoConexion b : elementosB) {
                    if (b.getTipo() != ElementoConexion.Tipo.PUNTO) {
                        continue;
                    }

                    double dx = b.getX1() - a.getX1();
                    double dy = b.getY1() - a.getY1();

                    double distancia = Math.hypot(dx, dy);

                    if (distancia < menorDistancia) {
                        menorDistancia = distancia;
                        elementoMovido = a;
                        elementoObjetivo = b;
                    }
                }
            }
        }

        if (elementoMovido != null && elementoObjetivo != null) {
            double dx =
                    elementoObjetivo.getX1() - elementoMovido.getX1();

            double dy =
                    elementoObjetivo.getY1() - elementoMovido.getY1();

            gestorConexiones.moverGrupoConectado(
                    piezaArrastrada, dx, dy, escala
            );
        }
    }

    // CREAR CONEXIONES AUTOMÁTICAMENTE

    private void crearConexionSiCorresponde() {
        if (piezaArrastrada == null) {
            return;
        }

        double menorDistancia =
                detectorProximidad.getDistanciaProximidad();

        Pieza piezaA = null;
        Pieza piezaB = null;
        int indiceA = -1;
        int indiceB = -1;

        List<ElementoConexion> elementosA =
                piezaArrastrada.obtenerElementosConexion();

        for (Pieza otra : gestorPiezas.obtenerPiezas()) {
            if (otra == piezaArrastrada) {
                continue;
            }

            List<ElementoConexion> elementosB =
                    otra.obtenerElementosConexion();

            for (int i = 0; i < elementosA.size(); i++) {
                ElementoConexion a = elementosA.get(i);

                if (a.getTipo() != ElementoConexion.Tipo.PUNTO) {
                    continue;
                }

                for (int j = 0; j < elementosB.size(); j++) {
                    ElementoConexion b = elementosB.get(j);

                    if (b.getTipo() != ElementoConexion.Tipo.PUNTO) {
                        continue;
                    }

                    double dx = b.getX1() - a.getX1();
                    double dy = b.getY1() - a.getY1();

                    double distancia = Math.hypot(dx, dy);

                    if (distancia < menorDistancia) {
                        menorDistancia = distancia;
                        piezaA = piezaArrastrada;
                        piezaB = otra;
                        indiceA = i;
                        indiceB = j;
                    }
                }
            }
        }

        if (piezaA != null && piezaB != null) {
            boolean agregada = gestorConexiones.agregarConexion(
                    piezaA, indiceA, piezaB, indiceB
            );

            if (agregada) {
                System.out.println("Conexión creada");
                System.out.println("Total de conexiones: "
                        + gestorConexiones.getConexiones().size());
            }
        }
    }

    // CONTROLES DE ZOOM (ESTADO Y PORCENTAJE)

    private void cambiarZoom(double cambio) {
        zoom += cambio;

        if (zoom < zoomMinimo) {
            zoom = zoomMinimo;
        }

        if (zoom > zoomMaximo) {
            zoom = zoomMaximo;
        }

        dibujarPiezas();
        actualizarBotonZoom(zoomActualButton);
    }

    private void actualizarBotonZoom(Button boton) {
        boton.setText(
                String.format(
                        java.util.Locale.ROOT,
                        "%.0f%%",
                        zoom * 100
                )
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
