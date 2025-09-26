# HoloDB REST API Mocking Demo

This demo shows how embedded HoloDB can automatically mock an entire REST API directly from plain JPA entities.
It is a regular Micronaut Java application and can also serve as a starting template for similar projects.

## :arrow_forward: Run it

Start the application using the `demo` task:

```
./gradlew demo
```

## :arrows_counterclockwise: Test the endpoints

Then the following endpoints will be available:

- `http://localhost:8080/categories/**`
- `http://localhost:8080/authors/**`
- `http://localhost:8080/posts/**`
- `http://localhost:8080/posts/{id}/comments/**`

For example, retrieving an author's data:

```bash
curl -s http://localhost:8080/authors/21 | jq
```

Output:

```json
{
  "id": 21,
  "firstname": "Eric",
  "lastname": "Martin"
}
```

Explore the bundled Swagger UI for a friendlier testing experience:

```
http://localhost:8080/swagger-ui
```

## :gear: How it works

The `demo` Micronaut environment configures:

- JDBC URL: `jdbc:holodb:jpa:///demo`
- Driver: `hu.webarticum.holodb.jpa.JpaMetamodelDriver`

This automatically configures and launches an embedded HoloDB instance based on your JPA entities.
You can fine-tune the configuration with your specific settings using annotations such as `@HoloTable`, `@HoloColumn`, etc.

## :memo: Notes

> [!NOTE]
> Currently, the explicit startup listener (`HoloInit`) is necessary for scanning the JPA metamodel.
>
> This requirement is planned to be eliminated in the future.
