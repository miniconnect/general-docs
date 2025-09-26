# HoloDB Wikidata Example

This example is similar to the [standalone example](../holodb-standalone),
however it adds a custom value set retrieved from Wikidata using a SPARQL query.
You can find the exact query [in the `professionals.sparql` file](professionals.sparql).

## :wrench: Build it

Build the Docker image:

```bash
./build.sh
````

This runs the SPARQL query and collects the results into `professionals.txt`.

## :arrow_forward: Run it

Start the container:

```bash
./start.sh
```

HoloDB will now run using the data stored in `professionals.txt`.
The configuration (`config.yaml`) references this file via the `valuesResource` key.

## :stop_button: Stop it

Stop and remove the container:

```bash
./kill.sh
```
