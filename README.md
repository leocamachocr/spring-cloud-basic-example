# project-research

Este repositorio tiene el fin de ser utilizado para colocar pruebas de concepto (POC)

## Configuración de Micro-servicios

### Implementación del API

En una primera abstracción se tiene a la aplicación como un todo que expone sus servicios utilizando comunicación REST. Este debe ser consumido por aplicaciones cliente: Web, Mobiles, Sistemas de terceros.

````plantuml
@startuml
actor client
node app
node ui
database db

client --> ui
ui --> app
app-->db
@enduml
````

### Implementación de REST-API

En un segundo nivel de abstracción se tienen los micro-servicios que conforman la aplicación. Estos se comunican utilizando REST. Esto se da por medio del Servidor de descubrimiento Eureka.
Para la exposición del API con aplicaciones externas, se realiza implementando un Gateway.

````plantuml
@startuml
actor client
node gateway
node eureka
node service1
node service2
node ui
database db1
database db2

client --> ui 
ui --> gateway 
gateway -> eureka
service1 <..> eureka
service2 <..> eureka
gateway --> service1
gateway --> service2
service1-->db1
service2-->db2
@enduml
````

## POC de la implementación

### Basic Service y Client Service

Cada uno de estos servicios es un micro-servicio como tal, cada uno de ellos es un servicio independiente, con su propia autonomía es decir: su propia base de datos, sus procesos independientes.
La comunicación entre servicios se ha definido de forma sincrónica. Se considerará a futuro la implementación de una comunicación asincrónica dependiendo de las necesidades que el sistema genere.

### Gateway

El directorio [gateway](/gateway) contiene la configuración de los diferentes servicios, trazando los patrones de las URL con cada servicio. Este se comunica con Eureka para identificar el mejor servicio disponible en cada momento.
