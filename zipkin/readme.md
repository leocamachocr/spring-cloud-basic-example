# Zipkin

El JAR de Zipkin no está commiteado en el repositorio.
Usar los scripts provistos para descargarlo y arrancarlo.

## Descargar

```bash
./zipkin/download.sh
```

## Arrancar

```bash
./zipkin/start.sh
```

## Cambiar versión

Pasar la versión como variable de entorno:

```bash
ZIPKIN_VERSION=3.5.1 ./zipkin/start.sh
```

La versión por defecto es `3.5.1`.
El JAR descargado vive en este directorio y está ignorado por git.
