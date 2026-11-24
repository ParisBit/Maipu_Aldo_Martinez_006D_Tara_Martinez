package cl.tablerolab.ui;

import cl.tablerolab.model.Pieza;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.control.SplitPane;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ScrollPane;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import cl.tablerolab.service.ValidadorTablero;

import cl.tablerolab.model.Tablero;

import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.optimizer.OptimizadorMultiplesTableros;
import cl.tablerolab.optimizer.ResultadoOptimizacion;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import cl.tablerolab.model.CotizacionTablero;
import cl.tablerolab.model.ResumenCostos;
import cl.tablerolab.service.CalculadorCostos;

import cl.tablerolab.model.Retazo;
import cl.tablerolab.service.ExportadorPDF;

import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import cl.tablerolab.persistence.MaterialDAO;
import cl.tablerolab.persistence.FormatoTableroDAO;
import java.sql.SQLException;
import java.util.Map;

import cl.tablerolab.persistence.ProyectoService;

import javafx.scene.control.ChoiceDialog;

import cl.tablerolab.service.CalculadorRetazos;

import cl.tablerolab.persistence.ConfiguracionDAO;

import javafx.scene.control.ButtonBar;

import javafx.scene.control.ButtonType;

import cl.tablerolab.security.RespaldoService;
import cl.tablerolab.security.RestauracionPendienteService;



public class MainView {

    private final BorderPane root;

    private TextField anchoTableroField;
    private TextField largoTableroField;
    private TextField espesorField;
    private TextField kerfField;

    // Datos de cotización.
    private ComboBox<String> materialCombo;

    private ComboBox<String> formatoCombo;
    private Map<String, Integer> formatosIds = new java.util.LinkedHashMap<>();

    private Map<String, Integer> materialesIds = new java.util.LinkedHashMap<>();

    private TextField proveedorField;
    private TextField enlaceProveedorField;
    private TextField precioUnitarioField;
    private TextField tablerosAComprarField;

    // Gestión de costos.
    private CotizacionTablero cotizacionActual;

    private final CalculadorCostos calculadorCostos =
            new CalculadorCostos();

    private ResumenCostos resumenCostosActual;

    private final List<Pieza> piezas = new ArrayList<>();

    private final ValidadorTablero validadorTablero =
            new ValidadorTablero();

    private VBox listaPiezas;

    private VBox listaPiezasPendientes;

    private Label tituloPiezasPendientes;

    private Label tiposLabel;
    private Label unidadesLabel;
    private Label superficieLabel;

    private Label superficieTableroLabel;
    private Label superficieOcupadaLabel;
    private Label desperdicioLabel;
    private Label aprovechamientoLabel;

    private Label unidadesColocadasLabel;
    private Label unidadesPendientesLabel;

    // Resumen económico.
    private Label costoUnitarioLabel;
    private Label costoUtilizadoLabel;
    private Label costoCompraLabel;
    private Label diferenciaCostoLabel;

    private TextField nombreField;

    private TextField nombreProyectoField;

    private TextField anchoPiezaField;
    private TextField largoPiezaField;
    private TextField cantidadField;

    private ScrollPane panelTableroScroll;
    private ScrollPane panelPiezasScroll;
    private TabPane panelesCompactos;
    private boolean modoCompacto;

    private SplitPane panelPrincipal;
    private Parent areaTrabajo;

    private VBox contenedorCompacto;

    private Canvas canvasTablero;

    private Tablero tableroActual;

    private final OptimizadorMultiplesTableros optimizador =
            new OptimizadorMultiplesTableros();

    private ResultadoOptimizacion resultadoActual;

    private ComboBox<Integer> selectorTablero;

    private List<PiezaColocada> piezasColocadas =
            new ArrayList<>();

    private final CalculadorRetazos calculadorRetazos =
            new CalculadorRetazos();

    private final ExportadorPDF exportadorPDF =
            new ExportadorPDF();

    private List<Retazo> retazosActuales =
            new ArrayList<>();

    private Label resumenRetazosAprovechablesLabel;
    private Label resumenRetazosNoAprovechablesLabel;

    private double nivelZoom = 1.0;

    private double desplazamientoX = 0;
    private double desplazamientoY = 0;

    private double mouseAnteriorX;
    private double mouseAnteriorY;

    private Label tablerosUtilizadosLabel;
    private Label tablerosAComprarLabel;

    private final ProyectoService proyectoService =
            new ProyectoService();

    private final ConfiguracionDAO configuracionDAO =
            new ConfiguracionDAO();

    private final RespaldoService respaldoService =
            new RespaldoService();

    private final RestauracionPendienteService restauracionPendienteService =
            new RestauracionPendienteService();

    private Integer proyectoIdActual;

    private List<Retazo> calcularTodosLosRetazos() {
        List<Retazo> todosLosRetazos = new ArrayList<>();

        if (resultadoActual == null || tableroActual == null) {
            return todosLosRetazos;
        }

        List<List<PiezaColocada>> resultados =
                resultadoActual.getPiezasPorTablero();

        for (int i = 0; i < resultados.size(); i++) {
            List<Retazo> retazosTablero =
                    calculadorRetazos.calcularRetazos(
                            tableroActual,
                            resultados.get(i),
                            i + 1
                    );

            todosLosRetazos.addAll(retazosTablero);
        }

        return todosLosRetazos;
    }

    public MainView() {

        root = new BorderPane();

        ConfiguracionDAO.Preferencias preferencias =
                configuracionDAO.cargar();

        root.getStyleClass().add(
                preferencias.isTemaOscuro()
                        ? "tema-oscuro"
                        : "tema-claro"
        );

        crearBarraSuperior();
        crearPanelTablero();

        crearPanelTablero();

        kerfField.setText(
                String.valueOf(preferencias.getAnchoCorteMm())
        );

        crearAreaTrabajo();
        crearPanelPiezas();

        configurarDisenoAdaptable();
    }



    private void crearBarraSuperior() {
        Label titulo = new Label("TableroLab");
        titulo.getStyleClass().add("titulo-principal");

        nombreProyectoField = new TextField();
        nombreProyectoField.setPromptText("Nombre del proyecto");
        nombreProyectoField.setPrefWidth(220);

        Button nuevoButton = new Button("Nuevo");
        Button abrirButton = new Button("Abrir");
        Button guardarButton = new Button("Guardar");
        Button eliminarButton = new Button("Eliminar");
        Button exportarButton = new Button("Exportar PDF");
        Button respaldosButton = new Button("Respaldos");
        Button configuracionButton = new Button("Configuración");

        nuevoButton.setOnAction(event -> nuevoProyecto());
        abrirButton.setOnAction(event -> abrirProyecto());
        guardarButton.setOnAction(event -> guardarProyecto());
        eliminarButton.setOnAction(event -> eliminarProyecto());
        exportarButton.setOnAction(event -> exportarPDF());
        respaldosButton.setOnAction(event -> abrirRespaldos());
        configuracionButton.setOnAction(event -> abrirConfiguracion());

        HBox filaProyecto = new HBox(
                12,
                titulo,
                new Label("Proyecto:"),
                nombreProyectoField
        );
        filaProyecto.setAlignment(Pos.CENTER_LEFT);

        HBox filaAcciones = new HBox(
                8,
                nuevoButton,
                abrirButton,
                guardarButton,
                eliminarButton,
                exportarButton,
                respaldosButton,
                configuracionButton
        );
        filaAcciones.setAlignment(Pos.CENTER_LEFT);

        VBox barra = new VBox(10, filaProyecto, filaAcciones);
        barra.setPadding(new Insets(12, 15, 12, 15));
        barra.getStyleClass().add("panel");

        root.setTop(barra);
    }


    private void abrirRespaldos() {
        ButtonType crear = new ButtonType(
                "Crear respaldo",
                ButtonBar.ButtonData.LEFT
        );

        ButtonType restaurar = new ButtonType(
                "Restaurar respaldo",
                ButtonBar.ButtonData.LEFT
        );

        Alert dialogo = new Alert(
                Alert.AlertType.NONE,
                "Selecciona la operación que deseas realizar.",
                crear,
                restaurar,
                ButtonType.CLOSE
        );

        dialogo.setTitle("Respaldos");
        dialogo.setHeaderText("Administrar copias de seguridad");

        dialogo.showAndWait().ifPresent(respuesta -> {
            if (respuesta == crear) {
                crearRespaldo();
            } else if (respuesta == restaurar) {
                restaurarRespaldo();
            }
        });
    }




