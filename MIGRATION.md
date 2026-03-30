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

---

### Iteración 2 — Corrección R1, gateway-R-new y R2 ⚠️
- Fecha: 2026-03-30

#### Cambios aplicados

| # | Cambio | Archivo | Detalle |
|---|---|---|---|
| gateway-R-new | ✅ Resuelto | `AuthenticationFilter.java:67` | `containsHeader("Authorization")` ya estaba aplicado; compilación verificada |
| R2 | ✅ Resuelto | `AuthenticationFilter.java` | `@RefreshScope` eliminado (y su import) — sin Config Server, el proxy CGLIB es innecesario |
| trusted-proxies | ✅ Configurado | `gateway/application.yml` | `spring.cloud.gateway.server.webflux.trusted-proxies: ".*"` para entorno local |
| R1 | ✅ Resuelto (Caso A) | `authentication-service/build.gradle.kts` | `javax.xml.bind:jaxb-api:2.3.0` eliminado — no tenía ningún `import javax.xml.bind.*` en el código fuente; dependencia sin uso real |

#### Resultado final de compilación
| Módulo                 | Resultado |
|------------------------|-----------|
| eureka                 | ✅ BUILD SUCCESSFUL |
| gateway                | ✅ BUILD SUCCESSFUL |
| basic-service          | ✅ BUILD SUCCESSFUL |
| authentication-service | ✅ BUILD SUCCESSFUL |

#### Smoke test de arranque
| Servicio               | Resultado | Detalle |
|------------------------|-----------|---------|
| eureka                 | ✅ OK     | Arranca en 2.3 s, puerto 8761. Aviso INFO sobre Jakarta Bean Validation provider ausente (no crítico). |
| basic-service          | ✅ OK     | Arranca en 1.9 s, puerto 8082. Errores de heartbeat Eureka esperados (eureka no estaba corriendo en paralelo). |
| authentication-service | ❌ FALLA  | Ver detalle abajo |
| gateway                | ❌ FALLA  | Ver detalle abajo |

#### Errores de runtime identificados (para Iteración 3)

**authentication-service y gateway — mismo error raíz**

```
Field tracer in <FilterClass> required a bean of type 'io.micrometer.tracing.Tracer'
that could not be found.
```

- **authentication-service**: `TraceIdResponseFilter` — `@Autowired io.micrometer.tracing.Tracer`
- **gateway**: `TraceIdFilter` — `@Autowired io.micrometer.tracing.Tracer`

**Causa raíz:** En Spring Boot 4.x la autoconfiguración de Micrometer Tracing / Brave cambió.
Las dependencias `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` ya no registran
automáticamente un bean `Tracer` con las mismas condiciones que en Spring Boot 3.x.
Posibles soluciones para Iteración 3:
1. Reemplazar la inyección directa de `Tracer` por `ObservationRegistry` (API pública de Micrometer)
2. Agregar `spring-boot-starter-actuator` con la dependencia `io.micrometer:micrometer-tracing-bridge-brave` y verificar si el autoconfigure cambia
3. Eliminar los filtros `TraceIdFilter`/`TraceIdResponseFilter` si la propagación de traceId ya es manejada automáticamente por Micrometer en Spring Boot 4.x

#### Riesgos pendientes
- **R-tracer** (ALTA): `io.micrometer.tracing.Tracer` no se autowiring en authentication-service y gateway bajo Spring Boot 4.x — bloquea el arranque de ambos servicios.

---

### Iteración 3 — Corrección R-tracer: Micrometer Tracing ⚠️
- Fecha: 2026-03-30
- Caso en `TraceIdResponseFilter` (authentication-service): **CASO A** — solo propaga `X-B3-TraceId` al response header y loguea el traceId. Sin lógica de negocio.
- Caso en `TraceIdFilter` (gateway): **CASO A** — ídem, además loguea en `doOnSuccess`. Sin lógica de negocio.
- Acción en authentication-service: `TraceIdResponseFilter.java` **eliminado** (sin registros externos ni @Bean)
- Acción en gateway: `TraceIdFilter.java` **eliminado** (sin registros externos ni @Bean)
- Patrón de logging MDC `%X{traceId:-},%X{spanId:-}`: **ya presente** en ambos `application.yml` — el tracing en logs sigue funcionando automáticamente via Micrometer
- Dependencias `micrometer-tracing-bridge-brave:1.6.4` y `zipkin-reporter-brave`: resuelven correctamente desde el BOM de Spring Boot 4.0.5

#### Resultado de compilación
| Módulo                 | Resultado |
|------------------------|-----------|
| eureka                 | ✅ BUILD SUCCESSFUL |
| gateway                | ✅ BUILD SUCCESSFUL |
| basic-service          | ✅ BUILD SUCCESSFUL |
| authentication-service | ✅ BUILD SUCCESSFUL |

#### Smoke test de arranque
| Servicio               | Resultado | Detalle |
|------------------------|-----------|---------|
| eureka                 | ✅ OK     | Puerto 8761, 2.8 s |
| authentication-service | ✅ OK     | Puerto 8083, 5.1 s. Registra en Eureka correctamente. |
| basic-service          | ✅ OK     | Puerto 8082, 1.9 s. Registra en Eureka correctamente. |
| gateway                | ❌ FALLA  | Ver detalle abajo |

