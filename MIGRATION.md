# Migration Log

## Estado inicial

### Versiones actuales
| Componente   | Versión actual            | Versión objetivo |
|--------------|---------------------------|-----------------|
| Java         | 21                        | 21              |
| Gradle       | 8.5 (eureka/gateway/basic-service) / 8.4 (authentication-service) | 8.x |
| Spring Boot  | 3.4.3                     | 3.4.x           |
| Spring Cloud | 2024.0.0                  | 2024.0.x        |

> **Nota:** El proyecto ya está en las versiones objetivo para Java, Spring Boot y Spring Cloud.
> La Iteración 0 documenta el baseline tal como está.

---

### Dependencias clave por módulo

#### eureka
| Dependencia | Versión |
|---|---|
| spring-boot-starter-actuator | (BOM 3.4.3) |
| spring-cloud-starter-netflix-eureka-server | (BOM 2024.0.0) |
| micrometer-tracing-bridge-brave | (BOM 3.4.3) |
| zipkin-reporter-brave | (BOM 3.4.3) |

#### gateway
| Dependencia | Versión |
|---|---|
| spring-boot-starter-actuator | (BOM 3.4.3) |
| spring-cloud-starter-gateway | (BOM 2024.0.0) |
| spring-cloud-starter-netflix-eureka-client | (BOM 2024.0.0) |
| io.jsonwebtoken:jjwt | **0.12.5** (versión directa, fuera del BOM) |
| micrometer-tracing-bridge-brave | (BOM 3.4.3) |
| zipkin-reporter-brave | (BOM 3.4.3) |

#### basic-service
| Dependencia | Versión |
|---|---|
| spring-boot-starter-web | (BOM 3.4.3) |
| spring-cloud-starter-netflix-eureka-client | (BOM 2024.0.0) |
| spring-boot-starter-actuator | (BOM 3.4.3) |
| micrometer-tracing-bridge-brave | (BOM 3.4.3) |
| zipkin-reporter-brave | (BOM 3.4.3) |

#### authentication-service
| Dependencia | Versión |
|---|---|
| spring-boot-starter-web | (BOM 3.4.3) |
| spring-cloud-starter-netflix-eureka-client | (BOM 2024.0.0) |
| spring-boot-starter-actuator | (BOM 3.4.3) |
| spring-cloud-starter-openfeign | (BOM 2024.0.0) |
| spring-boot-starter-data-jpa | (BOM 3.4.3) |
| micrometer-tracing-bridge-brave | (BOM 3.4.3) |
| zipkin-reporter-brave | (BOM 3.4.3) |
| com.h2database:h2 | (BOM 3.4.3) — runtime only |
| spring-security-crypto | **6.2.2** (versión directa, fuera del BOM) |
| io.jsonwebtoken:jjwt | **0.12.5** (versión directa, fuera del BOM) |
| **javax.xml.bind:jaxb-api** | **2.3.0** (versión directa — namespace javax, RIESGO ALTO) |

---

### Propiedades de configuración destacadas

| Módulo | Propiedad | Observación |
|---|---|---|
| gateway | `jwt.secret` (plain text en application.yml) | Secreto JWT hardcoded — no es un problema de migración pero es una deuda de seguridad |
| authentication-service | `jwt.secret` (plain text en application.yml) | Idem |
| authentication-service | `management.tracing.propagation.type: B3` | Explícito; consistente con Brave |
| todos | `management.zipkin.tracing.endpoint` | Formato correcto para Micrometer/Spring Boot 3.x |
| todos | `logging.pattern.level` con MDC de traceId/spanId | Compatible con Micrometer Tracing |

---

### Riesgos identificados

| # | Módulo | Riesgo | Severidad |
|---|---|---|---|
| R1 | authentication-service | `javax.xml.bind:jaxb-api:2.3.0` — usa el namespace `javax.*` eliminado de la JDK en Java 11 y reemplazado por `jakarta.*` en Jakarta EE 9. Spring Boot 3.x requiere Jakarta EE 9+. Esta dependencia puede causar `ClassNotFoundException` en runtime si algún código la usa activamente. | **ALTA** |
| R2 | gateway | `@RefreshScope` en `AuthenticationFilter` sin Spring Cloud Config configurado — puede generar un bean proxy lento o fallos de inicialización si no hay un Config Server disponible. | MEDIA |
| R3 | gateway | Bloque `repositories` duplicado en `build.gradle.kts` (mavenCentral declarado dos veces). No rompe el build pero es un defecto de configuración. | BAJA |
| R4 | authentication-service | Gradle wrapper en versión 8.4 mientras los demás módulos usan 8.5. Inconsistencia menor. | BAJA |
| R5 | gateway / authentication-service | `io.jsonwebtoken:jjwt:0.12.5` declarado con versión directa fuera del BOM. JJWT 0.12.x ya usa la API `jakarta.*`, por lo que es compatible con Spring Boot 3.x, pero debe mantenerse actualizado manualmente. | BAJA |
| R6 | authentication-service | `spring-security-crypto:6.2.2` declarado con versión directa fuera del BOM. El BOM de Spring Boot 3.4.3 gestiona Spring Security — la versión directa puede entrar en conflicto con la versión del BOM. | MEDIA |