    private void abrirConfiguracion() {

        double anchoCorteActual;

        try {
            anchoCorteActual = Double.parseDouble(
                    kerfField.getText().trim().replace(",", ".")
            );
        } catch (NumberFormatException e) {
            anchoCorteActual = 3.0;
        }

        boolean temaOscuroActual =
                root.getStyleClass().contains("tema-oscuro");

        ConfiguracionView configuracionView =
                new ConfiguracionView();

        configuracionView.mostrar(
                root.getScene().getWindow(),
                anchoCorteActual,
                temaOscuroActual
        ).ifPresent(preferencias -> {

            kerfField.setText(
                    String.valueOf(preferencias.getAnchoCorteMm())
            );

            aplicarTema(preferencias.isTemaOscuro());
        });
    }


    private void aplicarTema(boolean temaOscuro) {

        root.getStyleClass().removeAll(
                "tema-claro",
                "tema-oscuro"
        );

        if (temaOscuro) {

            root.getStyleClass().add("tema-oscuro");


        } else {

            root.getStyleClass().add("tema-claro");

        }

        dibujarPlano();
    }

    private void crearPanelTablero() {

        Label titulo = new Label("Configuración del tablero");

        titulo.getStyleClass().add("titulo-seccion");

        Label materialLabel = new Label("Material:");

        materialCombo = new ComboBox<>();

        materialCombo.setMaxWidth(Double.MAX_VALUE);

        formatoCombo = new ComboBox<>();
        formatoCombo.setMaxWidth(Double.MAX_VALUE);
        formatoCombo.setOnAction(event -> actualizarDimensionesFormato());



        // Datos de cotización del tablero.

        Label proveedorLabel = new Label("Proveedor:");
        proveedorField = new TextField();
        proveedorField.setPromptText("Ej. Sodimac");

        Label enlaceProveedorLabel = new Label("Enlace del proveedor:");
        enlaceProveedorField = new TextField();
        enlaceProveedorField.setPromptText("https://...");

        Label precioUnitarioLabel = new Label("Precio unitario ($):");
        precioUnitarioField = new TextField();
        precioUnitarioField.setPromptText("Ej. 45000");

        Label tablerosAComprarLabel = new Label("Tableros a comprar:");
        tablerosAComprarField = new TextField("1");

        Button guardarCotizacionButton =
                new Button("Guardar cotización");

        guardarCotizacionButton.setMaxWidth(Double.MAX_VALUE);

        guardarCotizacionButton.setOnAction(
                event -> guardarCotizacion()
        );

        Label anchoLabel = new Label("Ancho (cm):");
        anchoTableroField = new TextField("183");

        Label largoLabel = new Label("Largo (cm):");
        largoTableroField = new TextField("250");

        Label espesorLabel = new Label("Espesor (mm):");
        espesorField = new TextField("15");

        Label kerfLabel = new Label("Ancho de corte (mm):");
        kerfField = new TextField("3");

        cargarMateriales();
        materialCombo.setOnAction(event -> cargarFormatos());

        Button aplicarButton = new Button("Aplicar configuración");
        aplicarButton.setMaxWidth(Double.MAX_VALUE);
        aplicarButton.setOnAction(event -> aplicarConfiguracion());

        VBox panel = new VBox(
                10,
                titulo,
                materialLabel,
                materialCombo,
                new Label("Formato de tablero:"),
                formatoCombo,
                anchoLabel,
                anchoTableroField,
                largoLabel,
                largoTableroField,
                espesorLabel,
                espesorField,
                kerfLabel,
                kerfField,
                aplicarButton,

                new Label("Cotización del tablero"),
                proveedorLabel,
                proveedorField,
                enlaceProveedorLabel,
                enlaceProveedorField,
                precioUnitarioLabel,
                precioUnitarioField,
                tablerosAComprarLabel,
                tablerosAComprarField,
                guardarCotizacionButton
        );

        panel.setPadding(new Insets(12));
        panel.setSpacing(10);
        panel.setPrefWidth(250);
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.getStyleClass().add("panel");

        panelTableroScroll = new ScrollPane(panel);

        panelTableroScroll.setFitToWidth(true);
        panelTableroScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        panelTableroScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        panelTableroScroll.setMinWidth(180);
        panelTableroScroll.setPrefWidth(250);

        root.setLeft(panelTableroScroll);
    }

    private void crearAreaTrabajo() {

        Label titulo = new Label("Plano de corte");

        titulo.getStyleClass().add("titulo-seccion");

        selectorTablero = new ComboBox<>();
        selectorTablero.setPromptText("Seleccionar tablero");
        selectorTablero.setDisable(true);

        selectorTablero.setOnAction(event -> {
            actualizarPiezasDelTableroSeleccionado();
            dibujarPlano();
            actualizarResumen();
        });

        canvasTablero = new Canvas(800, 600);

        StackPane plano = new StackPane(canvasTablero);

        plano.setOnMousePressed(event -> {
            mouseAnteriorX = event.getX();
            mouseAnteriorY = event.getY();
        });

        plano.setOnMouseDragged(event -> {

            double movimientoX = event.getX() - mouseAnteriorX;
            double movimientoY = event.getY() - mouseAnteriorY;

            desplazamientoX += movimientoX;
            desplazamientoY += movimientoY;

            mouseAnteriorX = event.getX();
            mouseAnteriorY = event.getY();

            dibujarPlano();
        });

        plano.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 4px;"
        );

        plano.setMinSize(0, 0);

        // Adaptar el Canvas al tamaño disponible.
        plano.widthProperty().addListener(
                (observable, anterior, nuevo) -> {
                    canvasTablero.setWidth(nuevo.doubleValue());
                    dibujarFondoCanvas();
                }
        );

        plano.heightProperty().addListener(
                (observable, anterior, nuevo) -> {
                    canvasTablero.setHeight(nuevo.doubleValue());
                    dibujarFondoCanvas();
                }
        );

        Button alejarButton = new Button("−");
        Button acercarButton = new Button("+");

        Label zoomLabel = new Label("100 %");

        alejarButton.setOnAction(event -> {
            nivelZoom = Math.max(0.25, nivelZoom - 0.25);
            zoomLabel.setText(
                    String.format("%.0f %%", nivelZoom * 100)
            );
            dibujarPlano();
        });

        acercarButton.setOnAction(event -> {
            nivelZoom = Math.min(3.0, nivelZoom + 0.25);
            zoomLabel.setText(
                    String.format("%.0f %%", nivelZoom * 100)
            );
            dibujarPlano();
        });

        HBox controlesZoom = new HBox(
                10,
                alejarButton,
                zoomLabel,
                acercarButton
        );

        controlesZoom.setAlignment(Pos.CENTER_RIGHT);

        // Leyenda de retazos.
        HBox leyendaAprovechable = new HBox(
                6,
                crearIndicadorRetazo("#9A8050"),
                new Label("Retazo aprovechable")
        );

        HBox leyendaNoAprovechable = new HBox(
                6,
                crearIndicadorRetazo("#A66F68"),
                new Label("Retazo no aprovechable")
        );

        HBox leyenda = new HBox(
                18,
                leyendaAprovechable,
                leyendaNoAprovechable
        );

        leyenda.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(leyenda, Priority.ALWAYS);

        HBox barraInferior = new HBox(
                10,
                leyenda,
                controlesZoom
        );

        HBox selectorArea = new HBox(
                10,
                new Label("Visualizar:"),
                selectorTablero
        );

        selectorArea.setAlignment(Pos.CENTER_LEFT);

        resumenRetazosAprovechablesLabel =
                new Label("Retazos aprovechables: 0 | Superficie: 0 cm²");

        resumenRetazosNoAprovechablesLabel =
                new Label("Retazos no aprovechables: 0 | Superficie: 0 cm²");

