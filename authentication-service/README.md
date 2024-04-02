# Authentication Service

The intention of this service is to serve as an example for the implementation of some basic functionalities of any application

## User Authentication

The spring-security framework is used with JWT authentication, all under the `dev.leocamacho.authentication.security` package

- `api`: Authentication and registration endpoints.
- `config`: Security configuration, security constants and authentication exception handling.
- `handlers`: Commands and queries: Authentication, Registration and Query by username.
- `http`: Filter to intercept and interpret JWT and Service to decode it.
- `jpa`: Entities related to security.
- `models`: Models for handling authentication and sessions.

## Exception Handling

Exception manager to respond through the api: `dev.leocamacho.authentication.exceptions`

- `ExceptionResponseHandler`: Exception controller, captures and processes them to respond in a standardized way for errors
- `ErrorCodes`: List of error codes, a code is implemented to facilitate the communication of the message type with web/mobile clients.