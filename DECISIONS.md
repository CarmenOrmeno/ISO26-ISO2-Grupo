# Decisiones del proyecto

Una entrada por decisión, escrita cuando se toma y enlazada a su acta y a su issue.
Formato: Fecha · Iteración · Acta · Issue — Contexto — Decisión — Alternativas — Motivo — Consecuencias.

> Las referencias «Acta» e «Issue» se completan con el enlace real de la wiki y el número
> de issue de GitHub cuando se crea.

---

## D-001 · Agente para implementar
Fecha: 05/10 · It. 0 · Acta: 05/10 · Issue: It0
- **Contexto:** necesitamos un agente que implemente el diseño y los casos de prueba.
- **Decisión:** VS Code + GitHub Copilot (licencia de estudiante) y Claude Code como agente de apoyo.
- **Alternativas:** OpenCode (menos soporte), generación de código de Visual Paradigm (solo esqueletos).
- **Motivo:** licencia UCLM gratuita, integración con el repositorio y con Maven.
- **Consecuencias:** coste de licencias 0 € para el equipo; el humano revisa todo lo que propone el agente.

## D-002 · Estructura Maven multimódulo
Fecha: 19/10 · It. 0 · Acta: 19/10 · Issue: Estructura Maven
- **Contexto:** hay dos aplicaciones, servidor y cliente, en un mismo repositorio.
- **Decisión:** pom padre `radiodiagnostico` (packaging pom) con dos módulos: `servidor` y `cliente`.
- **Alternativas:** dos repositorios; un solo módulo; un módulo Maven por caso de uso.
- **Motivo:** un `mvn verify` construye todo y las versiones se fijan una vez en el padre. Un módulo
  por caso de uso generaría 22 módulos con dependencias cruzadas (sobreingeniería).
- **Consecuencias:** cada módulo con su versión; al cambiar la del padre hay que actualizar `<parent>`.
  Los componentes de cada CU se separan como **paquetes** dentro del módulo servidor.

## D-003 · Versión de Java 17
Fecha: 19/10 · It. 0 · Acta: 19/10
- **Contexto:** las transparencias proponen Java 21, pero el JDK instalado en los equipos es el 17 LTS.
- **Decisión:** `maven.compiler.release` = 17 en el pom padre y `java-version: '17'` en el workflow.
- **Alternativas:** Java 21 (obliga a reinstalar el JDK a todo el equipo).
- **Motivo:** 17 es LTS, es el mínimo de JUnit 6 y ya está instalado.
- **Consecuencias:** se pueden usar `record`, `switch` con flechas y `sealed`; no `pattern matching` de `switch` (21).

## D-004 · Requisitos funcionales: una viñeta, un RF
Fecha: 05/10 · It. 0 · Acta: 05/10
- **Contexto:** viñetas como «Consulta y gestión de sus citas» podrían ser uno o varios RF.
- **Decisión:** cada viñeta del enunciado es un RF; «consultar y gestionar» es un único CU con varios flujos.
  Se añade el RF22 «Iniciar sesión», que no aparece en ninguna viñeta pero es necesario (confidencialidad).
- **Alternativas:** partir cada viñeta en consulta + gestión (44 CU).
- **Motivo:** la regla 1 RF : 1 CU : 1 iteración mantiene la planificación manejable.
- **Consecuencias:** 22 RF → 22 CU → 22 iteraciones de construcción/elaboración + It0 + ItN.

## D-005 · Herencia de actores
Fecha: 05/10 · It. 0 · Acta: 05/10
- **Contexto:** «el médico tiene las funcionalidades del paciente que le apliquen».
- **Decisión:** en el diagrama de casos de uso, el actor Médico **generaliza** al Paciente; en el código, el
  rol MEDICO incluye los permisos del rol PACIENTE.
- **Consecuencias:** el control de acceso se hace por **permisos**, no por rol (ver D-011).

## D-006 · Git Flow
Fecha: 05/10 · It. 0 · Acta: 05/10
- **Decisión:** ramas `main` (solo versiones etiquetadas), `develop` (integración, rama por defecto),
  `feature/CUxx-<modulo>`, `bugfix/<descripcion>`, `release/x.y.z`, `hotfix/x.y.z`. `main` y `develop`
  protegidas (pull request + 1 aprobación). No se borran las ramas al fusionar.
- **Consecuencias:** cada CU = issue padre + sub-issue y rama por módulo + pull request «Closes #n».

