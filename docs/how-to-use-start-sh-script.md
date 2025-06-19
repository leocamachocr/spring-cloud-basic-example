Sure! Here's a **user manual in English** for the `start.sh` script you provided:

---

# 📘 `start.sh` Script Manual

This shell script automates the **build and launch** of a Spring Boot application with Gradle. It supports running the application on a specified or random available port, with special handling for Eureka server.

---

## 🔧 Requirements

* Unix-like OS or Windows with Git Bash / WSL
* `curl`, `netstat` or `ss`, `shuf`, `gradlew` must be available
* Java and Gradle installed (or included via `gradlew`)
* Spring Boot application with `bootRun` and Actuator enabled
* `actuator/health/liveness` endpoint available

---

## ▶️ Usage

```bash
./start.sh <app-folder> [port]
```

### Parameters

* `<app-folder>` (required): The folder where the Spring Boot app is located.
* `[port]` (optional): The port to run the application. If omitted, a random free port between **8000–9000** is used.

---

## 🔍 What the Script Does

1. **Validates input**: Ensures that the app folder is provided.
2. **Detects the OS**: Determines if it's running on Windows to use `powershell` for port checking.
3. **Checks port availability**: Ensures the desired port is not already in use.
4. **Builds the app**:

    * Runs `./gradlew clean build`
    * Stores logs in `../logs/build-<app>-<timestamp>.log`
5. **Handles Eureka separately**:

    * Always runs on port `8761`
    * Prevents duplicate Eureka instances
6. **Starts the app** using `bootRun`:

    * Logs go to `../logs/<app>-<port>-<timestamp>.log`
    * The `SERVER_PORT` environment variable is set
7. **Optionally exposes a debug port**:

    * A second random free port is assigned and configured via `JAVA_TOOL_OPTIONS`
8. **Health check loop**:

    * Pings `http://localhost:<port>/actuator/health/liveness` until the app responds with `200 OK`

---

## 📁 Logs

All logs are saved to the `../logs/` directory relative to the script location:

* Build log: `build-<app>-<timestamp>.log`
* Run log: `<app>-<port>-<timestamp>.log`

---

## 🧪 Examples

### Run app on a random port

```bash
./start.sh payment-service
```

### Run app on port 8082

```bash
./start.sh payment-service 8082
```

### Start Eureka (always runs on port 8761)

```bash
./start.sh eureka
```

---

## ⚠️ Notes

* If the app doesn't expose `/actuator/health/liveness`, the script will hang. You can adjust the health check path in the script.
* This script assumes the app uses `Gradle` and has a `bootRun` task.
* Random ports are generated using `shuf`; if `shuf` is missing, install `coreutils`.

---

Would you like the manual exported as a Markdown (`README.md`) file or PDF?
