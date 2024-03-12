# Servicio de Cliente

La intención de este servicio es servir como ejemplo para la implementación de algunas funcionalidades básicas de cualquier aplicación

## Autenticación de usuarios

Se utiliza el framework de spring-security con autenticación vía JWT, todo esto bajo el paquete `ucr.sa.authentication.security`

- `api`: Endpoints de autenticación y registro.
- `config`: Configuración de la seguridad, constantes de seguridad y manejo de excepciones de autenticación.
- `handlers`: Comandos y consultas: Autenticación, Registro y Consulta por username.
- `http`: Filtro para interceptar e interpretar JWT y Servicio para decodificarlo.
- `jpa`: Entidades referentes a la seguridad.
- `models`: Modelos para el manejo de la autenticación y sesiones.

## Manejo de excepciones

Administrador de excepciones para responder atrevés del api: `ucr.sa.authentication.exceptions`

- `ExceptionResponseHandler`: Controlador de excepciones, las captura y procesa para responder de una manera estandarizada por los errores
- `ErrorCodes`: Listado de códigos de error, se implementa un código para facilitar la comunicación del tipo de mensaje con los clientes web/mobile.