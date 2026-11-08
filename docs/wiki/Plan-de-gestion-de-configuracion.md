# Plan de gestión de configuración

## 1. Elementos de configuración (EC)

| EC | Ubicación | Responsable | Aprueba | Versionado |
|---|---|---|---|---|
| Requisitos y CU | `docs/wiki/Requisitos-y-casos-de-uso.md`, `docs/casos-de-uso/` | Coordinador | Equipo (acta) | Git |
| Planificación | `docs/wiki/Planificacion.md` | Coordinador | Equipo (acta) | Git |
| Decisiones | `DECISIONS.md` | Quien toma la decisión | Equipo (acta) | Git |
| Reglas para agentes | `CONTEXT.md`, `.github/copilot-instructions.md` | Arquitecto | Coordinador | Git |
| pom padre (producto) | `pom.xml` | Resp. configuración | Coordinador | SemVer del producto |
| Módulo servidor | `servidor/` | Asignado en cada issue | Otro miembro (PR) | SemVer del módulo |
| Módulo cliente | `cliente/` | Asignado en cada issue | Otro miembro (PR) | SemVer del módulo |
| Pruebas | `*/src/test/java` | Resp. calidad | Arquitecto | Con su módulo |
| Workflow de CI | `.github/workflows/maven.yml` | Resp. configuración | Coordinador | Git |

## 2. Estrategia de ramas: Git Flow (D-006)

| Rama | Nace de | Se fusiona en | Uso |
|---|---|---|---|
| `main` | — | — | Solo versiones entregables, etiquetadas `vX.Y.Z`. **Nunca** commits directos |
| `develop` | `main` | — | Integración. Rama por defecto |
| `feature/CUxx-<modulo>` | `develop` | `develop` | Un CU = una iteración (`feature/CU06-servidor`, `feature/CU06-cliente`) |
| `feature/<tarea>` | `develop` | `develop` | Tareas que no son un CU (documentación, pruebas, CI) |
| `bugfix/<descripcion>` | `develop` | `develop` | Error encontrado en develop ya fusionado (un issue por fallo) |
| `release/X.Y.Z` | `develop` | `main` y `develop` | Cerrar versión: quitar `-SNAPSHOT`, solo correcciones |
| `hotfix/X.Y.Z` | `main` | `main` y `develop` | Arreglar una versión entregada |

Reglas:
- `main` y `develop` protegidas con *ruleset*: pull request + 1 aprobación, sin borrado ni *force push*.
- Nadie aprueba su propio PR ni un PR con el CI en rojo. Nunca `-DskipTests` para pasar el CI.
- Las ramas **no se borran** al fusionar.
- Commits: `tipo(CUxx): descripción (#issue)`. El PR lleva `Closes #n`.

## 3. Plan de versiones (Semantic Versioning)

Dos niveles, como en la sesión de Maven:

- **Producto** (`radiodiagnostico`, pom padre y etiquetas de `main`): `0.1.0-SNAPSHOT` mientras se
  desarrolla; en `release/0.1.0` pasa a `0.1.0` y se etiqueta `v0.1.0`. Después `develop` pasa a
  `0.2.0-SNAPSHOT`.
- **Módulos**: `1.0.0` con su primer CU; sube la **menor** con cada CU nuevo y el **parche** con cada
  corrección (`bugfix`/`hotfix`). La **mayor** solo si cambia un contrato `api` de forma incompatible.

| Producto | servidor | cliente | CU incluidos |
|---|---|---|---|
| v0.1.0 | 1.5.0 | 1.1.0 | CU01–CU06 |
| v0.2.0 | 1.15.0 | 1.3.0 | CU07–CU16 |
| v1.0.0 | 1.21.0 | 1.4.0 | CU17–CU22 + ItN |

En la rama `release` hay que cambiar también `<parent><version>` de los dos módulos.

## 4. Construcciones e integración continua

- `mvn verify` en la raíz construye los dos módulos (reactor), pasa las pruebas y comprueba la cobertura.
- GitHub Actions (`.github/workflows/maven.yml`) ejecuta `mvn -B verify` con Java 17 en cada *push* y PR a
  `main` y `develop`.
- Los informes (`mvn verify site`) no se suben: `target/` está en `.gitignore`; los resultados se resumen
  en el [Plan de pruebas](Plan-de-pruebas.md).

## 5. Control de cambios

1. El cambio se discute en una reunión → acta.
2. Se crea un issue (plantilla «Caso de uso» si es un CU) con responsable, en el tablero.
3. Rama desde el issue → commits → PR a `develop` con `Closes #n`.
4. Revisión por otro miembro (checklist: compila, CI verde, cobertura, SOLID, sin tocar otros CU).
5. Fusión; el issue pasa a *Done*. Si afecta a una decisión, se actualiza `DECISIONS.md`.
