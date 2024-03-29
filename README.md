# Spring Cloud Basic Example

This repository contains a basic example of a microservices architecture using Spring Cloud.

## How to run it locally

In order to run this services locally you will need to have installed Java 17. Gradle is not required.

There is a [script](scripts/run.sh) that will help you to start the services. You can run it using the following command:

```shell
./scripts/start.sh <service> <port>
```

When:

- `service` is the directory name of the service you want to start.
- `port` is the port where the service will be running. This is optional, but it's recommended to avoid port conflicts. Don't set the port for the Eureka service. By default, Eureka is running on port 8761, and that port is used in the configuration of the other services.

## Architecture

This is an attempt to create a microservices architecture using Spring Cloud. The idea is to get familiar with the basic concepts of microservices and how to implement them using Spring Cloud. Not all the concepts are implemented, we omitted the configuration server and the circuit breaker.

It consists in three types of services:

### Simple Spring Boot Services

In this example, there are two types of services: the basic service and the authentication service. The basic service only contains some basic logic to support REST calls to interact with other services and clients. The authentication service has the responsibility to authenticate users and generate JWT tokens. The sessions are handled statelessly in order to scale horizontally.

They are simple services with three tiers: Controller, to handle REST API requests. The business layer consists of the handlers packages. The idea is to implement handlers using the CQRS pattern. In this implementation, a handler will have a single responsibility instead of the traditional CRUD pattern. The persistence layer is implemented using JPA and an H2 database.

#### Configuration of a service

The `build.gradle.kts` file needs to have the following dependencies:

```kotlin
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
```

It's important to annotate in a configuration file or in the main class the following annotation: `@EnableDiscoveryClient`. This will allow the service to register itself in the Eureka server.

The last configuration is to set the eureka values to the application configuration file. Pointing out all the eureka server instances comma separated.

```yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Gateway

This is the entry point to the system. It's used to route the requests to the corresponding service. It also handles authorizations.

![ ](https://www.plantuml.com/plantuml/png/RP31YiCW48RlynH3xWRYSLb2RliS4nrsPLC3SQoKqdUlJJGqRK_ZVFxVE3W4eKNYQG8_MyRcXuGJNHgaWB_RURlXkF0_bHH5-MClpf2usQl0ozaP_aBdypXlk5lzWhiYZHISUS_Y8QmHB0dbBACTk-T60vXfZXPJIdY56-IgL5-tB1LLNtUjx-Dtsxv2VO2xc-LgVO9wdvdXOxtArrR3mDxclxm3)

#### Gateway interaction

![](httpS://www.plantuml.com/plantuml/png/XP7DJeGm4CVlynJ_x6Nrq3M0UY26xCsxRp1RPaqZjh4j46DyT_ccqOCd9iFl_y5m3f5zRDyRiguM79uvIi-V1t30wppFRwJryiOBTU5Wj0hYEwEbXB63YzM2RD8j-np_q7bA5o3IZMjhz7sFB_gcuQbKjPGeaPtm8bfC9_3oqKg8E4xETrw2QxKSQM6XghajIkIZQgQlpeCnIVOEtB5f-D88BP8j4IMAKHozaI6JxSx-ChbPX-nshw2gxGubxlSMqI5fxRNGYa6bsGz9je6zkl4Um9DIs4xoc383idx3wRHw_jaKH6UnzV33Vm00)

[Gateway service](https://spring.io/projects/spring-cloud-gateway) is the entry point to the system. It's used to route the requests to the corresponding service. It also handles authorization.

It contains a [filter](gateway/src/main/java/dev/leocamacho/gateway/config/AuthenticationFilter.java) to intercept all the requests and validate the JWT token.It also routes the requests to the corresponding service.This filter is executed before the request is routed to the service.It validates the requested [path](gateway/src/main/java/dev/leocamacho/gateway/config/RouterValidator.java), and if it's a public path, it allows the request to be routed to the service. If it's a private path, it validates the JWT token and then routes the request to the service.

#### Gateway configuration

Similar to other service, the `build.gradle.kts` file needs to have the following dependencies:

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-gateway")
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
```

