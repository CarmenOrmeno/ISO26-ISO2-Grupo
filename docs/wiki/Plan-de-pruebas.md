# Plan de pruebas

## 1. Alcance y niveles

| Nivel | Qué se prueba | Dónde | Cuándo |
|---|---|---|---|
| Unitarias | Entidades del dominio y servicios de cada componente | `servidor/src/test/java/.../<componente>` | En la iteración de cada CU |
| Integración (servidor) | Controladores `api` + control de acceso + servicios reales, montados con `Servidor` | `servidor/src/test/java/.../controlador` | En cada CU y en ItN |
| Sistema (cliente) | Flujos completos de consola con entrada simulada | `cliente/src/test/java` | En las iteraciones de cliente y en ItN |

## 2. Herramientas (D-013)

- **JUnit Jupiter 6.1.3** (`org.junit.jupiter.api`): `@Test`, `@DisplayName`, `@Nested`, `@BeforeEach`,
  `@ParameterizedTest` con `@CsvSource`/`@ValueSource`/`@EnumSource`, `assertThrows`, `assertAll`.
- **Surefire 3.5.6**: ejecuta las pruebas en `mvn test` y genera el informe.
- **JaCoCo 0.8.16**: cobertura de sentencias y ramas; regla `check` ≥ 75 % de líneas por módulo.
- Dobles de prueba: los *fakes* del propio diseño (`RepositorioEnMemoria`, `SsoSimulado`, `PacsSimulado`…)
  y un `Clock.fixed(...)` para el tiempo. Sin Mockito.

## 3. Técnicas de diseño de casos (T.04.04 y T.04.05)

Cada especificación de CU (`docs/casos-de-uso/CUxx.md`) incluye su tabla de diseño de pruebas con:
- **Clases de equivalencia** válidas (CEV) e inválidas (CEI) de cada entrada.
- **Valores límite** (VL) de los rangos (duración de la prueba, puntuación de la encuesta, horarios, fechas).
- **Conjetura de errores** (CE): nulos, cadenas vacías, operaciones repetidas, estados ilegales, acceso
  con el rol equivocado, recursos de otro paciente.
- **Criterio de combinación**: *each use* (cada valor de cada clase al menos una vez) para las entradas
  independientes; las combinaciones de **estado × operación** de las entidades con ciclo de vida
  (solicitud, cita) se cubren todas (tabla de transiciones válidas e inválidas).

## 4. Criterios de entrada, salida y suspensión

- Entrada: el CU tiene especificación aprobada e interfaces definidas.
- Salida: 100 % de las pruebas pasan, cobertura de líneas del módulo ≥ 75 %, PR con CI en verde.
- Suspensión: si el CI está en rojo en `develop`, nadie fusiona nada hasta arreglarlo (rama `bugfix`).

## 5. Gestión de fallos

Cada fallo encontrado después de fusionar se registra como issue y se corrige en su propia rama
`bugfix/<descripcion>` con su PR, para que en GitHub se vea el antes y el después. Los fallos encontrados
durante la propia iteración se corrigen en la rama `feature` con commits `fix(CUxx): …`.

## 6. Resultados

_Se completa al final con los resultados de `mvn verify site` (Surefire y JaCoCo)._
