# UC16: Database Integration with JDBC for Quantity Measurement Persistence

## Description

UC16 extends the Quantity Measurement Application by introducing persistent database storage through JDBC (Java Database Connectivity). Building upon the N-Tier architecture established in UC15, this use case implements a `QuantityMeasurementDatabaseRepository` class that replaces the in-memory `QuantityMeasurementCacheRepository` for long-term data persistence. The application now supports storing and retrieving quantity measurement operation history from a relational database, enabling audit trails, reporting, and historical analysis.

This use case also introduces:
- Professional project structure with Maven as the build tool.
- Proper package organization for controllers, services, repositories, and entities.
- Comprehensive test coverage and automated database schema creation.
- Dependency injection and factory patterns for switching between in-memory cache and database storage.

### Key Features
- **Professional Folder Structure**: Maven conventions with clear layer separation.
- **JDBC Integration**: Database operations with connection pooling.
- **Database Schema Management**: Automated schema creation and initialization.
- **Security**: Parameterized SQL queries to prevent SQL injection.
- **Transaction Management**: Ensures data consistency.
- **Testing**: Unit and integration testing with database fixtures.
- **Backward Compatibility**: Full support for UC1–UC15 functionality.

---

## Disadvantages of UC15 Implementation (Addressed in UC16)

### Limitations of UC15:
1. **Limited Data Persistence**:
  - In-memory cache lost on application crash.
  - Inefficient and non-scalable serialization to disk.
  - No support for multiple application instances.

2. **No Concurrent Access Management**:
  - No locking mechanism for concurrent updates.
  - Not suitable for distributed systems.

3. **Lack of Query Capabilities**:
  - No ability to query by specific criteria.
  - Entire dataset must be loaded into memory.

4. **No Data Validation at Storage Layer**:
  - No schema enforcement or referential integrity.

5. **Poor Scalability**:
  - Limited by heap memory.
  - Serialization to file is slow and blocks operations.

6. **Difficulty in Data Analysis and Reporting**:
  - No SQL support for complex queries.
  - Inefficient analytics.

7. **Debugging and Troubleshooting Issues**:
  - Serialized files are not human-readable.
  - No audit trail or change history.

8. **No Integration with Enterprise Tools**:
  - No compatibility with BI tools or data warehouses.

9. **Testing Difficulties**:
  - In-memory repository unsuitable for concurrent scenarios.
  - Hard to reset the database between tests.

10. **No Schema Versioning or Migration**:
   - Risky updates to serialized data format.

---

## Preconditions
- All functionality from UC1–UC15 is operational and tested.
- N-Tier architecture with clear layer separation is established.
- Maven is installed and configured.
- A relational database (MySQL, PostgreSQL, H2) is available.
- JDK 8 or higher is installed.

---

## Main Flow

### Step 1: Set Up Project Structure
1. **Maven Standard Directory Layout**:
  - Organize files into `src/main/java`, `src/main/resources`, and `src/test/java`.
  - Refactor package names to `com.app.quantitymeasurement`.

2. **Add Logger**:
  - Replace `System.out.println` with Java's built-in logging framework.

### Step 2: Create Maven POM Configuration
- Add dependencies for:
  - H2 Database (for testing).
  - MySQL and PostgreSQL JDBC drivers (commented for future use).
  - SLF4J and Logback for logging.
  - JUnit and Mockito for testing.
- Include Maven plugins:
  - Maven Shade Plugin for creating a fat JAR.
  - Maven Surefire Plugin for running tests.

### Step 3: Execute Maven Commands
- `mvn clean compile`: Compile the project.
- `mvn exec:java`: Run the application.

### Step 4: Database Configuration
1. **Create `application.properties`**:
  - Add database connection properties.
  - Configure environment-specific settings.

2. **Create Database Schema**:
  - Define schema in `src/main/resources/db/schema.sql`.

### Step 5: Create Database Utilities
- **`ApplicationConfig`**: Load database properties.
- **`ConnectionPool`**: Manage reusable database connections.

### Step 6: Update Exception Hierarchy
- Add `DatabaseException` for handling database-related errors.

### Step 7: Implement Database Repository
- Implement `QuantityMeasurementDatabaseRepository` using JDBC.
- Add CRUD operations and query methods.

### Step 8: Update Service to Support Both Repositories
- Use dependency injection to switch between cache and database repositories.

### Step 9: Update Application Entry Point
- Initialize the appropriate repository based on configuration.

---

## Postconditions
- Professional Maven project structure established.
- Database schema created for production and test environments.
- JDBC-based repository implemented with connection pooling.
- All UC1–UC15 functionality preserved and tested.
- Proper error handling and logging implemented.

---

## Concepts Learned
1. **Maven Project Structure**:
  - Standard directory hierarchy and package organization.
2. **JDBC**:
  - Connection management, parameterized queries, and resource cleanup.
3. **Connection Pooling**:
  - Efficient database resource management.
4. **Database Schema Design**:
  - Tables, indexes, and referential integrity.
5. **Testing with Mock Databases**:
  - H2 for isolated unit and integration tests.
6. **Transaction Management**:
  - Ensures data consistency.

---

## Example Output

### Example 1: Saving a Quantity Comparison to Database
```
Measurement saved: Length comparison (Meters to Kilometers)
```

### Example 2: Retrieving Measurements by Type
```
Retrieved Measurements: [Length, Weight]
```

### Example 3: Connection Pool Management
```
Active Connections: 5, Idle Connections: 3
```

---

## Test Case Examples

### Unit Tests
- `testDatabaseRepository_SaveEntity()`: Verifies data is saved correctly.
- `testDatabaseRepository_QueryByOperation()`: Verifies filtering by operation type.

### Integration Tests
- `testServiceWithDatabaseRepository_Integration()`: Verifies end-to-end functionality with database persistence.

### Maven Build Tests
- `testMavenBuild_Success()`: Verifies project builds successfully.
- `testMavenTest_AllTestsPass()`: Verifies all tests pass.

---

## Key Concepts Tested
- JDBC connection management.
- SQL query execution and parameterized queries.
- Data persistence and transaction management.
- Maven build process and dependency management.
- Backward compatibility with UC1–UC15.
