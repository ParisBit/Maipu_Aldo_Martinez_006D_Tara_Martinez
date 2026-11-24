
package cl.tablerolab.app;

import cl.tablerolab.ui.MainView;
import cl.tablerolab.persistence.InicializadorBD;
import cl.tablerolab.security.RespaldoService;
import cl.tablerolab.security.RestauracionPendienteService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.sql.SQLException;

public class TableroLabApp extends Application {

    @Override
    public void start(Stage stage) {

        // 1. Comprobar si existe una restauración pendiente.
        RestauracionPendienteService restauracionPendiente =
                new RestauracionPendienteService();

        try {

            Path respaldo = restauracionPendiente.obtenerPendiente();

            if (respaldo != null) {

                // 2. Restaurar antes de inicializar la base de datos.
                RespaldoService respaldoService = new RespaldoService();

                Path respaldoAnterior =
                        respaldoService.restaurarRespaldo(respaldo);

                // 3. Eliminar la solicitud solamente si la restauración tuvo éxito.
                restauracionPendiente.eliminarPendiente();

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Restauración completada");
                alerta.setHeaderText("La base de datos se restauró correctamente.");
                alerta.setContentText(
                        "TableroLab utilizará los datos del respaldo seleccionado.\n\n"
                                + "Se creó un respaldo automático de la base de datos anterior en:\n\n"
                                + respaldoAnterior
                );
                alerta.showAndWait();
            }

        } catch (Exception e) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de restauración");
            alerta.setHeaderText("No se pudo restaurar la base de datos.");
            alerta.setContentText(
                    "TableroLab no continuará iniciándose para proteger "
                            + "la base de datos.\n\n"
                            + "Detalle: "
                            + e.getMessage()
            );
            alerta.showAndWait();

            Platform.exit();
            return;
        }

        // 4. Inicializar la base de datos después de la restauración.
        try {

            InicializadorBD.inicializar();

        } catch (SQLException e) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de base de datos");
            alerta.setHeaderText("No se pudo inicializar la base de datos de TableroLab.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();

            Platform.exit();
            return;
        }

        // 5. Abrir la ventana principal.
        MainView mainView = new MainView();

        Scene scene = new Scene(mainView.getRoot());

        // Cargar estilos CSS.
        scene.getStylesheets().add(
                getClass().getResource("/tablerolab.css").toExternalForm()
        );

        stage.setTitle("TableroLab - Optimizador de Cortes");
        stage.setScene(scene);

        // Dimensiones mínimas de la ventana.
        stage.setMinWidth(850);
        stage.setMinHeight(600);

        // Tamaño inicial.
        stage.setWidth(1200);
        stage.setHeight(600);

        // Abrir la aplicación maximizada.
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
