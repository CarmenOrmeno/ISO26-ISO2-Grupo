# Radiodiagnóstico SESCAM · ISO26-ISO2-Grupo

Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM.
Proyecto de prácticas de Ingeniería del Software II (curso 2026/2027).

📖 **[Wiki del proyecto](https://github.com/CarmenOrmeno/ISO26-ISO2-Grupo/wiki)**: guía de lectura con requisitos, planificación,
arquitectura, planes y decisiones. Se genera desde `docs/wiki/` con `scripts/publicar_wiki.py` (workflow `wiki.yml`).

## Estructura

```
ISO26-ISO2-Grupo/
├── pom.xml            # pom padre «radiodiagnostico» (producto, sin código)
├── servidor/          # módulo Maven: backend (modelo, servicios, controladores/API)
├── cliente/           # módulo Maven: cliente JavaFX y de consola (vistas + controladores, MVC)
├── docs/wiki/         # páginas de la wiki (fuente; se publican solas en la wiki de GitHub)
├── scripts/           # publicar_wiki.py: genera la wiki desde docs/
├── docs/casos-de-uso/ # especificación de cada caso de uso (una por iteración)
├── DECISIONS.md       # registro de decisiones del proyecto
└── CONTEXT.md         # reglas de arquitectura para los agentes de IA
```

## Cómo se construye

Requisitos: JDK 17 y Maven 3.9.

```bash
mvn verify               # compila, pasa las pruebas y genera el informe de JaCoCo
mvn site site:stage      # después: sitio único con los informes de Surefire y JaCoCo
```

Los dos comandos van por separado: si se lanzan juntos, el sitio de la raíz se genera antes de que los
módulos hayan pasado sus pruebas y el informe de Surefire sale vacío.

Informes: abrir **`target/staging/index.html`**. La raíz agrupa las pruebas de los dos módulos en un único
`surefire.html`; la cobertura de JaCoCo está en `target/staging/servidor/jacoco` y `target/staging/cliente/jacoco`.

> Si el proyecto está en una carpeta sincronizada con iCloud (`~/Documents`, `~/Desktop`), iCloud crea copias
> «` 2`» (`surefire 2.html`, `target 2`…). Conviene trabajar fuera de iCloud.

## Cómo se ejecuta el cliente

```bash
mvn -q install -DskipTests
java -jar cliente/target/cliente-*.jar             # vista gráfica JavaFX (D-017)
java -jar cliente/target/cliente-*.jar --consola   # vista de consola
mvn -q -pl cliente javafx:run                      # JavaFX desde Maven (tras el install)
```

El jar lleva JavaFX para la plataforma en la que se construye (macOS, Linux o Windows). En la ventana
principal, a la izquierda están las acciones del rol agrupadas por caso de uso; al elegir una se genera su
formulario y, al pulsar **Ejecutar**, se ve el resultado (las consultas sin datos se ejecutan solas).

Sin configuración, los datos se guardan en memoria. Para guardarlos en **MySQL 8** (D-018):

```bash
mysql -uroot -p < scripts/crear-bd.sql        # una vez: bases de datos y usuario (cambia la clave)
export RADIO_DB_URL=jdbc:mysql://127.0.0.1:3306/radiodiagnostico
export RADIO_DB_TEST_URL=jdbc:mysql://127.0.0.1:3306/radiodiagnostico_test   # pruebas de integración
export RADIO_DB_USUARIO=radiodiagnostico
export RADIO_DB_CLAVE=<tu clave>
java -jar cliente/target/cliente-*.jar         # al arrancar indica dónde guarda los datos
```

Las pruebas contra MySQL se omiten si no está `RADIO_DB_TEST_URL`; en el CI siempre se ejecutan.

Usuarios de demostración (SSO simulado): `paciente1`, `medico1`, `admision1`, `admin1`
(contraseña: `demo`).

## Documentación

Empezad por la wiki: [docs/wiki/Home.md](docs/wiki/Home.md).
