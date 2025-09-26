# HoloDB Customized Example

This example is similar to the [standalone example](../holodb-standalone),
however it adds some custom Java components that are wired into HoloDB through the configuration file.
These Java components are implemented in the surrounding Gradle project (see the `src` directory).

## :wrench: Build it

Compile the project and build the Docker image:

```bash
./gradlew build
./build.sh
````

## :arrow_forward: Run it

Start the container:

```bash
./start.sh
```

Now HoloDB will run with your custom implementations enabled.
You can reach it on the local port **3430**.
See the configuration file (`config.yaml`) to learn how to reference them.

## :stop_button: Stop it

Stop and remove the container:

```bash
./kill.sh
```