---

## Resultado del build inicial

Todos los módulos compilan exitosamente con `./gradlew compileJava`.

| Módulo | Resultado |
|---|---|
| eureka | ✅ BUILD SUCCESSFUL (compileJava UP-TO-DATE) |
| gateway | ✅ BUILD SUCCESSFUL (compileJava UP-TO-DATE) |
| basic-service | ✅ BUILD SUCCESSFUL (compileJava UP-TO-DATE) |
| authentication-service | ✅ BUILD SUCCESSFUL (compileJava UP-TO-DATE) |

> Notas: Los builds emiten warnings de JVM sobre `java.lang.System::load` (native-platform en Gradle 8.x con Java 21).
> Estos son warnings del tooling de Gradle, no de la aplicación, y no impiden la compilación.

---

## Iteraciones

### Iteración 0 — Auditoría ✅
- Fecha: 2026-03-30
- Cambios: ninguno (solo lectura y documentación)
- Hallazgo clave: el proyecto ya está en las versiones objetivo (Java 21, Spring Boot 3.4.3, Spring Cloud 2024.0.0, Gradle 8.x). Las iteraciones siguientes se enfocarán en resolver los riesgos identificados (especialmente R1 y R6) y validar el comportamiento en runtime.

### Iteración 1 — Actualización de versiones en build files ⚠️
- Fecha: 2026-03-30
- Spring Boot: 3.4.3 → 4.0.5
- Spring Cloud: 2024.0.0 → 2025.1.1
- Java toolchain: 21 → 25
- Gradle wrapper: 8.5 → 8.14 (requisito mínimo impuesto por Spring Boot 4.0.x plugin; 8.5 falla al cargar el plugin)
- spring-security-crypto: versión explícita `6.2.2` eliminada — ahora gestionada por BOM → resuelve a `7.0.4`
- Repositorio duplicado en gateway: eliminado (`mavenCentral()` doble → uno solo)
- Gateway artifact renombrado: `spring-cloud-starter-gateway` → `spring-cloud-gateway-server-webflux` (el starter fue retirado en Spring Cloud 2025.1.x)

#### Resultado de compilación por módulo
| Módulo                 | Resultado | Detalle                                                           |
|------------------------|-----------|-------------------------------------------------------------------|
| eureka                 | ✅        | BUILD SUCCESSFUL                                                  |
| basic-service          | ✅        | BUILD SUCCESSFUL                                                  |
| authentication-service | ✅        | BUILD SUCCESSFUL (javax.xml.bind:jaxb-api:2.3.0 resuelve pero es riesgo R1 pendiente) |
| gateway                | ❌        | 1 error de compilación — ver detalle abajo                        |

#### Errores de compilación identificados (para Iteración 2)

**gateway — `AuthenticationFilter.java:67`**
```
error: cannot find symbol
    return !request.getHeaders().containsKey("Authorization");
                                ^
  symbol:   method containsKey(String)
  location: class HttpHeaders
```
`HttpHeaders.containsKey(String)` fue eliminado en Spring Framework 7.0.
`HttpHeaders` ya no implementa `Map<String, List<String>>` directamente.
El reemplazo es `HttpHeaders.hasHeader(String)` (o `containsHeader(String)` según contexto).

#### Riesgos pendientes para iteraciones siguientes
- **R1** (ALTA): `javax.xml.bind:jaxb-api:2.3.0` en authentication-service — namespace `javax.*` incompatible con Jakarta EE 9+; compiló pero fallará en runtime. Debe eliminarse o reemplazarse por `jakarta.xml.bind:jakarta.xml.bind-api`.
- **gateway-R-new** (ALTA): `HttpHeaders.containsKey` → `HttpHeaders.hasHeader` en `AuthenticationFilter.java:67`.
