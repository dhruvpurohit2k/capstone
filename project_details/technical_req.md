**2. Technology and Implementation Requirements**

**2.1.**  **Backend Technologies**

* Java 21 (Mandatory)
* Spring Boot 3.5.7
* Spring Web for REST API development
* Spring Security for authentication and authorization
* Spring Data JPA for database operations
* Amazon RDS for PostgreSQL for relational data persistence
* Spring AI with Amazon Bedrock Converse for AI-powered functionality
* Bean Validation for request validation
* Appropriate exception handling and HTTP status codes

**2.2.**  **Frontend Technologies**

* React.js
* Functional Components and React Hooks
* Form handling and validation
* REST API integration using Axios
* Search, filtering and tabular data presentation
* Appropriate loading indicators, success messages and error handling

**2.3. Application Architecture Guidelines**

Implement a layered architecture comprising Controller, Service, Repository, Entity/DTO and AI Integration layers.

* Use DTOs for communication between frontend and backend.
* Maintain appropriate entity relationships and database constraints.
* Implement business logic within the service layer.
* Avoid exposing JPA entities directly through REST APIs.
* Maintain environment-specific configurations using appropriate configuration properties.
* Implement transaction management wherever required.
* Persist AI-generated insurance recommendations wherever specified.
* AI must not replace deterministic business validations or financial calculations.

**2.4 Environment configuration**

Set these variables in the build or application environment.

| Variables | Purpose |
| --- | --- |
| `MAVEN_USER_HOME` | Custom Maven user directory. The wrapper uses its `repository` directory for dependencies and reads `settings.xml` from it when present. |
| `M2_HOME` | Path to an installed Maven distribution. When omitted, the wrapper installs the distribution pinned in the project under `MAVEN_USER_HOME`. |
| `MAVEN_REPO_LOCAL` | Optional path to a separate Maven dependency repository. |
| `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | RDS PostgreSQL JDBC URL and database credentials supplied by the deployment. Include the TLS settings in the JDBC URL. For server verification, use `sslmode=verify-full` with the RDS CA file in `sslrootcert`. |
| `AI_CHAT_MODEL` | Set to `bedrock-converse` to enable AI recommendations. Defaults to `none`. |
| `AWS_REGION`, `BEDROCK_CONVERSE_MODEL` | Bedrock region and a Converse-compatible model ID or inference profile ID/ARN. |
| `BEDROCK_TEMPERATURE`, `BEDROCK_MAX_TOKENS` | Inference settings supported by the selected model. |
| `BEDROCK_MODEL_VERSION` | Optional audit label. The model ID is used when this is omitted. |
| `BEDROCK_REQUEST_TIMEOUT` | Optional duration for the entire Bedrock call. Defaults to 30 seconds. |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Set through the AWS SDK environment credential provider when supplying credentials directly. The SDK also supports the deployed workload's IAM role. |
| `JWT_SECRET` | Required nonblank JWT signing secret, read as UTF-8 text. |
| `JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL` | Optional duration overrides. Defaults are 15 minutes and 7 days, respectively. |
| `JWT_SECURE_COOKIES` | Optional secure-cookie flag. Defaults to `true`. |

Run `./mvnw clean package` from `backend` after setting the Maven paths. Maven's Spring context test uses the same backend environment and an already initialized PostgreSQL test database.

Hibernate validates the schema on startup. Provision RDS with the schema for this release before starting the service. Use a separate pre-initialized database with `SPRING_PROFILES_ACTIVE=development` when loading the example records.
