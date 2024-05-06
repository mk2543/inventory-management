# Introductions

Hey! I chose Kotlin / Spring Boot / Jooq / Flyway / PostgreSQL for this project.

The project provides REST endpoints for all required functionalities.

I also wore a product hat and made a lot of assumptions about how this product should work from the business point
of view. Normally I'd expect to discuss all of that with the team and the Product Owner, but here I went ahead with
my own assumptions. They are described below, in the document.

That also meant that I made some decisions that go against the assigment specification, or add something to it.

# How to run

## Prerequisites

- Java 17+
- Docker
- Docker Compose

## Running the application

1. Run dockerized PostgreSQL:
    ```shell
    docker-compose -f compose.yaml up -d
    ``` 

2. Run the application:
    ```shell 
   ./gradlew bootRun
   ```
3. Confirm that application is up

http://localhost:8080/actuator/health

4. Check SwaggerUI

http://localhost:8080/swagger-ui/index.html

Or OpenAPI Spec: http://localhost:8080/v3/api-docs

# My assumptions about the business requirements

- I assumed that the system should handle multiple warehouses and calculate inventory separately for each of them.
- Product and article definitions are still shared between them.
- I assumed that the requirement to "return products and their quantities" is about providing a product catalogue to be
  used in the e-commerce webshop.
    - because of that I'm returning only the products and their quantities, not articles. Webshop would not display the
      articles to the users.
    - The assigment asked to return all products, but I think it would be more useful to return only some of them based
      on
      product IDs, so I implemented both versions. No webshop displays the whole catalogue on some page, it's either
      search results, results for
      category, or at least either infinite scroll or some pagination.
- I assume the endpoint to sell products will be used by Order service, which will first change inventory, then process
  payment.
    - That's why that endpoint is defined in terms of OrderLines, each consisting of a product id and desired quantity
      of that product.
    - The purchase is a distributed transaction, so we should also have a way to revert the applied inventory changes if
      the payment fails. We would need another endpoint for that, but I didn't have time to implement it.
- The two points above also mean that I'm calculating product availability differently for the catalogue and order
  processing.
    - The catalogue double counts articles that are used in multiple products, while the order processing does not.
- The assigment requested an option to upload articles and products as a file, I decided to not implement it.
- Instead, I implemented a way to upload them via REST API. I think that's more useful for the future development of the
  system.
    - I assumed that the uploads should represent a delta change, not a full replacement.
    - That puts a limitation on the upload - it can handle only as much data as can fit in the memory. If we need to
      handle larger files, I'd suggest switching from JSON to JSON Lines format and uploading the file to S3 (or similar
      bucket storage, depending on the chosen infrastructure).
- I also made an assumption that products should have an ID, not just a name. I'm assigning the IDs during the upload
  and return them in the response.

# Architecture

I've chosen a simple layered architecture, as the project is small. In a larger project, I'd consider splitting the
package structure
across the vertical domain slices, but here it would be an overkill.

- `controllers` - REST controllers
- `domain` - business logic
- `repositories` - database access

## Database access

I've chosen Postgres because we need ACID transactions to ensure data consistency. I'm using Jooq for database access,
because it's flexible in the ways the queries can be constructed, and it's typesafe. The structure of the repositories
was dictated by the business logic, not the other way around - while I decided on a DB schema early on, I delayed
writing
actual repositories until the very end. In between I was using simple in-memory storage just to shape the best
interfaces.

I chose Flyway to manage the database schema because it's simple and easy to use.

## Database schema

- I'm using a simple schema with a few tables: `articles`, `product_articles` (as there's many to many relationship
- between them) `products`, `articles_inventory` (to keep track of articles in each warehouse).

- Warehouse IDs aren't currently validated in any way. They would either reside in app's config or in a separate DB
  table.

- The `articles_inventory` table stores a version field used for optimistic locking, to ensure the consistency of the
  quantity updates.

- I didn't bother to implement optimistic locking for other tables, as I assume the updates of article or product
  definitions are infrequent, but it could be added if needed.

- The schema is designed under the assumption that the sales are distributed across a wide range of articles. It
  wouldn't be optimal for sales of a few hot items (think for example of sales of PlayStation 5 just after its release,
  where the whole inventory was gone from shops a few seconds after it was added)

## Testing

- The domain logic is covered with unit tests. I used JUnit 5, AssertJ, MockK for that.
- I also wrote some integration tests for the controllers. They run against the Postgres running a TestContainers.
- In a bigger system, I'd also write some integration tests specific for the repositories.
- Ideally, we would also have some kind of black box tests, that would run the whole application in a container and
  verify that it behaves as expected. I didn't have time to write them.
- We would also need to verify integrations between different services.

## Rest API

- I used Spring Web for the REST API. The service provides OpenAPI spec, which can be used by the clients to generate
  stubs.

## Caching

- Currently, the project contains no caching at all, but probably we would need to cache at least the product catalogue
  endpoints. I think it's safe to assume that this project would be read-heavy, with the users browsing the webshop more
  frequently than they buy something.

## Production readiness

- The application exposes Prometheus metrics on: http://localhost:8080/actuator/prometheus
- Currently only the default metrics are there, but as Micrometer is already set up in the project, we could add custom
  metrics as well
- The application doesn't provide any authentication or authorization yet. In a real system, we would need to
  implement that, but the details depend on the context: would it accept only internal requests from other services,
  or would it be exposed to the Internet? Where would it be deployed? Some K8S cluster, with service mesh handling the
  security? Or maybe it would be deployed on a single server, behind a reverse proxy? All of those would require
  different security measures.
- It's also missing any kind of rate limiting, which would be necessary to prevent abuse.
- It doesn't have any logging yet. In a real system, we would probably want to expose the logs in a format that can be
  consumed by systems like ELK.
- We could also consider adding distributed tracing.

## Future business improvements

- As mentioned earlier, we should have a way to revert the inventory changes if the payment fails.
- We would also need some kind of inventory management, a simpler endpoint to adjust quantities of articles in the
  warehouse. It would be used for refunds or for manual adjustments (for example when a warehouse employee notices that
  something is damaged)
- We could consider adding an option to reserve some inventory for a short period of time at the moment when user adds
  some to their basket.
- As mentioned before, if we really need to handle large files for the uploads, we should have a way to do it in the
  background, without affecting the app's performance.
- I'm pretty sure that we would need some kind of reporting capabilities, but it's hard to say what exactly would be
  needed without knowing the business requirements. It could be anything from simple reports on the sales to exposing
  the data as events to some Data Lake / Data Warehouse.
- We may consider producing some events for the downstream systems to consume.

## Technical improvements

- Clean up build.gradle.kts, it's a mess ;-)
- JOOQ is using H2 in the Gradle build. We should switch over to Postgres (TestContainers) for consistency.
- Add more tests, especially for the repositories
- Consider if we need some mapping library for the domain <-> DTOs conversions. Right now the classes are simple, so I
  didn't see a need to add it yet