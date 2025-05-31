# Implementing monitoring and traceability

## Requirements

- Java 21
- Spring Boot
- Spring Cloud ecosystem
- Spring Cloud Sleuth
- GitBash(For Windows users)
- IntelliJ IDEA (Optional)

## Configuration

### Install Java 21

Ensure that Java 21 is installed on your system. You can check the version by running:

```bash
java -version
```

- If Java 21 is not installed, you can download it from the [Oracle](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) or Download it from IntelliJ IDEA. If you already have Java 21 in your system, but is not the default version, you can set it as the default version by following these steps:

#### Windows

1. Open the Start menu and search for "Environment Variables".
2. Click on "Edit the system environment variables".
3. In the System Properties window, click on the "Environment Variables" button.
4. In the Environment Variables window, find the "Path" variable in the "System variables" section and select it.
5. Click on the "Edit" button.
6. In the Edit Environment Variable window, click on "New" and add the path to your Java 21 installation (e.g.,
   `C:\Program Files\Java\jdk-21\bin`).
7. Click "OK" to close all the windows.
8. Open a new command prompt and run `java -version` to verify that Java 21 is now the default version.

##### Not permitted to edit the system environment variables

If you are not permitted to edit the system environment variables, you can set temporarily the
`JAVA_HOME` environment variable in your terminal session by running:

```bash
export JAVA_HOME=/path/to/your/java21
export PATH=$JAVA_HOME/bin:$PATH
```

#### Linux/MacOS

1. Open a terminal.
2. Edit your shell configuration file (e.g., `~/.bashrc`, `~/.bash_profile`, or `~/.zshrc`) and add the following lines:

```bash
export JAVA_HOME=/path/to/your/java21
export PATH=$JAVA_HOME/bin:$PATH
```






### Make sure your Spring Cloud is configured