In this case there is a new dependency, `spring-cloud-starter-gateway`. This is the library that contains the gateway functionality.

Also, it's important to annotate in a configuration file or in the main class the following annotation: `@EnableDiscoveryClient`. This will allow the service to register itself in the Eureka server.

The configuration of eureka client in application.yml is the same as the other services.

##### Handling REST API requests

As it name suggests, the gateway is the entry point to the system. It's used to route the requests to the corresponding service. It also handles authorizations. To achieve this the gateway has an special configuration to redirect the requests to the corresponding service.

````yml
spring:
  cloud:
    gateway:
      routes:
        - id: basic-service
          uri: lb://basic-service
          predicates:
            - Path=/api/public/basic/**, /api/private/basic/**
        - id: authentication-service
          uri: lb://authentication-service
          predicates:
            - Path=/api/public/auth/**, /api/private/auth/**````
````

In this example, routes are manually defined to expose a standard REST API definition for all services. Each service has a private and a public path defined. The public path is used to make the service accessible to the world, while the private path is used to expose services that require authentication. The logic for this is managed by the gateway in the
[Authentication filter](/gateway/src/main/java/dev/leocamacho/gateway/config/AuthenticationFilter.java).

### Eureka Service

This is the service registry. It is used to register all the services and to discover them. There is no special code here; it is just a Spring Boot application with the Eureka Server dependency.

It is used to register all the services and discover them. Every service registers itself in the Eureka server. Additionally, it is used to discover other services. For example, the gateway service uses Eureka to discover the basic and authentication services. There is a web console to see the registered services. It is available at `http://localhost:8761/`.

Here is an example step by step how multiple instance of the same service are registered in Eureka:

![ ](
https://www.plantuml.com/plantuml/png/HS-nxe8m40RmlK_n1OR_nF2d2WbnO11M4ts22XUzg4VIAmu-lKr1tPhs-_M-QpKu7-R7tSWq4cZsT7F8hQtjjbxkihDJ7Web_-I1q80b8ed7_mnEaPwHTzsch2hpXmMvEKoUNoxm91_xeoWJZQ7hDuMyx_UbxOrPM-oAOlcymK5mMkUdmWfM7Ed26iWZsYuHOrgICMxp1W00)

After the service is register, Eureka will know the existence of the `instance1`.

![ ](
http://www.plantuml.com/plantuml/png/ZS-nhe8m50RWlK_n1uOxCTpsNaY80uEmckWJL3ZHHYX9J-lWqzi0ui2WMzFpVoV_gTXhvyFG2pjs10gERXgfKeNwB-Q_vApPj1cJkvkMKCBOY6ny_YRGZ6cCjAx_P9v_8l9Dv6f-C2iT_EZCGtipsXVUs1YwhYcaJRENgX5YQRxBidbxYOF5QIam7SamZ2DT37joYBQUo4RAxwceo7PXw9y0)

If another instance of the same service is registered, Eureka will know the existence of the `instance2`.

![ ](
https://www.plantuml.com/plantuml/png/dS-npe8m40VmlKznWPdXQpyX8GuCmcgYJr3XeXUX9BtMmQUtAQXH1erRqzxl_lzMxBHrVkY56Ji4z1RF6bAb2hKgfhlae9dQZD5Ug4KeWKn09Xp_4j0CyHdIklwNUVw9o2UHRqPxDrgwQaUZkTD5M1WwFQOWRPgDL8qGS-zrbasxIzWQsC_nyOiCnB338x532y7MaIoa_8M-rlf-WwZGT7xeRm00)

The same process is repeated for the `instance1` of the authentication service. During this process, each Eureka client will notify the Eureka server about its existence using a heartbeat mechanism. If the Eureka server doesn't receive a heartbeat from a service instance for a certain amount of time, it will remove the instance from the registry.

Similarly, depending on requirements, every client will cache the list of services and periodically refresh the list of services from the Eureka server.

### Authentication Service

This is a simple authentication service; it is not implementing the Spring Security Framework because it only generates a JWT token when given some credentials. Another implemented endpoint is to register users.

![ ](https://www.plantuml.com/plantuml/png/NP11IpCn48Rl-HKl-nq-FMnBMq-BI45PxnwyfybO1sDIaqaN5V-xqxKhk9332tdccU7T5h5PwcimCNb2Ss-51llUm1RiVpBENxPAolnXXLDi2-KZE-h0KGtH4LRZZ4BFlnJ-zVtTUjCRuqAg7iqm30q-pPU1fhQzcLPqM2tmG6-LYi0UU7ceky1k6TAUepb3ol_LTrYVfZndSb8W2NQsVppEqMIuRjkvORF0emti5IYmqIEJr1mrwYi3vGj9BwIc9x3yBpYUu4jZz7wrJGrrk5hWDZ15GTg9vJXlOhGOiwNejUnl)

#### Register

This server contains a REST endpoint to register users. It's a simple POST to receive the user information.

```shell
curl -X POST \
-H "Content-Type: application/json" \
-d '{"name":"test","email":"test@email.com","password":"1234ABC"}' \
http://localhost:8080/api/public/auth/register
```

Notice that the endpoint is targeting public paths. This means that the gateway will not validate the JWT token.
Also, it contains the `auth` path, indicating that this is the authentication service. (This will be something that we will know internally but not the client).
The password is encoded using a BCrypt algorithm. In this case we are using `spring-security-crypto` library from Spring Security.

#### Login

This server contains a REST endpoint to login users. It's a simple POST to receive the user information.

```shell
curl -X POST \
-H "Content-Type: application/json" \
-d '{"email":"test@email.com","password":"1234ABC"}' \
http://localhost:8080/api/public/auth/login
```

This is another public path. The password is encrypted to be compared with the password stored in the database.

The response is a JWT token that will be used to authenticate the user in the other services. This token is signed using a secret key that is stored in the gateway service.

The important configuration setting is the secret key used to sign the JWT token. This key is used to validate the token in the gateway service.

```yml
jwt:
  secret: KXiUPknkoVDCHdNrrYnxmA5oQLbv+LE7eHG8vkk71fjEPq0lmlZKwBhUVLUqKSmGam8P4kWxXKTGCDRgpZ5BMA==
```

This key is generated using a SecureRandom algorithm and encoded in Base64. It's important to keep this key secret.

#### Private paths

This token is used to authenticate the user in the other services. The token is sent in the Authorization header in the format `Bearer <token>`. The gateway service will validate the token and route the request to the corresponding service.

### Communication between services

In this implementation the communication between services is done using REST API calls. To achieve this, a complementary library is used to handle communication easily, [`spring-cloud-starter-openfeign`](https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-feign.html). It's a declarative HTTP client that simplifies the interaction between services.

## Tech debt

There are some tech debts in this implementation that need to be addressed:

- Implement a [configuration server](https://spring.io/projects/spring-cloud-config) to centralize the configuration of all services.
- Enhance the [circuit breaker](https://spring.io/guides/gs/circuit-breaker/) to handle failures in the communication between services.
- Improve token validation in the gateway service. The current implementation is very basic and doesn't handle token expiration. Even if there is a verification of the token expiration, there is no mechanism to tag a token as invalid. This could be done using a blacklist of tokens.

## Conclusion

In this example, we implemented a basic microservices architecture using Spring Cloud. We created a simple service registry using Eureka, a gateway service to route requests to the corresponding service, and two simple services: the basic service and the authentication service. We used Feign to simplify the communication between services. We also implemented a simple authentication mechanism using JWT tokens. Although we didn't implement all the features that Spring Cloud provide (like the configuration server and the circuit breaker), we achieved a basic understanding of how to implement a microservices architecture using Spring Cloud.
