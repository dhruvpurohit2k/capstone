**2. Technology and Implementation Requirements**

**2.1.**  **Backend Technologies**

* Java 21 (Mandatory)
* Spring Boot 3.5.7
* Spring Web for REST API development
* Spring Security for authentication and authorization
* Spring Data JPA for database operations
* PostgreSQL for relational data persistence
* Spring AI for AI-powered functionalities
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