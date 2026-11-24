
package cl.tablerolab.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfiguracionDAO {

    private static final Path DIRECTORIO =
            Path.of(
                    System.getProperty("user.home"),
                    ".tablerolab"
            );

    private static final Path ARCHIVO =
            DIRECTORIO.resolve("configuracion.properties");

    // Ubicación anterior, para conservar las preferencias existentes.
    private static final Path ARCHIVO_ANTIGUO =
            Paths.get("configuracion.properties");

    public static class Preferencias {

        private final double anchoCorteMm;
        private final boolean temaOscuro;

        public Preferencias(
                double anchoCorteMm,
                boolean temaOscuro) {

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

    public Preferencias cargar() {

        Properties propiedades = new Properties();

        try {

            if (Files.exists(ARCHIVO)) {

                try (InputStream entrada =
                             Files.newInputStream(ARCHIVO)) {

                    propiedades.load(entrada);
                }

            } else if (Files.isRegularFile(ARCHIVO_ANTIGUO)) {

                try (InputStream entrada =
                             Files.newInputStream(ARCHIVO_ANTIGUO)) {

                    propiedades.load(entrada);
                }

                // Migrar las preferencias a la nueva ubicación.
                try {
                    guardarPropiedades(propiedades);
                } catch (IOException ignored) {
                    // Se conservan los valores cargados en memoria.
                }
            }

            double anchoCorte = Double.parseDouble(
                    propiedades.getProperty(
                            "anchoCorteMm", "3.0")
            );

            boolean temaOscuro = Boolean.parseBoolean(
                    propiedades.getProperty(
                            "temaOscuro", "false")
            );

            if (!Double.isFinite(anchoCorte)
                    || anchoCorte <= 0) {

                anchoCorte = 3.0;
            }

            return new Preferencias(
                    anchoCorte,
                    temaOscuro
            );

        } catch (IOException | NumberFormatException e) {

            return new Preferencias(3.0, false);
        }
    }

    public void guardar(Preferencias preferencias)
            throws IOException {

        Properties propiedades = new Properties();

        propiedades.setProperty(
                "anchoCorteMm",
                String.valueOf(preferencias.getAnchoCorteMm())
        );

        propiedades.setProperty(
                "temaOscuro",
                String.valueOf(preferencias.isTemaOscuro())
        );

        guardarPropiedades(propiedades);
    }

    private void guardarPropiedades(Properties propiedades)
            throws IOException {

        Files.createDirectories(DIRECTORIO);

        try (OutputStream salida =
                     Files.newOutputStream(ARCHIVO)) {

            propiedades.store(
                    salida,
                    "Configuracion de TableroLab"
            );
        }
    }
}
