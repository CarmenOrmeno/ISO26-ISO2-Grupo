# Radiodiagnóstico SESCAM · Guía de lectura

Esta wiki es la **guía de lectura** del proyecto: desde aquí se llega a todos los artefactos.
Mientras la wiki de GitHub no esté activa, las páginas viven en `docs/wiki/` y se copian tal cual a la wiki.

| Página | Qué contiene |
|---|---|
| [Equipo y roles](Equipo-y-roles.md) | Quién es quién, quién coordina y quién aprueba qué |
| [Actas de reuniones](Actas-de-reuniones.md) | Una por reunión, con decisiones y compromisos → issues |
| [Requisitos y casos de uso](Requisitos-y-casos-de-uso.md) | RF, actores, diagramas generales y especificación de cada CU |
| [Planificación](Planificacion.md) | Fases, iteraciones, esfuerzo RADIT, costes, fechas y versiones |
| [Arquitectura](Arquitectura.md) | Módulos, capas, paquetes e interfaces (contratos) |
| [Plan de gestión de configuración](Plan-de-gestion-de-configuracion.md) | Git Flow, versiones, responsables, CI |
| [Plan de calidad](Plan-de-calidad.md) | Características ISO 25010 priorizadas y cómo se controlan |
| [Plan de pruebas](Plan-de-pruebas.md) | Diseño de casos de prueba y resultados de Surefire y JaCoCo |
| [Decisiones](Decisiones.md) | Enlace a [DECISIONS.md](../../DECISIONS.md) |
| [Autoevaluación y experiencia](Autoevaluacion-y-experiencia.md) | Una por persona (obligatoria) |

## Trazabilidad

```
Acta (wiki) ──► Decisión (DECISIONS.md) ──► Issue padre CUxx (plantilla «Caso de uso»)
                                             ├─► Sub-issue servidor ─► feature/CUxx-servidor ─► PR «Closes #n»
                                             └─► Sub-issue cliente  ─► feature/CUxx-cliente  ─► PR «Closes #m»
```

Cada CU tiene su especificación en [docs/casos-de-uso/](../casos-de-uso/) y su rama `feature/CUxx-…`
que no se borra al fusionar. En *Insights → Network* se ve la historia de las ramas.
