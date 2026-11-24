
package cl.tablerolab.ui;

import cl.tablerolab.persistence.ConfiguracionDAO;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

public class ConfiguracionView {

    private final ConfiguracionDAO configuracionDAO =
            new ConfiguracionDAO();

    public Optional<Preferencias> mostrar(
            Window propietario,
            double anchoCorteActual,
            boolean temaOscuroActual
    ) {

        Dialog<Preferencias> dialogo = new Dialog<>();

        dialogo.setTitle("Configuración de TableroLab");
        dialogo.setHeaderText("Preferencias de la aplicación");

        if (propietario != null) {
            dialogo.initOwner(propietario);
            dialogo.initModality(Modality.WINDOW_MODAL);
        }

        dialogo.setResizable(false);

        ButtonType guardar = new ButtonType(
                "Guardar",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancelar = new ButtonType(
                "Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(guardar, cancelar);

        Label unidadesTitulo = new Label("Unidades de trabajo");
        unidadesTitulo.setStyle("-fx-font-weight: bold;");

        Label unidadesTexto = new Label(
                "Dimensiones: centímetros (cm)\n" +
                        "Espesor y ancho de corte: milímetros (mm)"
        );

        Label optimizacionTitulo = new Label("Optimización");

        Label anchoCorteLabel = new Label(
                "Ancho de corte predeterminado (mm):"
        );

        TextField anchoCorteField = new TextField(
                String.valueOf(anchoCorteActual)
        );

        anchoCorteField.setMaxWidth(120);

        GridPane optimizacionGrid = new GridPane();
        optimizacionGrid.setHgap(12);
        optimizacionGrid.setVgap(10);
        optimizacionGrid.add(anchoCorteLabel, 0, 0);
        optimizacionGrid.add(anchoCorteField, 1, 0);

        Label aparienciaTitulo = new Label("Apariencia");
        aparienciaTitulo.setStyle("-fx-font-weight: bold;");

        Label temaLabel = new Label("Tema de la aplicación:");

        ComboBox<String> temaCombo = new ComboBox<>();
        temaCombo.getItems().addAll("Claro", "Oscuro");
        temaCombo.setValue(
                temaOscuroActual ? "Oscuro" : "Claro"
        );
        temaCombo.setMaxWidth(150);

        GridPane aparienciaGrid = new GridPane();
        aparienciaGrid.setHgap(12);
        aparienciaGrid.setVgap(10);
        aparienciaGrid.add(temaLabel, 0, 0);
        aparienciaGrid.add(temaCombo, 1, 0);

        VBox contenido = new VBox(
                12,
                unidadesTitulo,
                unidadesTexto,
                new Separator(),
                optimizacionTitulo,
                optimizacionGrid,
                new Separator(),
                aparienciaTitulo,
                aparienciaGrid
        );

        contenido.setPadding(new Insets(10));
        contenido.setMinWidth(390);

        panel.setContent(contenido);

        Node botonGuardar = panel.lookupButton(guardar);

        botonGuardar.addEventFilter(
                ActionEvent.ACTION,
                evento -> {

                    try {

                        double valor = Double.parseDouble(
                                anchoCorteField.getText()
                                        .trim()
                                        .replace(",", ".")
                        );

                        if (!Double.isFinite(valor) || valor <= 0) {
                            throw new NumberFormatException();
                        }

                    } catch (NumberFormatException e) {

                        evento.consume();

                        Alert alerta = new Alert(
                                Alert.AlertType.WARNING
                        );

                        alerta.initOwner(
                                dialogo.getDialogPane()
                                        .getScene().getWindow()
                        );

                        alerta.setTitle("Dato inválido");
                        alerta.setHeaderText(
                                "Ancho de corte incorrecto"
                        );

                        alerta.setContentText(
                                "Ingresa un valor numérico mayor que cero."
                        );

                        alerta.showAndWait();
                        return;
                    }

                    double anchoCorte = Double.parseDouble(
                            anchoCorteField.getText()
                                    .trim()
                                    .replace(",", ".")
                    );

                    boolean temaOscuro =
                            "Oscuro".equals(temaCombo.getValue());

                    try {

                        configuracionDAO.guardar(
                                new ConfiguracionDAO.Preferencias(
                                        anchoCorte,
                                        temaOscuro
                                )
                        );

                    } catch (IOException e) {

                        evento.consume();

                        Alert alerta = new Alert(
                                Alert.AlertType.ERROR
                        );

                        alerta.initOwner(
                                dialogo.getDialogPane()
                                        .getScene().getWindow()
                        );

                        alerta.setTitle("Error al guardar");
                        alerta.setHeaderText(
                                "No se pudo guardar la configuración"
                        );

                        alerta.setContentText(
                                "No fue posible escribir el archivo " +
                                        "configuracion.properties.\n\n" +
                                        e.getMessage()
                        );

                        alerta.showAndWait();
                    }
                }
        );

        dialogo.setResultConverter(boton -> {

            if (boton == guardar) {

                double anchoCorte = Double.parseDouble(
                        anchoCorteField.getText()
                                .trim()
                                .replace(",", ".")
                );

                boolean temaOscuro =
                        "Oscuro".equals(temaCombo.getValue());

                return new Preferencias(
                        anchoCorte,
                        temaOscuro
                );
            }

            return null;
        });

        return dialogo.showAndWait();
    }

    public static class Preferencias {

        private final double anchoCorteMm;
        private final boolean temaOscuro;

        public Preferencias(
                double anchoCorteMm,
                boolean temaOscuro
        ) {
            this.anchoCorteMm = anchoCorteMm;
            this.temaOscuro = temaOscuro;
        }

        public double getAnchoCorteMm() {
            return anchoCorteMm;
        }

        public boolean isTemaOscuro() {
            return temaOscuro;
        }
    }
}
