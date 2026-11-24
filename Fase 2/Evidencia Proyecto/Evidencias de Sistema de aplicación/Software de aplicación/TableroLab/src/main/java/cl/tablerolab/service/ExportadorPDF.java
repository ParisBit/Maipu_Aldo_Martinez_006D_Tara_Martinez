
package cl.tablerolab.service;

import cl.tablerolab.model.Tablero;
import cl.tablerolab.model.Pieza;
import cl.tablerolab.model.CotizacionTablero;
import cl.tablerolab.model.ResumenCostos;
import cl.tablerolab.model.PiezaColocada;
import cl.tablerolab.model.Retazo;
import cl.tablerolab.model.EstadoRetazo;
import cl.tablerolab.optimizer.ResultadoOptimizacion;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;


public class ExportadorPDF {

    private static final float MARGEN = 50;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getWidth();
    private static final float ALTO_PAGINA = PDRectangle.A4.getHeight();

    private static final float Y_INICIO = ALTO_PAGINA - 115;
    private static final float Y_LIMITE = 65;

    private static final PDType1Font FUENTE_NORMAL =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FUENTE_NEGRITA =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final NumberFormat FORMATO_MONEDA =
            NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    static {
        FORMATO_MONEDA.setMaximumFractionDigits(0);
        FORMATO_MONEDA.setMinimumFractionDigits(0);
    }


    // =========================================================
    // EXPORTACION PRINCIPAL
    // =========================================================

