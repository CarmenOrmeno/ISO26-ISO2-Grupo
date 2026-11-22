# 🧪 Plan de pruebas

[🏠 Inicio](Home.md)

> Niveles, herramientas, técnicas de diseño de casos y criterios de las pruebas.

---

## 🎚️ 1. Alcance y niveles

| Nivel | Qué se prueba | Dónde | Cuándo |
|---|---|---|---|
| Unitarias | Entidades del dominio y servicios de cada componente | `servidor/src/test/java/.../<componente>` | En la iteración de cada CU |
| Integración (servidor) | Controladores `api` + control de acceso + servicios reales, montados con `Servidor` | `servidor/src/test/java/.../controlador` | En cada CU y en ItN |
| Integración (MySQL) | `RepositorioMySql` cumple el contrato de `Repositorio`; el servidor conserva sus datos entre arranques | `servidor/src/test/java/.../persistencia`, `ServidorMySqlTest` | En cada PR (el CI levanta MySQL 8.0) |
| Sistema (cliente) | Controladores del cliente contra el servidor de demostración: todas las acciones de cada rol están permitidas por la política de permisos; flujos de consola con entrada simulada | `cliente/src/test/java` | En las iteraciones de cliente y en ItN |
| Vista JavaFX | Pantallas FXML: carga, inicio de sesión, formularios generados y resultados. Necesita pantalla, así que se prueba a mano (prueba de humo) y queda fuera de JaCoCo | `cliente/.../vista/fx` | Al cambiar la vista y en ItN |

## 🧰 2. Herramientas (D-013)

- **JUnit Jupiter 6.1.3** (`org.junit.jupiter.api`): `@Test`, `@DisplayName`, `@Nested`, `@BeforeEach`,
  `@ParameterizedTest` con `@CsvSource`/`@ValueSource`/`@EnumSource`, `assertThrows`, `assertAll`.
- **Surefire 3.5.6**: ejecuta las pruebas en `mvn test` y genera el informe.
- **JaCoCo 0.8.16**: cobertura de sentencias y ramas; regla `check` ≥ 75 % de líneas por módulo.
- Dobles de prueba: los *fakes* del propio diseño (`RepositorioEnMemoria`, `SsoSimulado`, `PacsSimulado`…)
  y un `Clock.fixed(...)` para el tiempo. Sin Mockito.

## 🧩 3. Técnicas de diseño de casos (T.04.04 y T.04.05)

Cada especificación de CU (`docs/casos-de-uso/CUxx.md`) incluye su tabla de diseño de pruebas con:
- **Clases de equivalencia** válidas (CEV) e inválidas (CEI) de cada entrada.
- **Valores límite** (VL) de los rangos (duración de la prueba, puntuación de la encuesta, horarios, fechas).
- **Conjetura de errores** (CE): nulos, cadenas vacías, operaciones repetidas, estados ilegales, acceso
  con el rol equivocado, recursos de otro paciente.
- **Criterio de combinación**: *each use* (cada valor de cada clase al menos una vez) para las entradas
  independientes; las combinaciones de **estado × operación** de las entidades con ciclo de vida
  (solicitud, cita) se cubren todas (tabla de transiciones válidas e inválidas).

## 🚥 4. Criterios de entrada, salida y suspensión

- Entrada: el CU tiene especificación aprobada e interfaces definidas.
- Salida: 100 % de las pruebas pasan, cobertura de líneas del módulo ≥ 75 %, PR con CI en verde.
- Suspensión: si el CI está en rojo en `develop`, nadie fusiona nada hasta arreglarlo (rama `bugfix`).

## 🐞 5. Gestión de fallos

Cada fallo encontrado después de fusionar se registra como issue y se corrige en su propia rama
`bugfix/<descripcion>` con su PR, para que en GitHub se vea el antes y el después. Los fallos encontrados
durante la propia iteración se corrigen en la rama `feature` con commits `fix(CUxx): …`.

## 📊 6. Resultados

Ejecución final de la iteración de transición (ItN, 22/11/2026): `mvn verify site site:stage` sobre la
versión 1.0.1-SNAPSHOT (servidor 1.12.1, cliente 2.0.1), con MySQL 8.0 (`RADIO_DB_TEST_URL`). Los informes HTML
están en `target/staging` (`surefire.html` y `servidor/jacoco`, `cliente/jacoco`); el CI repite la misma
ejecución en cada PR con el servicio MySQL.

### 6.1 Pruebas por nivel (Surefire)

