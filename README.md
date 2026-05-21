# School Management

REST API for managing students, teachers, and courses in a school, plus a small set of reporting endpoints. Built with Spring Boot 4 and a relational data model where students and teachers are linked to courses via many-to-many relationships.

## Prerequisites

- JDK 25
- Maven 3.9+ (or use the bundled `./mvnw` wrapper)

## Build & Run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` with the `dev` profile active by default.

### Useful URLs (dev profile)

| Resource     | URL                                       |
| ------------ | ----------------------------------------- |
| Swagger UI   | http://localhost:8080/swagger-ui.html     |
| OpenAPI JSON | http://localhost:8080/v3/api-docs         |
| H2 Console   | http://localhost:8080/h2-console          |

H2 console credentials (dev): JDBC URL `jdbc:h2:mem:schooldb`, user `admin`, empty password.

## Configuration

All settings in `application.properties` are externalized via `${ENV_VAR:default}` placeholders. The default profile is `dev`, which loads `application-dev.properties` with H2 in-memory values.

### Environment variables

| Variable                                       | Purpose                              |
| ---------------------------------------------- | ------------------------------------ |
| `SPRING_APPLICATION_NAME`                      | Application name                     |
| `SPRING_PROFILES_ACTIVE`                       | Active profile (default `dev`)       |
| `SPRING_DATASOURCE_URL`                        | JDBC URL                             |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME`          | JDBC driver class                    |
| `SPRING_DATASOURCE_USERNAME`                   | DB username                          |
| `SPRING_DATASOURCE_PASSWORD`                   | DB password                          |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`                | Hibernate schema strategy            |
| `SPRING_JPA_SHOW_SQL`                          | Log SQL statements                   |
| `SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL`   | Pretty-print logged SQL              |
| `SPRING_JPA_OPEN_IN_VIEW`                      | Open EntityManager in view (avoid)   |
| `SPRING_H2_CONSOLE_ENABLED`                    | Enable H2 web console                |
| `SPRING_H2_CONSOLE_PATH`                       | H2 console path                      |
| `SPRINGDOC_SWAGGER_UI_PATH`                    | Swagger UI path                      |
| `SPRINGDOC_API_DOCS_PATH`                      | OpenAPI JSON path                    |

### Switching profiles

```bash
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://... \
SPRING_DATASOURCE_USERNAME=... \
SPRING_DATASOURCE_PASSWORD=... \
./mvnw spring-boot:run
```

## API Overview

Base path: `/api`.

### Students — `/api/students`

| Method | Path           | Description                       |
| ------ | -------------- | --------------------------------- |
| POST   | `/`            | Create a student                  |
| GET    | `/`            | List all students                 |
| GET    | `/{id}`        | Get a student by id               |
| PUT    | `/{id}`        | Replace a student                 |
| DELETE | `/{id}`        | Delete a student                  |

### Teachers — `/api/teachers`

| Method | Path           | Description                       |
| ------ | -------------- | --------------------------------- |
| POST   | `/`            | Create a teacher                  |
| GET    | `/`            | List all teachers                 |
| GET    | `/{id}`        | Get a teacher by id               |
| PUT    | `/{id}`        | Replace a teacher                 |
| DELETE | `/{id}`        | Delete a teacher                  |

### Courses — `/api/courses`

| Method | Path           | Description                                            |
| ------ | -------------- | ------------------------------------------------------ |
| POST   | `/`            | Create a course (409 on duplicate name)                |
| GET    | `/`            | List all courses                                       |
| GET    | `/{id}`        | Get a course by id                                     |
| PUT    | `/{id}`        | Rename / re-type a course                              |
| DELETE | `/{id}`        | Delete a course (409 if linked to students or teachers)|

### Reports — `/api/reports`

| Method | Path                                            | Description                                          |
| ------ | ----------------------------------------------- | ---------------------------------------------------- |
| GET    | `/students/count`                               | Total student count                                  |
| GET    | `/teachers/count`                               | Total teacher count                                  |
| GET    | `/courses/count`                                | Course counts grouped by type                        |
| GET    | `/courses/{courseId}/students`                  | Students enrolled in a course                        |
| GET    | `/courses/{courseId}/students?minAge=N`         | Students in a course older than `N` (min 18)         |
| GET    | `/groups/{groupName}/students`                  | Students in a group                                  |
| GET    | `/group-course?group=G&courseId=N`              | Teachers + students for a group/course combination   |




# school-management