## D-007 · Arquitectura cliente-servidor con MVC y capas
Fecha: 19/10 · It. 0 · Acta: 19/10
- **Decisión:** el servidor tiene capas **api** (contratos/interfaces) → **controlador** (control de acceso,
  auditoría) → **servicio** (reglas de negocio) → **dominio** (modelo) → **persistencia** (repositorios).
  El cliente tiene **vista** (consola) y **controlador** y solo conoce las interfaces del paquete `api`.
- **Alternativas:** Spring Boot + REST (sobreingeniería para el alcance y el tiempo del curso).
- **Motivo:** «el ingeniero diseña las interfaces; la IA implementa los contratos».
- **Consecuencias:** la comunicación cliente-servidor se **simula** en proceso: el cliente recibe una
  implementación de las interfaces `api`. Cambiar a REST solo exigiría un adaptador.

## D-008 · Persistencia en memoria
Fecha: 19/10 · It. 0 · Acta: 19/10
- **Decisión:** interfaz genérica `Repositorio<T, ID>` con implementación `RepositorioEnMemoria`.
- **Alternativas:** base de datos H2/MySQL con JDBC.
- **Motivo:** el enunciado permite simular; la interfaz deja la puerta abierta a una BD (DIP).
- **Consecuencias:** los datos se pierden al cerrar; no hay coste de infraestructura.

## D-009 · Sistemas externos simulados mediante interfaces
Fecha: 19/10 · It. 0 · Acta: 19/10
- **Decisión:** SSO, HCE, Admisión y RIS/PACS son interfaces (`ProveedorSso`, `SistemaHce`,
  `SistemaAdmision`, `SistemaPacs`) con adaptadores simulados en el paquete `integracion`.
- **Motivo:** permite probar sin los sistemas reales y sustituirlos por adaptadores DICOM/HL7/FHIR.

## D-010 · Tiempo inyectable
Fecha: 19/10 · It. 0
- **Decisión:** los servicios reciben un `java.time.Clock` en lugar de llamar a `LocalDateTime.now()`.
- **Motivo:** testabilidad (ISO 25010): las pruebas de recordatorios, esperas y seguimientos son deterministas.

## D-011 · Control de acceso por permisos
Fecha: 26/10 · It. 0 · Acta: 26/10
- **Decisión:** cada operación de la API exige un `Permiso`; cada `Rol` tiene un conjunto de permisos
  configurable (CU17). Los accesos a datos clínicos se registran en auditoría (CU18).
- **Motivo:** confidencialidad y responsabilidad (ISO 25010, seguridad).

## D-012 · Características de calidad priorizadas
Fecha: 26/10 · It. 0 · Acta: 26/10
- **Decisión:** se priorizan Seguridad (confidencialidad, responsabilidad), Idoneidad funcional (corrección),
  Mantenibilidad (testabilidad, modularidad) y Compatibilidad (interoperabilidad). Ver
  [Plan de calidad](docs/wiki/Plan-de-calidad.md).

## D-013 · Pruebas: JUnit 6, Surefire y JaCoCo con umbral del 75 %
Fecha: 09/11 · It. 0 · Acta: 09/11
- **Decisión:** JUnit Jupiter 6.1.3 (sin JUnit 4), Surefire 3.5.6, JaCoCo 0.8.16 con regla de cobertura de
  líneas ≥ 75 % por módulo. `haltOnFailure` se pone a `true` cuando se alcanza el umbral.
- **Alternativas:** Mockito para dobles de prueba; se descarta: los repositorios en memoria y las
  implementaciones simuladas de las interfaces ya sirven de *fakes*.

## D-014 · Planificación y estimación con agentes
Fecha: 26/10 · It. 0 · Acta: 26/10
- **Decisión:** los agentes reducen el esfuerzo de Implementación (−60 %) y Pruebas (−40 %), pero no el de
  Requisitos, Análisis y Diseño, que son responsabilidad del equipo. Se añade un tiempo de revisión
  humana por CU y un 15 % de contingencia por el sesgo de optimismo de la IA.
- **Consecuencias:** ver [Planificación](docs/wiki/Planificacion.md).

## D-015 · Cliente de consola agrupado por actor
Fecha: 26/10 · It. 0 · Acta: 26/10
- **Contexto:** una rama de cliente por CU daría 22 ramas casi vacías.
- **Decisión:** el cliente se desarrolla en ramas `feature/CUxx-cliente` agrupadas por actor (CU01 sesión,
  CU06 administrativo, CU09 médico, CU13 paciente, CU22 administrador).
- **Consecuencias:** el módulo cliente sube de versión menor con cada una de esas ramas.
