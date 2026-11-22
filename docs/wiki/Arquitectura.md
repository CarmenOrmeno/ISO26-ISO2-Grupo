# 🏛️ Arquitectura (línea base)

[🏠 Inicio](Home.md)

> Módulos Maven, capas del servidor, componentes por caso de uso y ciclo de vida de una solicitud.

---

Las reglas que deben respetar humanos y agentes están en [CONTEXT.md](../../CONTEXT.md). Decisiones
relacionadas: D-002, D-007, D-008, D-009, D-010, D-011.

## 📦 1. Módulos Maven

```
radiodiagnostico (pom padre, producto)
├── servidor (jar) ── modelo, servicios, controladores y contratos api
└── cliente  (jar) ── vista de consola + controladores; depende de servidor y solo usa su paquete api
```

## 🧅 2. Capas del servidor

```mermaid
flowchart LR
    subgraph CLI["💻 cliente"]
        V["Vista"] --> CC["Controlador cliente"]
    end
    subgraph SRV["🖥️ servidor"]
        API{{"«interface»<br/>ApiXxx"}}
        CTRL["ControladorXxx"]
        CA["ControlAcceso"]
        AUD["Auditor"]
        SRVI{{"«interface»<br/>ServicioXxx"}}
        IMPL["ServicioXxxImpl"]
        DOM["Dominio"]
        REP{{"«interface»<br/>Repositorio&lt;T,ID&gt;"}}
        MEM["RepositorioEnMemoria"]
        EXT{{"«interface»<br/>SistemaPacs / SistemaHce / …"}}
        SIM["Simulados"]
        SERV["Servidor<br/>(raíz de composición)"]
        CTRL -. implementa .-> API
        CTRL --> CA --> AUD
        CTRL --> SRVI
        IMPL -. implementa .-> SRVI
        IMPL --> DOM
        IMPL --> REP
        MEM -. implementa .-> REP
        IMPL --> EXT
        SIM -. implementa .-> EXT
        SERV -. crea .-> CTRL
    end
    CC --> API
```

Cada componente expone **dos** interfaces:
- `ServicioXxx` (contrato interno): lo usan otros componentes del servidor. Sin seguridad.
- `ApiXxx` (contrato externo): lo usa el cliente. Cada método recibe el *token* y exige un permiso.

## 🧩 3. Componentes (paquetes de `es.uclm.esi.iso2.servidor`)

| Paquete | Responsabilidad | CU |
|---|---|---|
| `comun` | Repositorio genérico, excepciones, validaciones | — |
| `seguridad` | Usuarios, sesiones, roles, permisos, control de acceso | CU01, CU17 |
| `integracion` | Interfaces de SSO, HCE, Admisión, RIS/PACS y simulados | CU01, CU09, CU19 |
| `pacientes` | Alta, modificación y baja de pacientes | CU02 |
| `catalogo` | Tipos de prueba, modalidades, prioridades y parámetros | CU03 |
| `solicitudes` | Solicitudes de prueba y su ciclo de vida | CU04, CU14 |
| `agendas` | Agendas diarias y huecos | CU05, CU14 |
| `citas` | Citas: asignar, reprogramar, cancelar, confirmar | CU06, CU07 |
| `realizacion` | Registro de realización e incidencias técnicas | CU08 |
| `informes` | Informes radiológicos e imágenes | CU09 |
| `seguimiento` | Pruebas de seguimiento periódicas | CU10, CU16 |
| `esperas` | Listas de espera, prioridades y huecos | CU11 |
| `notificaciones` | Notificaciones y recordatorios | CU12 |
| `historial` | Historial de pruebas del paciente | CU13 |
| `atencion` | Incidencias del paciente y encuestas | CU15 |
| `auditoria` | Registro y consulta de accesos | CU18 |
| `estadisticas` | Estadísticas del médico, informes operativos y globales | CU20–CU22 |
| `api` / `controlador` | Contratos con el cliente y su implementación | todos |

## 🔄 4. Ciclo de vida de una solicitud

```mermaid
stateDiagram-v2
    [*] --> SOLICITADA : CU04 / CU10
    SOLICITADA --> CITADA : CU06 asignar
    CITADA --> SOLICITADA : CU06/CU07 cancelar cita
    CITADA --> REALIZADA : CU08
    REALIZADA --> INFORMADA : CU09
    SOLICITADA --> CANCELADA
    INFORMADA --> [*]
```

---

[🏠 Inicio](Home.md) · [⬆️ Arriba](#)
