
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
    private double distanciaProximidad = 20;

    private int contadorPiezas = 0;

    private Pieza piezaSeleccionada = null;
    private Pieza piezaArrastrada = null;

    private double offsetX;
    private double offsetY;

    private TextField anchoField;
    private TextField altoField;
    private TextField largoField;

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
                new Label("Ancho:"),
                anchoField,
                new Label("Alto:"),
                altoField,
                new Label("Largo:"),
                largoField,
                aplicarButton
        );

        // PANEL PARA CREAR PIEZAS

        Label tituloCrear = new Label("Crear pieza");

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
                        ancho, alto, largo
                )) {
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

                nuevaPieza.setAnchoCm(ancho);
                nuevaPieza.setAltoCm(alto);
                nuevaPieza.setLargoCm(largo);
                nuevaPieza.setColor(Color.GRAY);

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
                new Label("Ancho:"),
                nuevoAnchoField,
                new Label("Alto:"),
                nuevoAltoField,
                new Label("Largo:"),
                nuevoLargoField,
                crearButton
        );

        // PANEL DE CONEXIONES

        Label tituloConexiones = new Label("Conexiones");

        Button separarPiezaButton = new Button("Separar pieza");
        Button desconectarTodoButton =
                new Button("Desconectar todo");
        Button eliminarPiezaButton = new Button("Eliminar pieza");

        separarPiezaButton.setMaxWidth(Double.MAX_VALUE);
        desconectarTodoButton.setMaxWidth(Double.MAX_VALUE);
        eliminarPiezaButton.setMaxWidth(Double.MAX_VALUE);

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
                eliminarPiezaButton
        );

        VBox panelDerecho = new VBox(
                30,
                panelPropiedades,
                panelCrear,
                panelConexiones
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

        dibujarPiezas();

        // SELECCIÓN DE PIEZAS

        canvas.setOnMousePressed(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

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

            piezaArrastrada.setX(event.getX() - offsetX);
            piezaArrastrada.setY(event.getY() - offsetY);

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

        HBox root = new HBox(20, canvas, panelDerecho);
        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("MuebleDesigner");
        stage.setScene(scene);
        stage.show();
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
                    ancho, alto, largo
            )) {
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
        }

        pieza.actualizarGeometria(escala);
    }

    // DIBUJAR Y DETECTAR COLISIONES Y PROXIMIDAD

    private void dibujarPiezas() {
        gc.clearRect(
                0, 0, canvas.getWidth(), canvas.getHeight()
        );

        for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
            pieza.setEnColision(false);
            pieza.setCerca(false);

            actualizarGeometriaPieza(pieza);

            // Restablecer los vértices de los rectángulos.
            if (pieza.getGeometria() instanceof Rectangulo) {
                for (int i = 0; i < 4; i++) {
                    Vertice vertice = pieza.obtenerVertice(i);

                    if (vertice != null) {
                        vertice.setCerca(false);
                    }
                }
            }
        }

        for (int i = 0;
             i < gestorPiezas.obtenerCantidadPiezas(); i++) {

            for (int j = i + 1;
                 j < gestorPiezas.obtenerCantidadPiezas(); j++) {

                Pieza piezaA =
                        gestorPiezas.obtenerPiezas().get(i);

                Pieza piezaB =
                        gestorPiezas.obtenerPiezas().get(j);

                if (detectorColisiones.hayColision(
                        piezaA, piezaB
                )) {
                    piezaA.setEnColision(true);
                    piezaB.setEnColision(true);

                } else if (detectorProximidad
                        .piezasEstanCercaPorElementos(
                                piezaA, piezaB
                        )) {

                    piezaA.setCerca(true);
                    piezaB.setCerca(true);

                    detectarVerticesCercanos(piezaA, piezaB);
                }
            }
        }

        for (Pieza pieza : gestorPiezas.obtenerPiezas()) {
            dibujadorPiezas.dibujarPieza(
                    gc, pieza, escala
            );
        }
    }

    // DETECTAR VÉRTICES CERCANOS

    private void detectarVerticesCercanos(Pieza a, Pieza b) {
        if (!(a.getGeometria() instanceof Rectangulo)
                || !(b.getGeometria() instanceof Rectangulo)) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                comprobarVertices(
                        a.obtenerVertice(i),
                        b.obtenerVertice(j)
                );
            }
        }
    }

    private void comprobarVertices(Vertice a, Vertice b) {
        if (detectorProximidad.verticesEstanCerca(a, b)) {
            a.setCerca(true);
            b.setCerca(true);
        }
    }

    // IMÁN DE VÉRTICES

    private void aplicarImanVertices() {
        if (piezaArrastrada == null) {
            return;
        }

        if (!(piezaArrastrada.getGeometria() instanceof Rectangulo)) {
            return;
        }

        Vertice verticeMovido = null;
        Vertice verticeObjetivo = null;

        double menorDistancia =
                detectorProximidad.getDistanciaProximidad();

        for (Pieza otra : gestorPiezas.obtenerPiezas()) {
            if (otra == piezaArrastrada
                    || !(otra.getGeometria() instanceof Rectangulo)) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                Vertice a = piezaArrastrada.obtenerVertice(i);

                for (int j = 0; j < 4; j++) {
                    Vertice b = otra.obtenerVertice(j);

                    double distancia =
                            detectorProximidad
                                    .distanciaEntreVertices(a, b);

                    if (distancia < menorDistancia) {
                        menorDistancia = distancia;
                        verticeMovido = a;
                        verticeObjetivo = b;
                    }
                }
            }
        }

        if (verticeMovido != null && verticeObjetivo != null) {
            double dx =
                    verticeObjetivo.getX() - verticeMovido.getX();
            double dy =
                    verticeObjetivo.getY() - verticeMovido.getY();

            gestorConexiones.moverGrupoConectado(
                    piezaArrastrada,
                    dx,
                    dy,
                    escala
            );
        }
    }

    // CREAR CONEXIONES AUTOMÁTICAMENTE

    private void crearConexionSiCorresponde() {
        if (piezaArrastrada == null
                || !(piezaArrastrada.getGeometria() instanceof Rectangulo)) {
            return;
        }

        double menorDistancia =
                detectorProximidad.getDistanciaProximidad();

        Pieza piezaA = null;
        Pieza piezaB = null;

        int indiceA = -1;
        int indiceB = -1;

        for (Pieza otra : gestorPiezas.obtenerPiezas()) {
            if (otra == piezaArrastrada
                    || !(otra.getGeometria() instanceof Rectangulo)) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                Vertice a = piezaArrastrada.obtenerVertice(i);

                for (int j = 0; j < 4; j++) {
                    Vertice b = otra.obtenerVertice(j);

                    double distancia =
                            detectorProximidad
                                    .distanciaEntreVertices(a, b);

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
            boolean agregada =
                    gestorConexiones.agregarConexion(
                            piezaA,
                            indiceA,
                            piezaB,
                            indiceB
                    );

            if (agregada) {
                System.out.println("Conexión creada");

                System.out.println(
                        "Total de conexiones: "
                                + gestorConexiones.getConexiones().size()
                );
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

