# Spring Cloud Basic Example

This repo contains a basic example of a microservices architecture using Spring Cloud.

## Architecture

This is an attempt to create a microservices architecture using Spring Cloud. It consists in three types of services:

- Simple Spring Boot Services: In this example there are two types of services, the basic service and the authentication service. The basic service only contains some basic logic to support REST calls and interact with others services. The authentication service is used to authenticate users and generate JWT tokens. They are designed to be stateless and to be able to scale horizontally.
- Eureka Server: This is the service registry. It's used to register all the services and to discover them.
- Gateway: This is the entry point to the system. It's used to route the requests to the corresponding service. It also handles authorization.

Note: Not all the features of Spring Cloud are used, in order to simplify the example. Also, the security is limited to an enough level to show how to implement it. However, it's a good starting point to understand how to create a microservices architecture using Spring Cloud.

![](https://www.plantuml.com/plantuml/png/RP31YiCW48RlynH3xWRYSLb2RliS4nrsPLC3SQoKqdUlJJGqRK_ZVFxVE3W4eKNYQG8_MyRcXuGJNHgaWB_RURlXkF0_bHH5-MClpf2usQl0ozaP_aBdypXlk5lzWhiYZHISUS_Y8QmHB0dbBACTk-T60vXfZXPJIdY56-IgL5-tB1LLNtUjx-Dtsxv2VO2xc-LgVO9wdvdXOxtArrR3mDxclxm3)

### Gateway Service

![](
httpS://www.plantuml.com/plantuml/png/XP7DJeGm4CVlynJ_x6Nrq3M0UY26xCsxRp1RPaqZjh4j46DyT_ccqOCd9iFl_y5m3f5zRDyRiguM79uvIi-V1t30wppFRwJryiOBTU5Wj0hYEwEbXB63YzM2RD8j-np_q7bA5o3IZMjhz7sFB_gcuQbKjPGeaPtm8bfC9_3oqKg8E4xETrw2QxKSQM6XghajIkIZQgQlpeCnIVOEtB5f-D88BP8j4IMAKHozaI6JxSx-ChbPX-nshw2gxGubxlSMqI5fxRNGYa6bsGz9je6zkl4Um9DIs4xoc383idx3wRHw_jaKH6UnzV33Vm00
)

[Gateway service](https://spring.io/projects/spring-cloud-gateway) is the entry point to the system. It's used to route the requests to the corresponding service. It also handles authorization.

It contains a [filter](gateway/src/main/java/dev/leocamacho/gateway/config/AuthenticationFilter.java) to intercept all the requests and validate the JWT token. It also routes the requests to the corresponding service. This filter is executed before the request is routed to the service. It validates the requested [path](gateway/src/main/java/dev/leocamacho/gateway/config/RouterValidator.java), and if it's a public path, it allows the request to be routed to the service. If it's a private path, it validates the JWT token and then routes the request to the service.

### Eureka Service

Eureka service is the service registry. It's used to register all the services and to discover them.

Every service registers itself in the Eureka server. Also, it is used to discover other services. For example, the gateway service uses Eureka to discover the basic and authentication services.

Here is an example step by step how multiple instance of the same service are registered in Eureka:

![](https://www.plantuml.com/plantuml/png/HS-nxe8m40RmlK_n1OR_nF2d2WbnO11M4ts22XUzg4VIAmu-lKr1tPhs-_M-QpKu7-R7tSWq4cZsT7F8hQtjjbxkihDJ7Web_-I1q80b8ed7_mnEaPwHTzsch2hpXmMvEKoUNoxm91_xeoWJZQ7hDuMyx_UbxOrPM-oAOlcymK5mMkUdmWfM7Ed26iWZsYuHOrgICMxp1W00)

After the service is register, Eureka will know the existence of the `instance1`.

![](http://www.plantuml.com/plantuml/png/ZS-nhe8m50RWlK_n1uOxCTpsNaY80uEmckWJL3ZHHYX9J-lWqzi0ui2WMzFpVoV_gTXhvyFG2pjs10gERXgfKeNwB-Q_vApPj1cJkvkMKCBOY6ny_YRGZ6cCjAx_P9v_8l9Dv6f-C2iT_EZCGtipsXVUs1YwhYcaJRENgX5YQRxBidbxYOF5QIam7SamZ2DT37joYBQUo4RAxwceo7PXw9y0)

If another instance of the same service is registered, Eureka will know the existence of the `instance2`.

![](https://www.plantuml.com/plantuml/png/dS-npe8m40VmlKznWPdXQpyX8GuCmcgYJr3XeXUX9BtMmQUtAQXH1erRqzxl_lzMxBHrVkY56Ji4z1RF6bAb2hKgfhlae9dQZD5Ug4KeWKn09Xp_4j0CyHdIklwNUVw9o2UHRqPxDrgwQaUZkTD5M1WwFQOWRPgDL8qGS-zrbasxIzWQsC_nyOiCnB338x532y7MaIoa_8M-rlf-WwZGT7xeRm00)

Same process is repeated for the `instance1` of authentication service.

During this process each eureka instance will notify the Eureka server about it existence using a heartbeat mechanism. If the Eureka server doesn't receive a heartbeat from a service instance for a certain amount of time, it will remove the instance from the registry.

In the same way, depending on the necessity, every client will cache the list of services and will periodically refresh the list of services from the Eureka server.

### Authentication Service

This is a simple authentication service, its not implementing Spring Security Framework because it only generates a JWT token given some credentials. The other endpoint implemented is to register users.

![](https://www.plantuml.com/plantuml/png/NP11IpCn48Rl-HKl-nq-FMnBMq-BI45PxnwyfybO1sDIaqaN5V-xqxKhk9332tdccU7T5h5PwcimCNb2Ss-51llUm1RiVpBENxPAolnXXLDi2-KZE-h0KGtH4LRZZ4BFlnJ-zVtTUjCRuqAg7iqm30q-pPU1fhQzcLPqM2tmG6-LYi0UU7ceky1k6TAUepb3ol_LTrYVfZndSb8W2NQsVppEqMIuRjkvORF0emti5IYmqIEJr1mrwYi3vGj9BwIc9x3yBpYUu4jZz7wrJGrrk5hWDZ15GTg9vJXlOhGOiwNejUnl)