| Nivel | Clases | Pruebas | Fallos | Omitidas | Tiempo |
|---|---:|---:|---:|---:|---:|
| Unitarias (servidor): dominio y servicios | 29 | 262 | 0 | 0 | 2,6 s |
| Integración (servidor): controladores + control de acceso + servicios reales | 21 | 167 | 0 | 0 | 10,4 s |
| Integración (MySQL): `RepositorioMySql` y reinicio del servidor completo | 4 | 24 | 0 | 0 | 33,5 s |
| Unitarias (cliente): campos, valores, resultados, sesión y arranque | 5 | 25 | 0 | 0 | 0,7 s |
| Sistema (cliente): acciones de los 4 roles, consola y flujo completo | 3 | 15 | 0 | 0 | 1,6 s |
| **Total** | **62** | **493** | **0** | **0** | |

Pruebas de integración de la ItN:
- `FlujoCompletoTest`: el ciclo de vida completo de una prueba con los cuatro roles a través del cliente
  (CU04 → CU05 → CU11 → CU07 → CU12 → CU08 → CU09 → CU13 → CU15 → CU19 → CU18 → CU20 → CU21 → CU22).
- `ServidorMySqlTest.cicloCompletoTrasReinicio`: el mismo ciclo contra MySQL; tras reiniciar se conservan los
  datos y la HCE no recibe duplicados.
- `CatalogoAccionesTest.accionesCoherentes`: cada acción de cada rol lee bien sus campos y está permitida por
  la política de permisos del servidor.

### 6.2 Cobertura (JaCoCo)

| Módulo | Líneas | Ramas | Umbral (líneas) |
|---|---:|---:|---:|
| servidor | 98,7 % | 93,6 % | 75 % ✅ |
| cliente (sin la vista JavaFX) | 97,2 % | 96,4 % | 75 % ✅ |

| Paquete del servidor | Líneas | Ramas | Paquete del servidor | Líneas | Ramas |
|---|---:|---:|---|---:|---:|
| `agendas` | 99,1 % | 93,8 % | `integracion` | 100 % | 100 % |
| `atencion` | 96,9 % | 86,7 % | `interoperabilidad` | 95,4 % | 62,5 % |
| `auditoria` | 100 % | 87,5 % | `notificaciones` | 97,6 % | 100 % |
| `catalogo` | 100 % | 93,3 % | `pacientes` | 100 % | 100 % |
| `citas` | 100 % | 92,4 % | `persistencia` | 88,4 % | 94,4 % |
| `comun` | 100 % | 100 % | `realizacion` | 97,6 % | 83,3 % |
| `controlador` | 100 % | 100 % | `seguimiento` | 98,1 % | 100 % |
| `esperas` | 100 % | 92,9 % | `seguridad` | 100 % | 94,6 % |
| `estadisticas` | 100 % | 94,1 % | `solicitudes` | 100 % | 100 % |
| `historial` | 100 % | 93,8 % | `informes` | 95,0 % | 100 % |

Lo que no se cubre y por qué:
- **Vista JavaFX** (`cliente.vista.fx`): necesita pantalla. Se excluye de JaCoCo (D-017) y se prueba a mano con
  una prueba de humo: carga de `login.fxml` y `principal.fxml`, inicio de sesión, consultas sin campos y
  formularios con texto, desplegable y fecha, y arranque desde el jar ejecutable.
- **`interoperabilidad` (ramas 62,5 %)**: quedan sin probar combinaciones de fallo poco probables (un paciente
  de admisión que deja de ser válido entre la lectura y el alta, o una caída de la HCE a mitad de un envío
  con varios informes pendientes). Los casos principales de caída y recuperación sí están probados.
- **`persistencia` (88,4 % de líneas)**: los errores de conexión de JDBC que no se pueden provocar sin parar
  MySQL a mitad de una prueba.

### 6.3 Fallos encontrados

Durante las iteraciones se encontraron y corrigieron fallos en las propias pruebas (valores esperados o datos
de preparación incorrectos); cada uno está documentado en la sección «Fallos encontrados» de su CU.

En la ItN se encontró un problema de usabilidad: si MySQL está configurado (`RADIO_DB_URL`) pero no responde,
el cliente terminaba con un volcado de pila. Ahora muestra la causa y cómo seguir y termina con código 1
(`fix(ItN)`, prueba `MainTest`). Tras la revisión de calidad (PR #109) se añadió la detección de
acciones del cliente mal construidas, comprobada con una prueba de mutación.

---

[🏠 Inicio](Home.md) · [⬆️ Arriba](#)