        VBox resumenRetazos = new VBox(
                5,
                resumenRetazosAprovechablesLabel,
                resumenRetazosNoAprovechablesLabel
        );

        VBox area = new VBox(
                15,
                titulo,
                selectorArea,
                plano,
                barraInferior,
                resumenRetazos
        );

        areaTrabajo = area;

        area.setPadding(new Insets(15));
        area.setMinWidth(0);
        area.setMinHeight(0);
        area.setMaxHeight(Double.MAX_VALUE);
        area.getStyleClass().add("panel");

        plano.setMinHeight(0);
        plano.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(plano, Priority.ALWAYS);

        root.setCenter(area);
    }

    private void ejecutarOptimizacion() {

        if (tableroActual == null) {
            resultadoActual = null;
            piezasColocadas.clear();
            return;
        }

        resultadoActual = optimizador.optimizar(
                tableroActual,
                piezas
        );

        selectorTablero.getItems().clear();

        int cantidadTableros =
                resultadoActual.getCantidadTablerosUtilizados();

        for (int i = 1; i <= cantidadTableros; i++) {
            selectorTablero.getItems().add(i);
        }

        if (cantidadTableros == 0) {

            selectorTablero.setValue(null);
            selectorTablero.setDisable(true);
            piezasColocadas.clear();

        } else {

            selectorTablero.setDisable(false);
            selectorTablero.setValue(1);

            actualizarPiezasDelTableroSeleccionado();
        }

        dibujarPlano();
        actualizarListaPiezasPendientes();
        actualizarResumen();
    }

    private void actualizarPiezasDelTableroSeleccionado() {

        piezasColocadas.clear();
        retazosActuales.clear();

        actualizarResumenRetazos();

        if (resultadoActual == null || selectorTablero == null) {
            return;
        }

        Integer numeroTablero = selectorTablero.getValue();

        if (numeroTablero == null) {
            return;
        }

        List<List<PiezaColocada>> resultados =
                resultadoActual.getPiezasPorTablero();

        int indice = numeroTablero - 1;

        if (indice >= 0 && indice < resultados.size()) {
            piezasColocadas = new ArrayList<>(
                    resultados.get(indice)
            );
        }

        if (proyectoIdActual != null) {
            try {
                retazosActuales =
                        proyectoService.listarRetazosProyectoYTablero(
                                proyectoIdActual,
                                numeroTablero
                        );
            } catch (SQLException e) {
                mostrarError(
                        "No se pudieron recuperar los retazos: " + e.getMessage()
                );
                retazosActuales = calculadorRetazos.calcularRetazos(
                        tableroActual,
                        piezasColocadas,
                        numeroTablero
                );
            }
        } else {
            retazosActuales = calculadorRetazos.calcularRetazos(
                    tableroActual,
                    piezasColocadas,
                    numeroTablero
            );
        }

        actualizarResumenRetazos();


    }

    private void dibujarFondoCanvas() {
        dibujarPlano();
    }

    private void dibujarPlano() {

        if (canvasTablero == null) {
            return;
        }

        GraphicsContext gc = canvasTablero.getGraphicsContext2D();

        double anchoCanvas = canvasTablero.getWidth();
        double altoCanvas = canvasTablero.getHeight();

        boolean temaOscuro =
                root.getStyleClass().contains("tema-oscuro");

        Color colorFondo = temaOscuro
                ? Color.web("#25221F")
                : Color.web("#F5EFE5");

        Color colorTablero = temaOscuro
                ? Color.web("#51483D")
                : Color.web("#E8D8BD");

        Color colorBorde = temaOscuro
                ? Color.web("#C5A77F")
                : Color.web("#806344");

        Color colorTexto = temaOscuro
                ? Color.web("#F0E7DA")
                : Color.web("#40372D");

        gc.setFill(colorFondo);
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        if (tableroActual == null) {
            gc.setFill(Color.GRAY);
            gc.fillText(
                    "Configura el tablero para visualizar el plano.",
                    20,
                    30
            );
            return;
        }

        double margen = 30;

        double escalaBase = Math.min(
                (anchoCanvas - margen * 2) / tableroActual.getAncho(),
                (altoCanvas - margen * 2) / tableroActual.getLargo()
        );

        double escala = escalaBase * nivelZoom;

        if (!Double.isFinite(escala) || escala <= 0) {
            return;
        }

        double anchoDibujo = tableroActual.getAncho() * escala;
        double largoDibujo = tableroActual.getLargo() * escala;

        double origenX =
                (anchoCanvas - anchoDibujo) / 2 + desplazamientoX;

        double origenY =
                (altoCanvas - largoDibujo) / 2 + desplazamientoY;

// Mantener siempre una parte del tablero visible.
        double minimoVisible = 60;

        double minimoX = minimoVisible - anchoDibujo;
        double maximoX = anchoCanvas - minimoVisible;

        double minimoY = minimoVisible - largoDibujo;
        double maximoY = altoCanvas - minimoVisible;

// Limitar el desplazamiento horizontal.
        origenX = Math.max(minimoX, Math.min(origenX, maximoX));

// Limitar el desplazamiento vertical.
        origenY = Math.max(minimoY, Math.min(origenY, maximoY));

        // Dibujar superficie del tablero.
        gc.setFill(colorTablero);
        gc.fillRect(origenX, origenY, anchoDibujo, largoDibujo);

        // Mostrar dimensiones del tablero.
        gc.setFill(colorTexto);
        gc.setFont(javafx.scene.text.Font.font(12));

        gc.fillText(
                String.format(
                        java.util.Locale.US,
                        "%.1f cm",
                        tableroActual.getAncho()
                ),
                origenX + anchoDibujo / 2 - 25,
                origenY - 10
        );

        gc.save();

        gc.translate(origenX - 12, origenY + largoDibujo / 2);
        gc.rotate(-90);

        gc.fillText(
                String.format(
                        java.util.Locale.US,
                        "%.1f cm",
                        tableroActual.getLargo()
                ),
                -25,
                0
        );

        gc.restore();

        gc.setStroke(colorBorde);
        gc.setLineWidth(2);
        gc.strokeRect(origenX, origenY, anchoDibujo, largoDibujo);

        // Dibujar retazos con achurado cruzado y límites individuales.
        for (Retazo retazo : retazosActuales) {

            double x = origenX + retazo.getX() * escala;
            double y = origenY + retazo.getY() * escala;

            double ancho = retazo.getAncho() * escala;
            double largo = retazo.getLargo() * escala;

            Color colorAchurado = retazo.isAprovechable()
                    ? (temaOscuro
                    ? Color.web("#B5A06F")
                    : Color.web("#9A8050"))
                    : (temaOscuro
                    ? Color.web("#A08080")
                    : Color.web("#A66F68"));

            gc.save();

            // Delimitar el espacio del retazo.
            gc.beginPath();
            gc.rect(x, y, ancho, largo);
            gc.clip();

            gc.setStroke(colorAchurado);
            gc.setLineWidth(0.8);

            double separacion = 8;

            // Achurado diagonal en una dirección.
            for (double desplazamiento = -largo;
                 desplazamiento <= ancho;
                 desplazamiento += separacion) {

                gc.strokeLine(
                        x + desplazamiento,
                        y,
                        x + desplazamiento + largo,
                        y + largo
                );
            }

            // Achurado diagonal en dirección contraria.
            for (double desplazamiento = -largo;
                 desplazamiento <= ancho;
                 desplazamiento += separacion) {

                gc.strokeLine(
                        x + desplazamiento,
                        y + largo,
                        x + desplazamiento + largo,
                        y
                );
            }

            gc.restore();

            // Borde individual del retazo.
            gc.setStroke(retazo.isAprovechable()
                    ? (temaOscuro
                    ? Color.web("#D2B77F")
                    : Color.web("#806344"))
                    : (temaOscuro
                    ? Color.web("#C58F8F")
                    : Color.web("#A66F68")));

            gc.setLineWidth(1.5);

            gc.strokeRect(
                    x,
                    y,
                    ancho,
                    largo
            );
        }

        // Colores para identificar las piezas.
        Color[] colores = {
                Color.web("#C6A77D"),
                Color.web("#A9B89A"),
                Color.web("#D9B98C"),
                Color.web("#C6A4A4"),
                Color.web("#9FB8B0"),
                Color.web("#C5B2D0")
        };

        int indice = 0;

        for (PiezaColocada colocada : piezasColocadas) {



            double x = origenX + colocada.getX() * escala;
            double y = origenY + colocada.getY() * escala;

            double ancho = colocada.getAncho() * escala;
            double largo = colocada.getLargo() * escala;

            Color color = colores[indice % colores.length];

            gc.setFill(color);

            gc.fillRect(
                    x + 1,
                    y + 1,
                    Math.max(0, ancho - 2),
                    Math.max(0, largo - 2)
            );

            gc.setStroke(Color.web("#66513B"));
            gc.setLineWidth(1.3);

            gc.strokeRect(
                    x,
                    y,
                    ancho,
                    largo
            );

            if (ancho > 55 && largo > 35) {

                gc.setFill(colorTexto);

                gc.fillText(
                        colocada.getPiezaOriginal().getNombre(),
                        x + 5,
                        y + 15
                );

                gc.setFont(javafx.scene.text.Font.font(10));

                gc.fillText(
                        String.format(
                                java.util.Locale.US,
                                "%.1f × %.1f cm",
                                colocada.getAncho(),
                                colocada.getLargo()
                        ),
                        x + 5,
                        y + 29
                );
            }

            indice++;
        }
    }


    private void crearPanelPiezas() {

        Label titulo = new Label("Piezas");

        titulo.getStyleClass().add("titulo-seccion");

        Label nombreLabel = new Label("Nombre:");
        nombreField = new TextField();
        nombreField.setPromptText("Ej. Costado");

        Label anchoLabel = new Label("Ancho (cm):");
        anchoPiezaField = new TextField();
        anchoPiezaField.setPromptText("Ej. 80");

        Label largoLabel = new Label("Largo (cm):");
        largoPiezaField = new TextField();
        largoPiezaField.setPromptText("Ej. 40");

        Label cantidadLabel = new Label("Cantidad:");
        cantidadField = new TextField("1");

        Button agregarButton = new Button("Agregar pieza");
        agregarButton.setMaxWidth(Double.MAX_VALUE);
        agregarButton.setOnAction(event -> agregarPieza());

        Label listaTitulo = new Label("Lista de piezas");

        listaPiezas = new VBox(8);
        listaPiezas.getStyleClass().add("lista-piezas");

        tituloPiezasPendientes = new Label("Piezas pendientes");

        tituloPiezasPendientes.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;"
        );

        listaPiezasPendientes = new VBox(8);

        listaPiezasPendientes.setPadding(
                new Insets(5)
        );

        listaPiezasPendientes.getStyleClass().add(
                "lista-piezas-pendientes"
        );

        ScrollPane scrollLista = new ScrollPane(listaPiezas);
        scrollLista.setFitToWidth(true);
        scrollLista.setFitToHeight(false);
        scrollLista.setPrefHeight(220);
        scrollLista.setMinHeight(120);
        scrollLista.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(scrollLista, Priority.ALWAYS);

        tiposLabel = new Label("Tipos de piezas: 0");
        unidadesLabel = new Label("Unidades totales: 0");
        superficieLabel = new Label("Superficie total: 0 cm²");

        superficieTableroLabel =
                new Label("Superficie del tablero: 0 cm²");

        superficieOcupadaLabel =
                new Label("Superficie ocupada: 0 cm²");

        desperdicioLabel =
                new Label("Superficie desperdiciada: 0 cm²");

        aprovechamientoLabel =
                new Label("Aprovechamiento: 0 %");

        unidadesColocadasLabel =
                new Label("Unidades colocadas: 0");

        unidadesPendientesLabel =
                new Label("Unidades pendientes: 0");

        costoUnitarioLabel =
                new Label("Precio unitario: No cotizado");

        tablerosUtilizadosLabel =
                new Label("Tableros utilizados: 0");

        tablerosAComprarLabel =
                new Label("Tableros a comprar: 0");

        costoUtilizadoLabel =
                new Label("Costo utilizado: No cotizado");

        costoCompraLabel =
                new Label("Costo de compra: No cotizado");

        diferenciaCostoLabel =
                new Label("Diferencia: No cotizada");

        actualizarListaPiezas();

        VBox resumen = new VBox(
                8,
                new Label("Resumen de piezas"),
                tiposLabel,
                unidadesLabel,
                unidadesColocadasLabel,
                unidadesPendientesLabel,
                superficieLabel,
                superficieTableroLabel,
                superficieOcupadaLabel,
                desperdicioLabel,
                aprovechamientoLabel,

                new Label("Resumen de costos"),
                tablerosUtilizadosLabel,
                tablerosAComprarLabel,
                costoUnitarioLabel,
                costoUtilizadoLabel,
                costoCompraLabel,
                diferenciaCostoLabel
        );

        resumen.setPadding(new Insets(10));

        resumen.getStyleClass().add("resumen-panel");

        VBox panel = new VBox(
                10,
                titulo,
                nombreLabel,
                nombreField,
                anchoLabel,
                anchoPiezaField,
                largoLabel,
                largoPiezaField,
                cantidadLabel,
                cantidadField,
                agregarButton,
                listaTitulo,
                scrollLista,
                tituloPiezasPendientes,
                listaPiezasPendientes,
                resumen
        );

        panel.setPadding(new Insets(12));
        panel.setSpacing(8);
        panel.setPrefWidth(280);
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.getStyleClass().add("panel");

        panelPiezasScroll = new ScrollPane(panel);

        panelPiezasScroll.setFitToWidth(true);
        panelPiezasScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        panelPiezasScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        panelPiezasScroll.setMinWidth(180);
        panelPiezasScroll.setPrefWidth(280);

        root.setRight(panelPiezasScroll);
    }


    private void guardarCotizacion() {

        try {

            if (tableroActual == null) {
                mostrarError(
                        "Primero debes aplicar la configuración del tablero."
                );
                return;
            }

            String proveedor = proveedorField.getText().trim();
            String enlace = enlaceProveedorField.getText().trim();
            String material = materialCombo.getValue();

            if (proveedor.isEmpty()) {
                mostrarError("Debes ingresar el nombre del proveedor.");
                return;
            }

            if (material == null || material.isBlank()) {
                mostrarError("Debes seleccionar un material.");
                return;
            }

            double precio = Double.parseDouble(
                    precioUnitarioField.getText().trim()
            );

            int tablerosAComprar = Integer.parseInt(
                    tablerosAComprarField.getText().trim()
            );

            if (!Double.isFinite(precio) || precio < 0) {
                mostrarError("El precio unitario no puede ser negativo.");
                return;
            }

            if (tablerosAComprar < 0) {
                mostrarError("La cantidad a comprar no puede ser negativa.");
                return;
            }

            int tablerosUtilizados = resultadoActual == null
                    ? 0
                    : resultadoActual.getCantidadTablerosUtilizados();

            if (tablerosAComprar < tablerosUtilizados) {
                mostrarError(
                        "La cantidad a comprar no puede ser inferior "
                                + "a los tableros utilizados."
                );
                return;
            }

            double espesorMilimetros =
                    tableroActual.getEspesor() * 10.0;

            cotizacionActual = new CotizacionTablero(
                    proveedor,
                    enlace,
                    material,
                    material + " " + espesorMilimetros + " mm",
                    espesorMilimetros,
                    tableroActual.getAncho(),
                    tableroActual.getLargo(),
                    precio
            );

            resumenCostosActual = new ResumenCostos(
                    tablerosUtilizados,
                    tablerosAComprar,
                    precio
            );

            tablerosUtilizadosLabel.setText(
                    "Tableros utilizados: " +
                            resumenCostosActual.getTablerosUtilizados()
            );

            tablerosAComprarLabel.setText(
                    "Tableros a comprar: " +
                            resumenCostosActual.getTablerosAComprar()
            );

            actualizarResumenCostos();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Cotización");
            alerta.setHeaderText("Cotización guardada");
            alerta.setContentText(
                    "Proveedor: " + proveedor
                            + "\nMaterial: " + material
                            + "\nPrecio unitario: $" + precio
                            + "\nTableros utilizados: " + tablerosUtilizados
                            + "\nTableros a comprar: " + tablerosAComprar
            );
            alerta.showAndWait();

        } catch (NumberFormatException e) {
            mostrarError(
                    "El precio y la cantidad de tableros deben ser numéricos."
            );
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private void aplicarConfiguracion() {
        try {
            double anchoTablero = Double.parseDouble(
                    anchoTableroField.getText().trim()
            );

            double largoTablero = Double.parseDouble(
                    largoTableroField.getText().trim()
            );

            double espesorMilimetros = Double.parseDouble(
                    espesorField.getText().trim()
            );

            double espesor = espesorMilimetros / 10.0;

            double anchoCorte = Double.parseDouble(
                    kerfField.getText().trim()
            );

            Tablero tablero = new Tablero(
                    anchoTablero,
                    largoTablero,
                    espesor,
                    anchoCorte
            );

            String error = validadorTablero.validarConfiguracion(tablero);

            if (error != null) {
                mostrarError(error);
                return;
            }

            for (Pieza pieza : piezas) {
                if (!validadorTablero.piezaCabe(
                        anchoTablero,
                        largoTablero,
                        pieza.getAncho(),
                        pieza.getLargo()
                )) {
                    mostrarError(
                            "La pieza \"" + pieza.getNombre()
                                    + "\" no cabe en las nuevas dimensiones del tablero."
                    );
                    return;
                }
            }

            tableroActual = tablero;

            ejecutarOptimizacion();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Configuración");
            alerta.setHeaderText("Configuración aplicada");
            alerta.setContentText(
                    "Los datos del tablero son válidos y la optimización se ha realizado."
            );
            alerta.showAndWait();

        } catch (NumberFormatException e) {
            mostrarError(
                    "Ingresa valores numéricos válidos en la configuración."
            );
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }



    private void agregarPieza() {

        try {

            String nombre = nombreField.getText().trim();

            double ancho = Double.parseDouble(
                    anchoPiezaField.getText().trim()
            );

            double largo = Double.parseDouble(
                    largoPiezaField.getText().trim()
            );

            int cantidad = Integer.parseInt(
                    cantidadField.getText().trim()
            );

            if (nombre.isEmpty()) {
                mostrarError("Debes ingresar un nombre para la pieza.");
                return;
            }

            if (ancho <= 0 || largo <= 0 || cantidad <= 0) {
                mostrarError("Las dimensiones y la cantidad deben ser mayores que cero.");
                return;
            }

            if (!validarDimensionesPieza(ancho, largo)) {
                return;
            }

            Pieza pieza = new Pieza(
                    nombre,
                    ancho,
                    largo,
                    cantidad
            );

            piezas.add(pieza);

            actualizarListaPiezas();

            limpiarFormulario();

            if (tableroActual != null) {
                ejecutarOptimizacion();

                System.out.println(
                        "Tipos de piezas solicitados: " + piezas.size()
                );

                System.out.println(
                        "Unidades colocadas: " +
                                resultadoActual.getTotalUnidadesColocadas()
                );

                System.out.println(
                        "Unidades pendientes: " +
                                resultadoActual.getTotalUnidadesPendientes()
                );
            }

        } catch (NumberFormatException e) {

            mostrarError("Ingresa valores numéricos válidos.");
        }
    }



    private void actualizarListaPiezas() {

        listaPiezas.getChildren().clear();

        if (piezas.isEmpty()) {

            listaPiezas.getChildren().add(
                    new Label("Todavía no hay piezas agregadas.")
            );

            actualizarResumen();
            return;
        }

        for (Pieza pieza : piezas) {

            Label descripcion = new Label(
                    pieza.toString() +
                            "\nÁrea total: " +
                            pieza.getAreaTotal() +
                            " cm²"
            );

            descripcion.setWrapText(true);
            descripcion.setMaxWidth(Double.MAX_VALUE);

            Button eliminarButton = new Button("Eliminar");
            eliminarButton.setMinWidth(90);
            eliminarButton.setPrefWidth(90);
            eliminarButton.setMaxWidth(90);

            eliminarButton.setOnAction(event -> {
                piezas.remove(pieza);

                actualizarListaPiezas();

                if (tableroActual != null) {
                    ejecutarOptimizacion();
                }
            });

            HBox fila = new HBox(
                    8,
                    descripcion,
                    eliminarButton
            );

            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(8));
            fila.setFillHeight(true);

            fila.getStyleClass().add("fila-pieza");

            HBox.setHgrow(descripcion, Priority.ALWAYS);

            listaPiezas.getChildren().add(fila);
        }

        actualizarResumen();
    }

    private void actualizarListaPiezasPendientes() {
        listaPiezasPendientes.getChildren().clear();

        if (resultadoActual == null) {
            tituloPiezasPendientes.setText("Piezas pendientes");
            listaPiezasPendientes.getChildren().add(
                    new Label("Aplica la configuración del tablero para calcular pendientes.")
            );
            return;
        }

        if (resultadoActual.getPiezasPendientes().isEmpty()) {
            tituloPiezasPendientes.setText("Piezas pendientes (0)");
            listaPiezasPendientes.getChildren().add(
                    new Label("No hay piezas pendientes.")
            );
            return;
        }

        tituloPiezasPendientes.setText(
                "Piezas pendientes (" +
                        resultadoActual.getTotalUnidadesPendientes() + ")"
        );

        for (Pieza pieza : resultadoActual.getPiezasPendientes()) {
            Label detalle = new Label(
                    pieza.getNombre() +
                            "\nDimensiones: " +
                            String.format(
                                    java.util.Locale.US,
                                    "%.1f × %.1f cm",
                                    pieza.getAncho(),
                                    pieza.getLargo()
                            ) +
                            "\nCantidad pendiente: " + pieza.getCantidad()
            );

            detalle.setWrapText(true);
            detalle.setMaxWidth(Double.MAX_VALUE);
            detalle.setPadding(new Insets(8));

            detalle.getStyleClass().add("detalle-pendientes");

            listaPiezasPendientes.getChildren().add(detalle);
        }
    }

    private void actualizarResumen() {

        int totalUnidades = 0;
        double superficieTotal = 0;

        for (Pieza pieza : piezas) {
            totalUnidades += pieza.getCantidad();
            superficieTotal += pieza.getAreaTotal();
        }

        tiposLabel.setText(
                "Tipos de piezas: " + piezas.size()
        );

        unidadesLabel.setText(
                "Unidades totales: " + totalUnidades
        );

        int unidadesColocadas = 0;
        int unidadesPendientes = totalUnidades;
        int cantidadTableros = 0;

        double superficieTablero = 0;
        double superficieOcupada = 0;

        if (resultadoActual != null && tableroActual != null) {

            unidadesColocadas =
                    resultadoActual.getTotalUnidadesColocadas();

            unidadesPendientes =
                    resultadoActual.getTotalUnidadesPendientes();

            cantidadTableros =
                    resultadoActual.getCantidadTablerosUtilizados();

            superficieTablero =
                    tableroActual.getArea() * cantidadTableros;

            for (List<PiezaColocada> tablero :
                    resultadoActual.getPiezasPorTablero()) {

                for (PiezaColocada colocada : tablero) {

                    superficieOcupada +=
                            colocada.getAncho() * colocada.getLargo();
                }
            }
        }

        double desperdicio = Math.max(
                0,
                superficieTablero - superficieOcupada
        );

        double aprovechamiento = 0;

        if (superficieTablero > 0) {
            aprovechamiento =
                    (superficieOcupada / superficieTablero) * 100;
        }

        unidadesColocadasLabel.setText(
                "Unidades colocadas: " + unidadesColocadas
        );

        unidadesPendientesLabel.setText(
                "Unidades pendientes: " + unidadesPendientes
        );

        superficieLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Superficie solicitada: %.2f cm²",
                        superficieTotal
                )
        );

        superficieTableroLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Superficie de %d tablero(s): %.2f cm²",
                        cantidadTableros,
                        superficieTablero
                )
        );

        superficieOcupadaLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Superficie ocupada: %.2f cm²",
                        superficieOcupada
                )
        );

        desperdicioLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Superficie desperdiciada: %.2f cm²",
                        desperdicio
                )
        );

        aprovechamientoLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Aprovechamiento: %.2f %%",
                        aprovechamiento
                )
        );

        actualizarResumenCostos();
    }

    private void actualizarResumenRetazos() {

        int cantidadAprovechables = 0;
        int cantidadNoAprovechables = 0;

        double superficieAprovechable = 0;
        double superficieNoAprovechable = 0;

        for (Retazo retazo : retazosActuales) {

            if (retazo.isAprovechable()) {

                cantidadAprovechables++;
                superficieAprovechable += retazo.getArea();

            } else {

                cantidadNoAprovechables++;
                superficieNoAprovechable += retazo.getArea();
            }
        }

        resumenRetazosAprovechablesLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Retazos aprovechables: %d | Superficie: %.2f cm²",
                        cantidadAprovechables,
                        superficieAprovechable
                )
        );

        resumenRetazosNoAprovechablesLabel.setText(
                String.format(
                        java.util.Locale.US,
                        "Retazos no aprovechables: %d | Superficie: %.2f cm²",
                        cantidadNoAprovechables,
                        superficieNoAprovechable
                )
        );
    }

    private void actualizarResumenCostos() {

        if (cotizacionActual == null) {

            costoUnitarioLabel.setText(
                    "Precio unitario: No cotizado"
            );

            costoUtilizadoLabel.setText(
                    "Costo utilizado: No cotizado"
            );

            costoCompraLabel.setText(
                    "Costo de compra: No cotizado"
            );

            diferenciaCostoLabel.setText(
                    "Diferencia: No cotizada"
            );

            resumenCostosActual = null;
            return;
        }

        int tablerosUtilizados = resultadoActual == null
                ? 0
                : resultadoActual.getCantidadTablerosUtilizados();

        int tablerosAComprar;

        try {

            tablerosAComprar = Integer.parseInt(
                    tablerosAComprarField.getText().trim()
            );

        } catch (NumberFormatException e) {

            tablerosAComprar = tablerosUtilizados;
        }

        tablerosAComprar = Math.max(
                tablerosAComprar,
                tablerosUtilizados
        );

        tablerosAComprarField.setText(
                String.valueOf(tablerosAComprar)
        );

        resumenCostosActual = new ResumenCostos(
                tablerosUtilizados,
                tablerosAComprar,
                cotizacionActual.getPrecioUnitario()
        );

        java.text.NumberFormat formatoMoneda =
                java.text.NumberFormat.getCurrencyInstance(
                        new java.util.Locale("es", "CL")
                );

        costoUnitarioLabel.setText(
                "Precio unitario: " +
                        formatoMoneda.format(
                                resumenCostosActual.getPrecioUnitario()
                        )
        );

        costoUtilizadoLabel.setText(
                "Costo utilizado: " +
                        formatoMoneda.format(
                                resumenCostosActual.getCostoUtilizado()
                        )
        );

        costoCompraLabel.setText(
                "Costo de compra: " +
                        formatoMoneda.format(
                                resumenCostosActual.getCostoCompra()
                        )
        );

        diferenciaCostoLabel.setText(
                "Diferencia: " +
                        formatoMoneda.format(
                                resumenCostosActual.getDiferencia()
                        )
        );
    }


    private boolean validarDimensionesPieza(
            double anchoPieza,
            double largoPieza
    ) {
        try {
            double anchoTablero = Double.parseDouble(
                    anchoTableroField.getText().trim()
            );

            double largoTablero = Double.parseDouble(
                    largoTableroField.getText().trim()
            );

            if (anchoTablero <= 0 || largoTablero <= 0) {
                mostrarError(
                        "Las dimensiones del tablero deben ser mayores que cero."
                );
                return false;
            }

            if (!validadorTablero.piezaCabe(
                    anchoTablero,
                    largoTablero,
                    anchoPieza,
                    largoPieza
            )) {
                mostrarError(
                        "La pieza supera las dimensiones del tablero, "
                                + "incluso girándola 90 grados."
                );
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            mostrarError(
                    "Las dimensiones del tablero deben ser numéricas."
            );
            return false;
        }
    }



    private void limpiarFormulario() {

        nombreField.clear();
        anchoPiezaField.clear();
        largoPiezaField.clear();

        cantidadField.setText("1");
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Datos incorrectos");
        alerta.setHeaderText("No se pudo agregar la pieza");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }



    private void configurarDisenoAdaptable() {

        Tab pestañaTablero = new Tab(
                "Configuración",
                panelTableroScroll
        );

        Tab pestañaPiezas = new Tab(
                "Piezas",
                panelPiezasScroll
        );

        pestañaTablero.setClosable(false);
        pestañaPiezas.setClosable(false);

        panelesCompactos = new TabPane(
                pestañaTablero,
                pestañaPiezas
        );

        panelesCompactos.setMinHeight(100);
        panelesCompactos.setPrefHeight(180);
        panelesCompactos.setMaxHeight(200);

        panelPrincipal = new SplitPane(
                panelTableroScroll,
                areaTrabajo,
                panelPiezasScroll
        );

        panelPrincipal.setOrientation(
                javafx.geometry.Orientation.HORIZONTAL
        );

        panelPrincipal.setDividerPositions(0.23, 0.77);

        contenedorCompacto = new VBox(8);
        contenedorCompacto.setMinSize(0, 0);

        VBox.setVgrow(areaTrabajo, Priority.ALWAYS);
        VBox.setVgrow(panelesCompactos, Priority.SOMETIMES);

        root.setLeft(null);
        root.setRight(null);
        root.setBottom(null);

        root.setMinSize(0, 0);

        root.setCenter(panelPrincipal);

        modoCompacto = false;

        root.widthProperty().addListener(
                (observable, anterior, nuevo) -> {

                    boolean necesitaModoCompacto =
                            nuevo.doubleValue() < 1100;

                    if (necesitaModoCompacto == modoCompacto) {
                        return;
                    }

                    modoCompacto = necesitaModoCompacto;

                    if (modoCompacto) {

                        panelPrincipal.getItems().clear();

                        contenedorCompacto.getChildren().setAll(
                                areaTrabajo,
                                panelesCompactos
                        );

                        root.setCenter(contenedorCompacto);

                    } else {

                        contenedorCompacto.getChildren().clear();

                        panelPrincipal.getItems().setAll(
                                panelTableroScroll,
                                areaTrabajo,
                                panelPiezasScroll
                        );

                        root.setCenter(panelPrincipal);
                    }
                }
        );
    }

    private javafx.scene.shape.Rectangle crearIndicadorRetazo(String colorHex) {

        javafx.scene.shape.Rectangle indicador =
                new javafx.scene.shape.Rectangle(18, 12);

        indicador.setFill(Color.web(colorHex));
        indicador.setStroke(Color.web("#806344"));

        return indicador;
    }

    private void exportarPDF() {

        if (tableroActual == null || resultadoActual == null) {
            mostrarError(
                    "Primero debes aplicar la configuración del tablero "
                            + "y ejecutar la optimización."
            );
            return;
        }

        FileChooser selector = new FileChooser();

        selector.setTitle("Exportar proyecto a PDF");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Documento PDF (*.pdf)",
                        "*.pdf"
                )
        );

        selector.setInitialFileName("TableroLab.pdf");

        File archivo = selector.showSaveDialog(
                root.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        String nombre = archivo.getName();

        if (!nombre.toLowerCase().endsWith(".pdf")) {
            archivo = new File(
                    archivo.getParentFile(),
                    nombre + ".pdf"
            );
        }

        try {

            Path destino = archivo.toPath();

            exportadorPDF.exportar(
                    destino,
                    tableroActual,
                    materialCombo.getValue(),
                    piezas,
                    resultadoActual,
                    cotizacionActual,
                    resumenCostosActual,
                    calcularTodosLosRetazos()
            );

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Exportación PDF");
            alerta.setHeaderText("PDF generado correctamente");
            alerta.setContentText(
                    "Archivo guardado en:\n" + archivo.getAbsolutePath()
            );
            alerta.showAndWait();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo generar el PDF: " + e.getMessage()
            );
        }
    }

    private void cargarMateriales() {

        try {

            MaterialDAO materialDAO = new MaterialDAO();

            materialesIds = new java.util.LinkedHashMap<>(
                    materialDAO.listarConIds()
            );

            materialCombo.getItems().setAll(materialesIds.keySet());

            if (!materialesIds.isEmpty()) {
                materialCombo.getSelectionModel().selectFirst();
                cargarFormatos();
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar materiales: " + e.getMessage());
        }
    }

    private void cargarFormatos() {
        try {
            String materialSeleccionado = materialCombo.getValue();

            if (materialSeleccionado == null) {
                formatoCombo.getItems().clear();
                formatosIds.clear();
                return;
            }

            Integer materialId = materialesIds.get(materialSeleccionado);

            if (materialId == null) {
                mostrarError("No se pudo identificar el material seleccionado.");
                return;
            }

            FormatoTableroDAO formatoDAO = new FormatoTableroDAO();

            formatosIds = new java.util.LinkedHashMap<>(
                    formatoDAO.listarConIdsPorMaterial(materialId)
            );

            formatoCombo.getItems().setAll(formatosIds.keySet());

            if (!formatosIds.isEmpty()) {
                formatoCombo.getSelectionModel().selectFirst();
                actualizarDimensionesFormato();
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar formatos: " + e.getMessage());
        }
    }

    public Parent getRoot() {
        return root;
    }

    private void actualizarDimensionesFormato() {
        try {
            String formatoSeleccionado = formatoCombo.getValue();

            if (formatoSeleccionado == null) {
                return;
            }

            Integer formatoId = formatosIds.get(formatoSeleccionado);

            if (formatoId == null) {
                mostrarError("No se pudo identificar el formato seleccionado.");
                return;
            }

            FormatoTableroDAO formatoDAO = new FormatoTableroDAO();

            double[] dimensiones =
                    formatoDAO.obtenerDimensionesPorId(formatoId);

            anchoTableroField.setText(
                    String.valueOf(dimensiones[0])
            );

            largoTableroField.setText(
                    String.valueOf(dimensiones[1])
            );

            espesorField.setText(
                    String.valueOf(dimensiones[2])
            );

        } catch (SQLException e) {
            mostrarError(
                    "Error al actualizar las dimensiones: " + e.getMessage()
            );
        }
    }

    private void guardarProyecto() {

        try {
            if (proyectoIdActual != null) {
                mostrarError(
                        "Este proyecto ya fue guardado. "
                                + "La actualización de proyectos existentes "
                                + "se implementará posteriormente."
                );
                return;
            }

            String nombre = nombreProyectoField.getText().trim();

            if (nombre.isEmpty()) {
                mostrarError("Debes ingresar un nombre para el proyecto.");
                return;
            }

            if (tableroActual == null) {
                mostrarError(
                        "Primero debes aplicar la configuración del tablero."
                );
                return;
            }

            if (piezas.isEmpty()) {
                mostrarError("Debes agregar al menos una pieza.");
                return;
            }

            String materialSeleccionado = materialCombo.getValue();
            String formatoSeleccionado = formatoCombo.getValue();

            Integer materialId = materialesIds.get(materialSeleccionado);
            Integer formatoId = formatosIds.get(formatoSeleccionado);

            if (materialId == null || formatoId == null) {
                mostrarError(
                        "Debes seleccionar un material y un formato válidos."
                );
                return;
            }

            double anchoCorteMm = Double.parseDouble(
                    kerfField.getText().trim()
            );

            if (!Double.isFinite(anchoCorteMm) || anchoCorteMm <= 0) {
                mostrarError(
                        "El ancho de corte debe ser mayor que cero."
                );
                return;
            }

            int id = proyectoService.guardarProyectoCompleto(
                    nombre,
                    materialId,
                    formatoId,
                    anchoCorteMm,
                    piezas,
                    calcularTodosLosRetazos()
            );

            proyectoIdActual = id;

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Guardar proyecto");
            alerta.setHeaderText("Proyecto guardado correctamente");
            alerta.setContentText(
                    "Proyecto: " + nombre
                            + "\nID: " + id
                            + "\nPiezas: " + piezas.size()
            );
            alerta.showAndWait();

        } catch (NumberFormatException e) {
            mostrarError("El ancho de corte debe ser un valor numérico.");
        } catch (SQLException e) {
            mostrarError(
                    "No se pudo guardar el proyecto: " + e.getMessage()
            );
        }

    }

    private void nuevoProyecto() {

        // Desvincular el proyecto guardado anteriormente.
        proyectoIdActual = null;

        // Limpiar el nombre del proyecto.
        nombreProyectoField.clear();

        // Limpiar las piezas.
        piezas.clear();

        // Reiniciar la configuración aplicada.
        tableroActual = null;
        resultadoActual = null;
        piezasColocadas.clear();

        // Limpiar retazos.
        retazosActuales.clear();

        // Limpiar cotización y costos.
        cotizacionActual = null;
        resumenCostosActual = null;

        proveedorField.clear();
        enlaceProveedorField.clear();
        precioUnitarioField.clear();
        tablerosAComprarField.setText("1");

        // Reiniciar la selección del tablero comercial.
        materialCombo.getSelectionModel().clearSelection();
        formatoCombo.getSelectionModel().clearSelection();

        // Seleccionar nuevamente el primer material disponible.
        if (!materialCombo.getItems().isEmpty()) {
            materialCombo.getSelectionModel().selectFirst();
        } else {
            anchoTableroField.clear();
            largoTableroField.clear();
            espesorField.clear();
        }

        // Restaurar el ancho de corte predeterminado.
        kerfField.setText(
                String.valueOf(
                        configuracionDAO.cargar().getAnchoCorteMm()
                )
        );

        // Reiniciar el selector de tableros.
        selectorTablero.getItems().clear();
        selectorTablero.setValue(null);
        selectorTablero.setDisable(true);

        // Reiniciar el zoom y el desplazamiento del plano.
        nivelZoom = 1.0;
        desplazamientoX = 0;
        desplazamientoY = 0;

        // Actualizar todos los elementos visuales.
        actualizarListaPiezas();
        actualizarListaPiezasPendientes();
        actualizarResumenRetazos();
        actualizarResumen();
        dibujarPlano();
    }

    private void abrirProyecto() {

        try {
            List<String> proyectos = proyectoService.listarProyectos();

            if (proyectos.isEmpty()) {
                mostrarError("No hay proyectos guardados para abrir.");
                return;
            }

            ChoiceDialog<String> dialogo =
                    new ChoiceDialog<>(proyectos.get(0), proyectos);

            dialogo.setTitle("Abrir proyecto");
            dialogo.setHeaderText("Selecciona un proyecto guardado");
            dialogo.setContentText("Proyecto:");

            dialogo.showAndWait().ifPresent(proyectoSeleccionado -> {

                try {
                    String textoId = proyectoSeleccionado
                            .substring(4, proyectoSeleccionado.indexOf(" |"));

                    int proyectoId = Integer.parseInt(textoId);

                    List<String> datos =
                            proyectoService.obtenerDatosProyecto(proyectoId);

                    List<Pieza> piezasGuardadas =
                            proyectoService.listarPiezasProyecto(proyectoId);

                    cargarProyectoEnFormulario(
                            proyectoId,
                            datos,
                            piezasGuardadas
                    );

                } catch (Exception e) {
                    mostrarError("No se pudo abrir el proyecto: " + e.getMessage());
                }
            });

        } catch (SQLException e) {
            mostrarError("No se pudieron consultar los proyectos: " + e.getMessage());
        }
    }

    private void cargarProyectoEnFormulario(
            int proyectoId,
            List<String> datos,
            List<Pieza> piezasGuardadas
    ) {

        if (datos == null || datos.size() != 7) {
            mostrarError("Los datos del proyecto están incompletos.");
            return;
        }

        if (piezasGuardadas == null) {
            mostrarError("No se pudieron recuperar las piezas del proyecto.");
            return;
        }

        try {

            String nombre = datos.get(0);

            int materialId = Integer.parseInt(datos.get(1));
            int formatoId = Integer.parseInt(datos.get(2));

            double anchoCorteMm = Double.parseDouble(datos.get(3));

            double anchoCm = Double.parseDouble(datos.get(4));
            double largoCm = Double.parseDouble(datos.get(5));
            double espesorMm = Double.parseDouble(datos.get(6));

            // Buscar la etiqueta del material correspondiente a su ID.
            String etiquetaMaterial = materialesIds.entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(materialId))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (etiquetaMaterial == null) {
                mostrarError("El material del proyecto ya no está disponible.");
                return;
            }

            // Seleccionar material y recuperar sus formatos.
            materialCombo.setValue(etiquetaMaterial);
            cargarFormatos();

            // Buscar la etiqueta del formato correspondiente a su ID.
            String etiquetaFormato = formatosIds.entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(formatoId))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (etiquetaFormato == null) {
                mostrarError("El formato del proyecto ya no está disponible.");
                return;
            }

            formatoCombo.setValue(etiquetaFormato);

            // Restaurar los datos originales del proyecto.
            nombreProyectoField.setText(nombre);

            anchoTableroField.setText(String.valueOf(anchoCm));
            largoTableroField.setText(String.valueOf(largoCm));
            espesorField.setText(String.valueOf(espesorMm));
            kerfField.setText(String.valueOf(anchoCorteMm));

            // Recuperar las piezas.
            piezas.clear();
            piezas.addAll(piezasGuardadas);

            // Restablecer los datos de cotización.
            cotizacionActual = null;
            resumenCostosActual = null;

            proveedorField.clear();
            enlaceProveedorField.clear();
            precioUnitarioField.clear();
            tablerosAComprarField.setText("1");

            // Reconstruir el tablero y ejecutar la optimización.
            tableroActual = new Tablero(
                    anchoCm,
                    largoCm,
                    espesorMm / 10.0,
                    anchoCorteMm
            );

            proyectoIdActual = proyectoId;

            actualizarListaPiezas();
            ejecutarOptimizacion();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Abrir proyecto");
            alerta.setHeaderText("Proyecto cargado correctamente");
            alerta.setContentText(
                    "Proyecto: " + nombre
                            + "\nPiezas recuperadas: " + piezas.size()
                            + "\nConfiguración restaurada correctamente."
            );
            alerta.showAndWait();

        } catch (NumberFormatException e) {
            mostrarError("Los datos numéricos del proyecto no son válidos.");
        }
    }


    private void eliminarProyecto() {

        try {
            List<String> proyectos = proyectoService.listarProyectos();

            if (proyectos.isEmpty()) {
                mostrarError("No hay proyectos guardados para eliminar.");
                return;
            }

            ChoiceDialog<String> dialogo =
                    new ChoiceDialog<>(proyectos.get(0), proyectos);

            dialogo.setTitle("Eliminar proyecto");
            dialogo.setHeaderText("Selecciona el proyecto que deseas eliminar");
            dialogo.setContentText("Proyecto:");

            dialogo.showAndWait().ifPresent(proyectoSeleccionado -> {

                try {
                    String textoId = proyectoSeleccionado
                            .substring(4, proyectoSeleccionado.indexOf(" |"));

                    int proyectoId = Integer.parseInt(textoId);

                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

                    confirmacion.setTitle("Confirmar eliminación");
                    confirmacion.setHeaderText("¿Eliminar este proyecto?");
                    confirmacion.setContentText(
                            "Se eliminarán permanentemente el proyecto, "
                                    + "sus piezas, retazos y cotizaciones.\n\n"
                                    + proyectoSeleccionado
                                    + "\n\nEsta operación no se puede deshacer."
                    );

                    confirmacion.showAndWait().ifPresent(respuesta -> {

                        if (respuesta == ButtonType.OK) {

                            try {
                                proyectoService.eliminarProyecto(proyectoId);

                                if (proyectoIdActual != null
                                        && proyectoIdActual == proyectoId) {

                                    nuevoProyecto();
                                }

                                mostrarInformacion(
                                        "Proyecto eliminado correctamente."
                                );

                            } catch (SQLException e) {
                                mostrarError(
                                        "No se pudo eliminar el proyecto: "
                                                + e.getMessage()
                                );
                            }
                        }
                    });

                } catch (Exception e) {
                    mostrarError(
                            "No se pudo identificar el proyecto: "
                                    + e.getMessage()
                    );
                }
            });

        } catch (SQLException e) {
            mostrarError(
                    "No se pudieron consultar los proyectos: "
                            + e.getMessage()
            );
        }
    }

    private void mostrarInformacion(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("TableroLab");
        alerta.setHeaderText("Operación realizada correctamente");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }


    private void crearRespaldo() {

        FileChooser selector = new FileChooser();

        selector.setTitle("Crear respaldo de TableroLab");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Base de datos SQLite (*.db)",
                        "*.db"
                )
        );

        selector.setInitialFileName("tablerolab_respaldo.db");

        File archivo = selector.showSaveDialog(
                root.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        String nombre = archivo.getName();

        if (!nombre.toLowerCase().endsWith(".db")) {
            archivo = new File(
                    archivo.getParentFile(),
                    nombre + ".db"
            );
        }

        try {

            Path destino = archivo.toPath();

            respaldoService.crearRespaldo(destino);

            respaldoService.verificarRespaldo(destino);

            mostrarInformacion(
                    "Respaldo creado y verificado correctamente.\n\n"
                            + "Ubicación:\n"
                            + archivo.getAbsolutePath()
            );

        } catch (Exception e) {

            mostrarError(
                    "No se pudo crear el respaldo: " + e.getMessage()
            );
        }
    }


    private void restaurarRespaldo() {

        FileChooser selector = new FileChooser();

        selector.setTitle("Seleccionar respaldo para restaurar");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Bases de datos SQLite (*.db)",
                        "*.db"
                )
        );

        File archivo = selector.showOpenDialog(
                root.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        try {

            Path respaldo = archivo.toPath();

            // Verificar el archivo antes de solicitar la restauración.
            respaldoService.verificarRespaldo(respaldo);

            Alert confirmacion = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmacion.setTitle("Confirmar restauración");

            confirmacion.setHeaderText(
                    "¿Deseas restaurar esta copia de seguridad?"
            );

            confirmacion.setContentText(
                    "Archivo seleccionado:\n"
                            + archivo.getAbsolutePath()
                            + "\n\n"
                            + "La base de datos actual será reemplazada "
                            + "por los datos del respaldo.\n\n"
                            + "Antes de reemplazarla se creará "
                            + "un respaldo automático de la base actual.\n\n"
                            + "Para completar la operación tendrás que "
                            + "cerrar y volver a abrir TableroLab."
            );

            confirmacion.showAndWait().ifPresent(respuesta -> {

                if (respuesta == ButtonType.OK) {

                    try {

                        // Registrar la restauración para el próximo inicio.
                        restauracionPendienteService.guardar(respaldo);

                        Alert alerta = new Alert(
                                Alert.AlertType.INFORMATION
                        );

                        alerta.setTitle("Restauración programada");

                        alerta.setHeaderText(
                                "Solicitud de restauración guardada"
                        );

                        alerta.setContentText(
                                "El respaldo fue verificado y la restauración "
                                        + "quedó programada.\n\n"
                                        + "Ahora debes cerrar completamente "
                                        + "TableroLab y volver a abrirlo.\n\n"
                                        + "La restauración se ejecutará "
                                        + "durante el próximo inicio."
                        );

                        alerta.showAndWait();

                    } catch (Exception e) {

                        mostrarError(
                                "No se pudo programar la restauración: "
                                        + e.getMessage()
                        );
                    }
                }
            });

        } catch (Exception e) {

            mostrarError(
                    "El respaldo no pudo verificarse: " + e.getMessage()
            );
        }
    }


}

