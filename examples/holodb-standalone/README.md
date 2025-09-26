# HoloDB Standalone Example

This example shows how to run HoloDB in a standalone Docker container using a custom configuration file (`config.yaml`).
The included shell scripts help with building and managing the container.

## :wrench: Prepare it

Build the Docker image for local use:

```bash
./build.sh
````

## :arrow_forward: Run it

Start the container:

```bash
./start.sh
```

Now you can reach the server on the local port **3430**.
There are several ways to connect to it, for example:

- [via JDBC (in case of Java)](https://github.com/miniconnect/miniconnect
- [using the MiniConnect REPL](https://github.com/miniconnect/miniconnect-client)
- [from any application using the Postgres wire protocol (experimental)](https://github.com/miniconnect/miniconnect-postgres)

## :stop_button: Stop it

Stop and remove the container:

```bash
./kill.sh
```
