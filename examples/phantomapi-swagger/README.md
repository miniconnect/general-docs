# PhantomAPI Swagger

A Docker Compose setup running PhantomAPI based on an OpenAPI file, and a Swagger UI service for manual tryout.

## :arrow_forward: Run it

Start the services:

```bash
./start.sh
```

This will run the service stack in foreground mode.
It also launches a browser tab pointing to the Swagger UI.
If you want to only run the services, you can use the wrapped command directly:

```bash
docker compose up --remove-orphans
```
