# TableroLab

**Optimización de cortes de tableros, aprovechamiento de materiales y control de costos en un solo lugar.**

TableroLab es una aplicación de escritorio desarrollada para facilitar la planificación y optimización de cortes de tableros de madera. Su propósito es ayudar a carpinteros, talleres y usuarios independientes a organizar sus proyectos, distribuir piezas sobre tableros, visualizar los resultados y estimar los costos de los materiales.

El proyecto busca acercar herramientas de planificación y optimización a más personas mediante una aplicación intuitiva y de instalación sencilla.

---

## 1. Problema que aborda

La planificación manual de cortes puede provocar:

- Desperdicio innecesario de material.
- Dificultades para distribuir las piezas sobre los tableros.
- Pérdida de tiempo en cálculos y planificación.
- Dificultad para estimar el costo de los materiales.
- Poco control sobre los espacios sobrantes después del corte.

TableroLab busca centralizar estas tareas en una sola herramienta.

## 2. Propuesta de valor

TableroLab integra herramientas para planificar cortes, visualizar su distribución, calcular costos y analizar los retazos resultantes.

Sus principales objetivos son:

- Facilitar la planificación de proyectos de carpintería.
- Optimizar la distribución de piezas sobre los tableros.
- Aprovechar mejor la superficie disponible.
- Facilitar la estimación económica de los materiales.
- Reducir el tiempo dedicado a tareas manuales de planificación.
- Ofrecer una alternativa gratuita y accesible.

## 3. Funcionalidades principales

- **Gestión de proyectos:** creación y administración de proyectos de corte.
- **Gestión de materiales:** administración de materiales y formatos de tableros.
- **Gestión de piezas:** ingreso y organización de las piezas requeridas.
- **Optimización de cortes:** distribución de piezas mediante algoritmos heurísticos.
- **Rotación de piezas:** consideración de orientaciones permitidas durante la optimización.
- **Ancho de corte:** consideración del material perdido por el espesor de la herramienta de corte.
- **Optimización de múltiples tableros:** distribución de piezas utilizando una cantidad configurable de tableros.
- **Visualización gráfica:** representación de los tableros, piezas colocadas y espacios disponibles.
- **Cálculo de retazos:** identificación geométrica de los espacios sobrantes.
- **Cálculo de costos:** estimación del costo de los tableros utilizados.
- **Cotizaciones:** generación y administración de cotizaciones.
- **Exportación PDF:** generación de informes con resultados, planos, piezas, costos y retazos.
- **Respaldo y restauración:** herramientas para proteger y recuperar la información almacenada.
- **Interfaz gráfica:** aplicación de escritorio con tema visual configurable.

## 4. Tecnologías utilizadas

| Tecnología | Utilización |
|---|---|
| Java 21 | Lenguaje de programación |
| JavaFX | Interfaz gráfica |
| Maven | Gestión de dependencias y construcción |
| SQLite | Base de datos local |
| PDFBox | Generación de documentos PDF |
| JUnit 5 | Pruebas automatizadas |
| jpackage | Generación de distribución e instalador de Windows |

## 5. Arquitectura del proyecto

TableroLab utiliza una arquitectura organizada por responsabilidades:

| Paquete | Responsabilidad |
|---|---|
| `app` | Inicio y configuración de la aplicación |
| `ui` | Interfaz gráfica y componentes visuales |
| `model` | Entidades y modelos de datos |
| `optimizer` | Algoritmos de optimización de cortes |
| `service` | Lógica de negocio, cálculos y generación de documentos |
| `persistence` | Acceso a datos y operaciones con SQLite |
| `security` | Respaldo y restauración de información |

La aplicación funciona como un programa de escritorio. La interfaz se comunica con los servicios y la capa de persistencia mediante llamadas internas de Java. No requiere un servidor remoto para sus operaciones principales.

## 6. Requisitos

### Para utilizar el instalador

- Windows compatible con el ejecutable distribuido.
- Instalador de TableroLab.
- No es necesario instalar Java por separado, ya que la distribución incorpora su propio entorno de ejecución.

### Para ejecutar desde el código fuente

- JDK 21.
- Maven.
- IntelliJ IDEA u otro entorno compatible con proyectos Maven.

## 7. Instalación

### Opción 1: Instalador de Windows

1. Descargar el archivo `TableroLab-1.0.0.exe` desde la sección de versiones o distribución del proyecto.
2. Ejecutar el instalador.
3. Seguir las instrucciones de instalación.
4. Abrir TableroLab desde el acceso correspondiente.

La distribución de Windows incorpora el entorno de ejecución de Java necesario para iniciar la aplicación.

### Opción 2: Ejecutar desde el código fuente

Clonar el repositorio:

```bash
git clone URL_DEL_REPOSITORIO
```

Ingresar al directorio:

```bash
cd TableroLab
```

Ejecutar las pruebas:

```bash
mvn test
```

Construir el proyecto:

```bash
mvn package
```

La ejecución desde el código fuente requiere un entorno Java 21 y las dependencias definidas en el proyecto Maven.

## 8. Base de datos y almacenamiento

TableroLab utiliza SQLite para almacenar localmente la información.

La ubicación predeterminada de la base de datos es:

```text
%USERPROFILE%\.tablerolab\tablerolab.db
```

La aplicación dispone de mecanismos de respaldo y restauración para facilitar la protección de los datos del usuario.

## 9. Pruebas de software

El proyecto incorpora pruebas automatizadas con JUnit 5.

En la última ejecución de validación se obtuvo el siguiente resultado:

| Indicador | Resultado |
|---|---:|
| Pruebas ejecutadas | 145 |
| Fallos | 0 |
| Errores | 0 |
| Pruebas omitidas | 0 |
| Resultado de construcción | BUILD SUCCESS |

Las pruebas incluyen componentes relacionados con los algoritmos de optimización, distribución de múltiples tableros, cálculo de retazos y persistencia de datos.

Estos resultados corresponden a las pruebas automatizadas existentes; no representan una medición de cobertura total del código ni sustituyen las pruebas de usabilidad y rendimiento.

## 10. Alcance y consideraciones

- La optimización utiliza algoritmos heurísticos y no garantiza encontrar la distribución matemáticamente óptima en todos los casos.
- El cálculo de costos considera el precio de los tableros registrados; no representa un presupuesto integral de fabricación.
- El cálculo de retazos identifica espacios geométricos disponibles, pero no constituye un sistema automático de inventario de sobrantes.
- La optimización de múltiples tableros admite una configuración de hasta cinco tableros.

## 11. Estado del proyecto

**Versión:** 1.0.0

TableroLab cuenta con una aplicación de escritorio funcional, persistencia local, herramientas de optimización, visualización gráfica, cálculo de costos, exportación PDF y mecanismos de respaldo y restauración.

El proyecto se encuentra en una etapa de consolidación y documentación, con posibilidades de evolución a partir de la retroalimentación de sus usuarios.

## 12. Mejoras futuras

Entre las posibles líneas de evolución se consideran:

- Mejoras en los algoritmos de optimización.
- Ampliación de las herramientas de gestión de retazos.
- Mejoras de experiencia de usuario.
- Incorporación de nuevas herramientas de análisis.
- Validación con talleres y usuarios del sector de la carpintería.
- Evaluación de alternativas de sostenibilidad económica para mantener el acceso gratuito.

## 13. Licencia y autoría

**Proyecto:** TableroLab  
**Versión:** 1.0.0  
**Tipo:** Aplicación de escritorio  
**Lenguaje principal:** Java

La licencia del proyecto hasta entonces, no se presupone que el código esté publicado bajo una licencia de código abierto.