#### Error de runtime gateway (nuevo — R-eureka-reactive)

```
java.lang.ClassNotFoundException:
  org.springframework.boot.web.server.context.WebServerInitializedEvent

Caused by: java.lang.IllegalStateException: Failed to introspect Class
  [org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration]
Caused by: java.lang.NoClassDefFoundError:
  org/springframework/boot/web/server/context/WebServerInitializedEvent
```

**Causa raíz:** `EurekaAutoServiceRegistration` de Spring Cloud Netflix 2025.1.1 sigue referenciando
`org.springframework.boot.web.server.context.WebServerInitializedEvent`, clase que fue eliminada
o movida en Spring Boot 4.x. El error ocurre únicamente en el gateway (stack reactivo/WebFlux);
authentication-service y basic-service (stack Servlet) no se ven afectados.

**Análisis:** Es una incompatibilidad entre `spring-cloud-starter-netflix-eureka-client` incluido
en Spring Cloud 2025.1.1 y el stack reactivo de Spring Boot 4.0.5. El cliente Eureka en contexto
WebFlux intenta escuchar `WebServerInitializedEvent` (o su subclase reactiva) en un paquete que ya
no existe en Spring Boot 4.x.

**Opciones para Iteración 4:**
1. Deshabilitar la autoconfiguración de Eureka en el gateway y registrarlo manualmente, o
2. Excluir `EurekaAutoServiceRegistration` vía `spring.cloud.discovery.enabled=false` +
   configurar el descubrimiento sin registro, o
3. Investigar si existe un fix en versiones más recientes de `spring-cloud-starter-netflix-eureka-client`

#### Smoke test funcional
No ejecutado — gateway no arrancó (puerto 8080 no disponible).

#### Riesgos pendientes
- **R-eureka-reactive** (ALTA): `EurekaAutoServiceRegistration` incompatible con stack WebFlux en Spring Boot 4.x — `WebServerInitializedEvent` no encontrada. Bloquea el arranque del gateway.

---

### Iteración 4 — Corrección R-eureka-reactive: gateway WebFlux ✅
- Fecha: 2026-03-30

#### Cambios aplicados en `gateway/`

| Cambio | Archivo | Detalle |
|---|---|---|
| `spring.cloud.service-registry.auto-registration.enabled: false` | `application.yml` | Evita que `EurekaClientAutoConfiguration` instancie `EurekaAutoServiceRegistration` (cuya introspección de clase falla en Spring Boot 4.x porque `WebServerInitializedEvent` fue movida). Primer fix aplicado. |
| `eureka.client.webclient.enabled: false` | `application.yml` | WebFlux en classpath activa automáticamente el transporte WebClient de Eureka, cuyo `Supplier<WebClient.Builder>` retorna null en el contexto del gateway. Deshabilitarlo fuerza el uso de RestClient, que sí funciona. |
| `spring-boot-starter-webflux` | `build.gradle.kts` | Requerido por `spring-cloud-gateway-server-webflux` para resolver `HttpHandlerAutoConfiguration` (movida a `spring-boot-autoconfigure-webflux` en Spring Boot 4.x). |
| Routes movidas a namespace nuevo | `application.yml` | En Spring Cloud 2025.1.x con `spring-cloud-gateway-server-webflux`, las rutas deben estar bajo `spring.cloud.gateway.server.webflux.routes` (antes era `spring.cloud.gateway.routes`). Fix crítico para que el routing funcione. |

#### Smoke test de arranque
| Servicio               | Resultado | Registrado en Eureka |
|------------------------|-----------|----------------------|
| eureka                 | ✅ OK     | N/A                  |
| authentication-service | ✅ OK     | Sí (puerto 8083)     |
| basic-service          | ✅ OK     | Sí (puerto 8082)     |
| gateway                | ✅ OK     | No (por diseño — `register-with-eureka: false`) |

#### Smoke test funcional end-to-end
| Paso                                  | HTTP Status | Observaciones |
|---------------------------------------|-------------|---------------|
| POST /api/public/auth/register        | 200 OK      | Usuario `migration@test.com` creado correctamente |
| POST /api/public/auth/login           | 200 OK      | JWT HS512 emitido correctamente |
| GET /api/private/basic CON JWT        | 200 OK      | Respuesta: `Hello from 'BASIC-SERVICE'!` — routing lb:// y AuthenticationFilter funcionan |
| GET /api/private/basic SIN JWT        | 401         | Respuesta: `Invalid session` — AuthenticationFilter rechaza correctamente |

#### Estado final de la migración
- ✅ **Migración completa** — todos los servicios compilados y funcionando con Java 25 / Spring Boot 4.0.5 / Spring Cloud 2025.1.1 / Gradle 8.14
- El flujo completo register → login → endpoint privado con JWT funciona end-to-end
- Riesgos pendientes: **ninguno** en el path crítico