    public void exportar(
            Path destino,
            Tablero tablero,
            String material,
            List<Pieza> piezasSolicitadas,
            ResultadoOptimizacion resultado,
            CotizacionTablero cotizacion,
            ResumenCostos resumenCostos,
            List<Retazo> retazos
    ) throws IOException {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "El destino del PDF no puede ser nulo."
            );
        }

        if (tablero == null) {
            throw new IllegalArgumentException(
                    "El tablero no puede ser nulo."
            );
        }

        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado de optimizacion no puede ser nulo."
            );
        }

        if (retazos == null) {
            retazos = new ArrayList<>();
        }

        if (piezasSolicitadas == null) {
            piezasSolicitadas = new ArrayList<>();
        }

        try (PDDocument documento = new PDDocument()) {

            dibujarResumenGeneral(
                    documento,
                    tablero,
                    material,
                    resultado,
                    cotizacion,
                    resumenCostos
            );

            dibujarPiezasSolicitadas(
                    documento,
                    piezasSolicitadas
            );

            dibujarPiezasPendientes(
                    documento,
                    resultado
            );

            List<List<PiezaColocada>> piezasPorTablero =
                    resultado.getPiezasPorTablero();

            if (piezasPorTablero != null) {

                int numeroTablero = 1;

                for (List<PiezaColocada> piezasColocadas
                        : piezasPorTablero) {

                    if (piezasColocadas == null) {
                        piezasColocadas = new ArrayList<>();
                    }

                    List<Retazo> retazosTablero =
                            obtenerRetazosTablero(
                                    retazos,
                                    numeroTablero
                            );

                    dibujarBalanceSuperficies(
                            documento,
                            tablero,
                            piezasColocadas,
                            retazosTablero,
                            numeroTablero
                    );

                    dibujarPlanoTablero(
                            documento,
                            tablero,
                            piezasColocadas,
                            numeroTablero,
                            retazosTablero
                    );

                    numeroTablero++;
                }
            }

            dibujarDetalleRetazos(
                    documento,
                    retazos
            );

            agregarNumeracionPaginas(documento);

            documento.save(destino.toFile());
        }
    }


    // =========================================================
    // UTILIDADES DE TEXTO
    // =========================================================

    private String textoSeguro(String texto) {

        if (texto == null) {
            return "";
        }

        String normalizado = Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        );

        normalizado = normalizado.replaceAll("\\p{M}+", "");

        normalizado = normalizado
                .replace("×", "x")
                .replace("–", "-")
                .replace("—", "-")
                .replace(" ", " ");

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < normalizado.length(); i++) {

            char caracter = normalizado.charAt(i);

            if (caracter >= 32 && caracter <= 126) {
                resultado.append(caracter);
            } else {
                resultado.append('?');
            }
        }

        return resultado.toString();
    }


    private void escribirTexto(
            PDPageContentStream contenido,
            String texto,
            float x,
            float y,
            float tamano,
            PDType1Font fuente
    ) throws IOException {

        contenido.setNonStrokingColor(0, 0, 0);

        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(x, y);
        contenido.showText(textoSeguro(texto));
        contenido.endText();
    }


    private String formatearNumero(double valor) {

        return String.format(
                Locale.US,
                "%.2f",
                valor
        );
    }


    private String formatearArea(double valor) {

        return String.format(
                Locale.US,
                "%.2f cm2",
                valor
        );
    }


    private String ajustarTexto(
            String texto,
            PDType1Font fuente,
            float tamano,
            float anchoMaximo
    ) throws IOException {

        String textoSeguro = textoSeguro(texto);

        if (fuente.getStringWidth(textoSeguro) / 1000 * tamano
                <= anchoMaximo) {
            return textoSeguro;
        }

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < textoSeguro.length(); i++) {

            String candidato = resultado.toString()
                    + textoSeguro.charAt(i)
                    + "...";

            float ancho = fuente.getStringWidth(candidato)
                    / 1000 * tamano;

            if (ancho > anchoMaximo) {
                break;
            }

            resultado.append(textoSeguro.charAt(i));
        }

        return resultado + "...";
    }


    // =========================================================
    // PAGINAS Y ENCABEZADOS
    // =========================================================

    private PDPage crearPagina(
            PDDocument documento,
            String titulo
    ) throws IOException {

        PDPage pagina = new PDPage(PDRectangle.A4);

        documento.addPage(pagina);

        try (PDPageContentStream contenido =
                     new PDPageContentStream(documento, pagina)) {

            escribirTexto(
                    contenido,
                    "TableroLab",
                    MARGEN,
                    ALTO_PAGINA - 40,
                    18,
                    FUENTE_NEGRITA
            );

            escribirTexto(
                    contenido,
                    titulo,
                    MARGEN,
                    ALTO_PAGINA - 65,
                    12,
                    FUENTE_NORMAL
            );

            contenido.setLineWidth(0.7f);

            contenido.moveTo(
                    MARGEN,
                    ALTO_PAGINA - 78
            );

            contenido.lineTo(
                    ANCHO_PAGINA - MARGEN,
                    ALTO_PAGINA - 78
            );

            contenido.stroke();
        }

        return pagina;
    }


    private PDPageContentStream abrirContenido(
            PDDocument documento,
            PDPage pagina
    ) throws IOException {

        return new PDPageContentStream(
                documento,
                pagina,
                PDPageContentStream.AppendMode.APPEND,
                true
        );
    }


    private void agregarNumeracionPaginas(
            PDDocument documento
    ) throws IOException {

        int total = documento.getNumberOfPages();

        for (int i = 0; i < total; i++) {

            PDPage pagina = documento.getPage(i);

            try (PDPageContentStream contenido =
                         abrirContenido(documento, pagina)) {

                contenido.setLineWidth(0.5f);

                contenido.moveTo(
                        MARGEN,
                        40
                );

                contenido.lineTo(
                        ANCHO_PAGINA - MARGEN,
                        40
                );

                contenido.stroke();

                escribirTexto(
                        contenido,
                        "TableroLab - Informe de optimizacion",
                        MARGEN,
                        27,
                        8,
                        FUENTE_NORMAL
                );

                String numero = "Pagina "
                        + (i + 1)
                        + " de "
                        + total;

                float anchoTexto =
                        FUENTE_NORMAL.getStringWidth(numero)
                                / 1000 * 8;

                escribirTexto(
                        contenido,
                        numero,
                        ANCHO_PAGINA - MARGEN - anchoTexto,
                        27,
                        8,
                        FUENTE_NORMAL
                );
            }
        }
    }


    // =========================================================
    // RESUMEN GENERAL
    // =========================================================

    private void dibujarResumenGeneral(
            PDDocument documento,
            Tablero tablero,
            String material,
            ResultadoOptimizacion resultado,
            CotizacionTablero cotizacion,
            ResumenCostos resumenCostos
    ) throws IOException {

        PDPage pagina = crearPagina(
                documento,
                "Resumen general del proyecto"
        );

        try (PDPageContentStream contenido =
                     abrirContenido(documento, pagina)) {

            float y = Y_INICIO;
            float salto = 22;

            escribirTexto(
                    contenido,
                    "INFORMACION GENERAL",
                    MARGEN,
                    y,
                    12,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Fecha de generacion: "
                            + LocalDateTime.now().format(FORMATO_FECHA),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Material: " + textoSeguro(material),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Espesor: " + tablero.getEspesor() + " cm",
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Dimensiones del tablero: "
                            + tablero.getAncho()
                            + " x "
                            + tablero.getLargo()
                            + " cm",
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Ancho de corte: "
                            + tablero.getAnchoCorte()
                            + " mm",
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "RESULTADOS DE OPTIMIZACION",
                    MARGEN,
                    y,
                    12,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Tableros utilizados: "
                            + resultado.getCantidadTablerosUtilizados(),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Unidades solicitadas: "
                            + resultado.getTotalUnidadesSolicitadas(),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Unidades colocadas: "
                            + resultado.getTotalUnidadesColocadas(),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Unidades pendientes: "
                            + resultado.getTotalUnidadesPendientes(),
                    MARGEN,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "COTIZACION Y COSTOS",
                    MARGEN,
                    y,
                    12,
                    FUENTE_NEGRITA
            );

            y -= salto;

            if (cotizacion != null) {

                escribirTexto(
                        contenido,
                        "Proveedor: "
                                + textoSeguro(cotizacion.getProveedor()),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Material cotizado: "
                                + textoSeguro(cotizacion.getMaterial()),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Descripcion: "
                                + ajustarTexto(
                                cotizacion.getDescripcion(),
                                FUENTE_NORMAL,
                                9,
                                450
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Formato cotizado: "
                                + cotizacion.getAncho()
                                + " x "
                                + cotizacion.getLargo()
                                + " cm",
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Espesor cotizado: "
                                + cotizacion.getEspesor()
                                + " mm",
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Precio unitario: "
                                + FORMATO_MONEDA.format(
                                cotizacion.getPrecioUnitario()
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                if (cotizacion.getEnlaceProveedor() != null
                        && !cotizacion.getEnlaceProveedor().isBlank()) {

                    escribirTexto(
                            contenido,
                            "Enlace: "
                                    + ajustarTexto(
                                    cotizacion.getEnlaceProveedor(),
                                    FUENTE_NORMAL,
                                    8,
                                    450
                            ),
                            MARGEN,
                            y,
                            8,
                            FUENTE_NORMAL
                    );

                    y -= salto;
                }

            } else {

                escribirTexto(
                        contenido,
                        "No se ha registrado una cotizacion.",
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;
            }

            y -= 5;

            if (resumenCostos != null) {

                escribirTexto(
                        contenido,
                        "RESUMEN ECONOMICO",
                        MARGEN,
                        y,
                        11,
                        FUENTE_NEGRITA
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Tableros utilizados: "
                                + resumenCostos.getTablerosUtilizados(),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Tableros a comprar: "
                                + resumenCostos.getTablerosAComprar(),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Precio unitario: "
                                + FORMATO_MONEDA.format(
                                resumenCostos.getPrecioUnitario()
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Costo de tableros utilizados: "
                                + FORMATO_MONEDA.format(
                                resumenCostos.getCostoUtilizado()
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Costo total de compra: "
                                + FORMATO_MONEDA.format(
                                resumenCostos.getCostoCompra()
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

                y -= salto;

                escribirTexto(
                        contenido,
                        "Diferencia: "
                                + FORMATO_MONEDA.format(
                                resumenCostos.getDiferencia()
                        ),
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );

            } else {

                escribirTexto(
                        contenido,
                        "No hay un resumen de costos disponible.",
                        MARGEN,
                        y,
                        9,
                        FUENTE_NORMAL
                );
            }
        }
    }


    // =========================================================
    // PIEZAS SOLICITADAS
    // =========================================================

    private void dibujarPiezasSolicitadas(
            PDDocument documento,
            List<Pieza> piezasSolicitadas
    ) throws IOException {

        dibujarTablaPiezas(
                documento,
                "Detalle de piezas solicitadas",
                piezasSolicitadas
        );
    }


    // =========================================================
    // PIEZAS PENDIENTES
    // =========================================================

    private void dibujarPiezasPendientes(
            PDDocument documento,
            ResultadoOptimizacion resultado
    ) throws IOException {

        List<Pieza> pendientes = resultado.getPiezasPendientes();

        if (pendientes == null || pendientes.isEmpty()) {
            return;
        }

        dibujarTablaPiezas(
                documento,
                "Piezas pendientes",
                pendientes
        );
    }


    // =========================================================
    // TABLA PAGINADA DE PIEZAS
    // =========================================================

    private void dibujarTablaPiezas(
            PDDocument documento,
            String titulo,
            List<Pieza> piezas
    ) throws IOException {

        int indice = 0;

        while (indice < piezas.size()) {

            PDPage pagina = crearPagina(
                    documento,
                    titulo
            );

            try (PDPageContentStream contenido =
                         abrirContenido(documento, pagina)) {

                float y = Y_INICIO;

                dibujarEncabezadoTablaPiezas(
                        contenido,
                        y
                );

                y -= 22;

                while (indice < piezas.size() && y >= Y_LIMITE) {

                    Pieza pieza = piezas.get(indice);

                    escribirTexto(
                            contenido,
                            ajustarTexto(
                                    pieza.getNombre(),
                                    FUENTE_NORMAL,
                                    8,
                                    195
                            ),
                            MARGEN,
                            y,
                            8,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearNumero(pieza.getAncho()),
                            260,
                            y,
                            8,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearNumero(pieza.getLargo()),
                            350,
                            y,
                            8,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            String.valueOf(pieza.getCantidad()),
                            455,
                            y,
                            8,
                            FUENTE_NORMAL
                    );

                    y -= 17;
                    indice++;
                }
            }
        }

        if (piezas.isEmpty()) {

            PDPage pagina = crearPagina(
                    documento,
                    titulo
            );

            try (PDPageContentStream contenido =
                         abrirContenido(documento, pagina)) {

                escribirTexto(
                        contenido,
                        "No hay piezas registradas.",
                        MARGEN,
                        Y_INICIO,
                        10,
                        FUENTE_NORMAL
                );
            }
        }
    }


    private void dibujarEncabezadoTablaPiezas(
            PDPageContentStream contenido,
            float y
    ) throws IOException {

        escribirTexto(
                contenido,
                "Nombre",
                MARGEN,
                y,
                9,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Ancho (cm)",
                260,
                y,
                9,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Largo (cm)",
                350,
                y,
                9,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Cantidad",
                450,
                y,
                9,
                FUENTE_NEGRITA
        );

        contenido.setLineWidth(0.4f);

        contenido.moveTo(
                MARGEN,
                y - 5
        );

        contenido.lineTo(
                ANCHO_PAGINA - MARGEN,
                y - 5
        );

        contenido.stroke();
    }


    // =========================================================
    // RETAZOS POR TABLERO
    // =========================================================

    private List<Retazo> obtenerRetazosTablero(
            List<Retazo> retazos,
            int numeroTablero
    ) {

        List<Retazo> resultado = new ArrayList<>();

        for (Retazo retazo : retazos) {

            if (retazo.getNumeroTablero() == numeroTablero) {
                resultado.add(retazo);
            }
        }

        return resultado;
    }

    private int obtenerNumeroRetazoEnTablero(
            List<Retazo> retazos,
            int indice
    ) {

        int numero = 0;
        int numeroTablero = retazos.get(indice).getNumeroTablero();

        for (int i = 0; i <= indice; i++) {

            if (retazos.get(i).getNumeroTablero() == numeroTablero) {
                numero++;
            }
        }

        return numero;
    }


    // =========================================================
    // BALANCE DE SUPERFICIES
    // =========================================================

    private void dibujarBalanceSuperficies(
            PDDocument documento,
            Tablero tablero,
            List<PiezaColocada> piezas,
            List<Retazo> retazos,
            int numeroTablero
    ) throws IOException {

        PDPage pagina = crearPagina(
                documento,
                "Balance de superficies - Tablero " + numeroTablero
        );

        double superficieTotal =
                tablero.getAncho() * tablero.getLargo();

        double superficiePiezas = 0;
        double superficieAprovechable = 0;
        double superficieNoAprovechable = 0;

        for (PiezaColocada pieza : piezas) {

            superficiePiezas +=
                    pieza.getAncho() * pieza.getLargo();
        }

        for (Retazo retazo : retazos) {

            if (retazo.getEstado() == EstadoRetazo.APROVECHABLE) {

                superficieAprovechable += retazo.getArea();

            } else {

                superficieNoAprovechable += retazo.getArea();
            }
        }

        double superficieClasificada =
                superficiePiezas
                        + superficieAprovechable
                        + superficieNoAprovechable;

        double superficieResidual =
                superficieTotal - superficieClasificada;

        try (PDPageContentStream contenido =
                     abrirContenido(documento, pagina)) {

            float y = Y_INICIO;
            float salto = 27;

            escribirTexto(
                    contenido,
                    "SUPERFICIE TOTAL DEL TABLERO",
                    MARGEN,
                    y,
                    11,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    formatearArea(superficieTotal),
                    MARGEN + 15,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "SUPERFICIE OCUPADA POR PIEZAS",
                    MARGEN,
                    y,
                    11,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    formatearArea(superficiePiezas),
                    MARGEN + 15,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "RETAZOS APROVECHABLES",
                    MARGEN,
                    y,
                    11,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    formatearArea(superficieAprovechable),
                    MARGEN + 15,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "RETAZOS NO APROVECHABLES",
                    MARGEN,
                    y,
                    11,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    formatearArea(superficieNoAprovechable),
                    MARGEN + 15,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "AREA RESIDUAL ESTIMADA",
                    MARGEN,
                    y,
                    11,
                    FUENTE_NEGRITA
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    formatearArea(superficieResidual),
                    MARGEN + 15,
                    y,
                    10,
                    FUENTE_NORMAL
            );

            y -= salto * 1.5f;

            escribirTexto(
                    contenido,
                    "Superficie clasificada: "
                            + formatearArea(superficieClasificada),
                    MARGEN,
                    y,
                    9,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Cantidad de piezas colocadas: "
                            + piezas.size(),
                    MARGEN,
                    y,
                    9,
                    FUENTE_NORMAL
            );

            y -= salto;

            escribirTexto(
                    contenido,
                    "Cantidad de retazos: "
                            + retazos.size(),
                    MARGEN,
                    y,
                    9,
                    FUENTE_NORMAL
            );

            y -= salto * 2;

            escribirTexto(
                    contenido,
                    "NOTA:",
                    MARGEN,
                    y,
                    9,
                    FUENTE_NEGRITA
            );

            y -= 16;

            escribirTexto(
                    contenido,
                    "El area residual incluye cortes y espacios no clasificados.",
                    MARGEN,
                    y,
                    8,
                    FUENTE_NORMAL
            );

            y -= 13;

            escribirTexto(
                    contenido,
                    "No representa exclusivamente material perdido por el corte.",
                    MARGEN,
                    y,
                    8,
                    FUENTE_NORMAL
            );
        }
    }


    // =========================================================
    // DETALLE COMPLETO DE RETAZOS
    // =========================================================

    private void dibujarDetalleRetazos(
            PDDocument documento,
            List<Retazo> retazos
    ) throws IOException {

        if (retazos == null || retazos.isEmpty()) {
            return;
        }

        int indice = 0;

        while (indice < retazos.size()) {

            PDPage pagina = crearPagina(
                    documento,
                    "Detalle de retazos generados"
            );

            try (PDPageContentStream contenido =
                         abrirContenido(documento, pagina)) {

                float y = Y_INICIO;

                dibujarEncabezadoRetazos(
                        contenido,
                        y
                );

                y -= 23;

                while (indice < retazos.size() && y >= Y_LIMITE) {

                    Retazo retazo = retazos.get(indice);

                    int numeroRetazo =
                            obtenerNumeroRetazoEnTablero(retazos, indice);

                    String identificador =
                            "T"
                                    + retazo.getNumeroTablero()
                                    + "-R"
                                    + numeroRetazo;

                    escribirTexto(
                            contenido,
                            identificador,
                            MARGEN,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            String.valueOf(retazo.getNumeroTablero()),
                            105,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearNumero(retazo.getX())
                                    + ", "
                                    + formatearNumero(retazo.getY()),
                            145,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearNumero(retazo.getAncho()),
                            250,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearNumero(retazo.getLargo()),
                            315,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    escribirTexto(
                            contenido,
                            formatearArea(retazo.getArea()),
                            375,
                            y,
                            7,
                            FUENTE_NORMAL
                    );

                    String estado =
                            retazo.isAprovechable()
                                    ? "Aprovechable"
                                    : "No aprovechable";

                    escribirTexto(
                            contenido,
                            estado,
                            465,
                            y,
                            6,
                            FUENTE_NORMAL
                    );

                    y -= 17;
                    indice++;
                }
            }
        }
    }


    private void dibujarEncabezadoRetazos(
            PDPageContentStream contenido,
            float y
    ) throws IOException {

        escribirTexto(
                contenido,
                "ID",
                MARGEN,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Tablero",
                105,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Posicion X,Y",
                145,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Ancho",
                250,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Largo",
                315,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Superficie",
                375,
                y,
                7,
                FUENTE_NEGRITA
        );

        escribirTexto(
                contenido,
                "Estado",
                465,
                y,
                7,
                FUENTE_NEGRITA
        );

        contenido.setLineWidth(0.4f);

        contenido.moveTo(
                MARGEN,
                y - 5
        );

        contenido.lineTo(
                ANCHO_PAGINA - MARGEN,
                y - 5
        );

        contenido.stroke();
    }


    // =========================================================
    // PLANO DE CORTE
    // =========================================================

    private void dibujarPlanoTablero(
            PDDocument documento,
            Tablero tablero,
            List<PiezaColocada> piezasColocadas,
            int numeroTablero,
            List<Retazo> retazos
    ) throws IOException {

        PDPage pagina = crearPagina(
                documento,
                "Plano de corte - Tablero " + numeroTablero
        );

        try (PDPageContentStream contenido =
                     abrirContenido(documento, pagina)) {

            float areaDisponibleAncho =
                    ANCHO_PAGINA - 2 * MARGEN - 45;

            float areaDisponibleAlto =
                    ALTO_PAGINA - 230;

            float escala = Math.min(
                    areaDisponibleAncho / (float) tablero.getAncho(),
                    areaDisponibleAlto / (float) tablero.getLargo()
            );

            float anchoPlano =
                    (float) tablero.getAncho() * escala;

            float largoPlano =
                    (float) tablero.getLargo() * escala;

            float origenX =
                    (ANCHO_PAGINA - anchoPlano) / 2;

            float origenY = 75;

            // Contorno del tablero.
            contenido.setLineWidth(1.5f);

            contenido.addRect(
                    origenX,
                    origenY,
                    anchoPlano,
                    largoPlano
            );

            contenido.stroke();

            // Cotas exteriores.
            dibujarCotaHorizontal(
                    contenido,
                    origenX,
                    origenX + anchoPlano,
                    origenY + largoPlano,
                    "Ancho: " + tablero.getAncho() + " cm"
            );

            dibujarCotaVertical(
                    contenido,
                    origenX,
                    origenY,
                    origenY + largoPlano,
                    "Largo: " + tablero.getLargo() + " cm"
            );

            // Retazos.
            dibujarRetazos(
                    contenido,
                    retazos,
                    numeroTablero,
                    (float) tablero.getLargo(),
                    origenX,
                    origenY,
                    escala
            );

            // Piezas colocadas.
            for (PiezaColocada pieza : piezasColocadas) {

                float x = origenX
                        + (float) pieza.getX() * escala;

                float y = origenY
                        + largoPlano
                        - (
                        (float) pieza.getY()
                                + (float) pieza.getLargo()
                ) * escala;

                float ancho =
                        (float) pieza.getAncho() * escala;

                float largo =
                        (float) pieza.getLargo() * escala;

                contenido.setLineWidth(0.8f);

                contenido.setNonStrokingColor(
                        0.82f,
                        0.88f,
                        0.94f
                );

                contenido.addRect(
                        x,
                        y,
                        ancho,
                        largo
                );

                contenido.fillAndStroke();

                String nombre =
                        pieza.getPiezaOriginal().getNombre();

                String medidas =
                        pieza.getAncho()
                                + " x "
                                + pieza.getLargo()
                                + " cm";

                if (ancho > 45 && largo > 30) {

                    float centroY = y + largo / 2;

                    float anchoNombre =
                            FUENTE_NEGRITA.getStringWidth(
                                    textoSeguro(nombre)
                            ) / 1000 * 7;

                    float anchoMedidas =
                            FUENTE_NORMAL.getStringWidth(
                                    textoSeguro(medidas)
                            ) / 1000 * 7;

                    float etiquetaAncho = Math.min(
                            Math.max(anchoNombre, anchoMedidas) + 10,
                            ancho - 4
                    );

                    contenido.setNonStrokingColor(1, 1, 1);

                    contenido.addRect(
                            x + 2,
                            centroY - 10,
                            etiquetaAncho,
                            23
                    );

                    contenido.fill();

                    escribirTexto(
                            contenido,
                            ajustarTexto(
                                    nombre,
                                    FUENTE_NEGRITA,
                                    7,
                                    etiquetaAncho - 6
                            ),
                            x + 5,
                            centroY + 2,
                            7,
                            FUENTE_NEGRITA
                    );

                    escribirTexto(
                            contenido,
                            ajustarTexto(
                                    medidas,
                                    FUENTE_NORMAL,
                                    7,
                                    etiquetaAncho - 6
                            ),
                            x + 5,
                            centroY - 7,
                            7,
                            FUENTE_NORMAL
                    );
                }
            }

            escribirTexto(
                    contenido,
                    "Dimensiones: "
                            + tablero.getAncho()
                            + " x "
                            + tablero.getLargo()
                            + " cm",
                    MARGEN,
                    45,
                    9,
                    FUENTE_NORMAL
            );
        }
    }


    // =========================================================
    // COTAS DEL PLANO
    // =========================================================

    private void dibujarCotaHorizontal(
            PDPageContentStream contenido,
            float xInicio,
            float xFin,
            float yTablero,
            String texto
    ) throws IOException {

        float separacion = 18;
        float yCota = yTablero + separacion;
        float tamanoFlecha = 4;

        contenido.setStrokingColor(0, 0, 0);
        contenido.setNonStrokingColor(0, 0, 0);
        contenido.setLineWidth(0.6f);

        contenido.moveTo(xInicio, yTablero);
        contenido.lineTo(xInicio, yCota + 4);

        contenido.moveTo(xFin, yTablero);
        contenido.lineTo(xFin, yCota + 4);

        contenido.stroke();

        contenido.moveTo(xInicio, yCota);
        contenido.lineTo(xFin, yCota);

        contenido.stroke();

        contenido.moveTo(xInicio, yCota);
        contenido.lineTo(
                xInicio + tamanoFlecha,
                yCota + 2
        );

        contenido.moveTo(xInicio, yCota);
        contenido.lineTo(
                xInicio + tamanoFlecha,
                yCota - 2
        );

        contenido.moveTo(xFin, yCota);
        contenido.lineTo(
                xFin - tamanoFlecha,
                yCota + 2
        );

        contenido.moveTo(xFin, yCota);
        contenido.lineTo(
                xFin - tamanoFlecha,
                yCota - 2
        );

        contenido.stroke();

        float anchoTexto =
                FUENTE_NORMAL.getStringWidth(
                        textoSeguro(texto)
                ) / 1000 * 8;

        escribirTexto(
                contenido,
                texto,
                (xInicio + xFin - anchoTexto) / 2,
                yCota + 5,
                8,
                FUENTE_NORMAL
        );
    }


    private void dibujarCotaVertical(
            PDPageContentStream contenido,
            float xTablero,
            float yInicio,
            float yFin,
            String texto
    ) throws IOException {

        float separacion = 22;
        float xCota = xTablero - separacion;
        float tamanoFlecha = 4;

        contenido.setStrokingColor(0, 0, 0);
        contenido.setNonStrokingColor(0, 0, 0);
        contenido.setLineWidth(0.6f);

        contenido.moveTo(xTablero, yInicio);
        contenido.lineTo(xCota - 4, yInicio);

        contenido.moveTo(xTablero, yFin);
        contenido.lineTo(xCota - 4, yFin);

        contenido.stroke();

        contenido.moveTo(xCota, yInicio);
        contenido.lineTo(xCota, yFin);

        contenido.stroke();

        contenido.moveTo(xCota, yInicio);
        contenido.lineTo(
                xCota - 2,
                yInicio + tamanoFlecha
        );

        contenido.moveTo(xCota, yInicio);
        contenido.lineTo(
                xCota + 2,
                yInicio + tamanoFlecha
        );

        contenido.moveTo(xCota, yFin);
        contenido.lineTo(
                xCota - 2,
                yFin - tamanoFlecha
        );

        contenido.moveTo(xCota, yFin);
        contenido.lineTo(
                xCota + 2,
                yFin - tamanoFlecha
        );

        contenido.stroke();

        float anchoTexto =
                FUENTE_NORMAL.getStringWidth(
                        textoSeguro(texto)
                ) / 1000 * 8;

        contenido.beginText();

        contenido.setFont(
                FUENTE_NORMAL,
                8
        );

        contenido.setTextMatrix(
                Matrix.getRotateInstance(
                        (float) (Math.PI / 2),
                        xCota - 10,
                        (yInicio + yFin - anchoTexto) / 2
                )
        );

        contenido.showText(textoSeguro(texto));
        contenido.endText();
    }


    // =========================================================
    // DIBUJO DE RETAZOS
    // =========================================================

    private void dibujarRetazos(
            PDPageContentStream contenido,
            List<Retazo> retazos,
            int numeroTablero,
            float largoTablero,
            float origenX,
            float origenY,
            float escala
    ) throws IOException {

        int numeroRetazo = 1;

        for (Retazo retazo : retazos) {

            float x =
                    origenX + (float) retazo.getX() * escala;

            float y =
                    origenY
                            + largoTablero * escala
                            - (
                            (float) retazo.getY()
                                    + (float) retazo.getLargo()
                    ) * escala;

            float ancho =
                    (float) retazo.getAncho() * escala;

            float largo =
                    (float) retazo.getLargo() * escala;

            contenido.setLineWidth(0.4f);

            contenido.setStrokingColor(
                    0.39f,
                    0.39f,
                    0.39f
            );

            contenido.addRect(
                    x,
                    y,
                    ancho,
                    largo
            );

            contenido.stroke();

            // Achurado diagonal.
            contenido.saveGraphicsState();

            contenido.addRect(
                    x,
                    y,
                    ancho,
                    largo
            );

            contenido.clip();

            contenido.setLineWidth(0.35f);

            for (float desplazamiento = -largo;
                 desplazamiento < ancho;
                 desplazamiento += 6) {

                contenido.moveTo(
                        x + desplazamiento,
                        y
                );

                contenido.lineTo(
                        x + desplazamiento + largo,
                        y + largo
                );
            }

            contenido.stroke();
            contenido.restoreGraphicsState();

            // Identificacion.
            String identificador =
                    "T" + numeroTablero + "-R" + numeroRetazo;

            String medidas = String.format(
                    Locale.US,
                    "%.1f x %.1f",
                    retazo.getAncho(),
                    retazo.getLargo()
            );

            contenido.beginText();

            contenido.setFont(
                    FUENTE_NEGRITA,
                    6
            );

            contenido.setNonStrokingColor(
                    0.15f,
                    0.15f,
                    0.15f
            );

            if (ancho >= 30 && largo >= 18) {

                contenido.newLineAtOffset(
                        x + 2,
                        y + largo / 2
                );

                contenido.showText(
                        identificador + " " + medidas
                );

            } else if (ancho >= 12 && largo >= 10) {

                contenido.newLineAtOffset(
                        x + 2,
                        y + largo / 2
                );

                contenido.showText(identificador);
            }

            contenido.endText();

            numeroRetazo++;
        }
    }
}
