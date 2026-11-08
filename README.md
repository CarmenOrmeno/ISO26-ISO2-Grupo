# Radiodiagnóstico SESCAM · ISO26-ISO2-Grupo

Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM.
Proyecto de prácticas de Ingeniería del Software II (curso 2026/2027).

## Estructura

```
ISO26-ISO2-Grupo/
├── pom.xml            # pom padre «radiodiagnostico» (producto, sin código)
├── servidor/          # módulo Maven: backend (modelo, servicios, controladores/API)
├── cliente/           # módulo Maven: cliente de consola (vista + controlador, MVC)
├── docs/wiki/         # páginas de la wiki (guía de lectura del proyecto)
├── docs/casos-de-uso/ # especificación de cada caso de uso (una por iteración)
├── DECISIONS.md       # registro de decisiones del proyecto
└── CONTEXT.md         # reglas de arquitectura para los agentes de IA
```

## Cómo se construye

Requisitos: JDK 17 y Maven 3.9.

```bash
mvn verify          # compila, pasa las pruebas y genera el informe de JaCoCo
mvn verify site     # además genera el sitio con los informes de Surefire y JaCoCo
```

Informes: `servidor/target/site/index.html` y `cliente/target/site/index.html`.

## Cómo se ejecuta el cliente

```bash
mvn -q install -DskipTests
java -jar cliente/target/cliente-*.jar
```

Usuarios de demostración (SSO simulado): `paciente1`, `medico1`, `admision1`, `admin1`
(contraseña: `demo`).

## Documentación

Empezad por la wiki: [docs/wiki/Home.md](docs/wiki/Home.md).
