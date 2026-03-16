package com.quantityMeasurementApp.repository;

import com.quantityMeasurementApp.exception.DatabaseException;
import com.quantityMeasurementApp.model.QuantityMeasurementEntity;
import com.quantityMeasurementApp.util.ApplicationConfig;
import com.quantityMeasurementApp.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QuantityMeasurementDatabaseRepository implements IQuantityMeasurementRepository {

    private static final Logger logger =
            LoggerFactory.getLogger(QuantityMeasurementDatabaseRepository.class);

    private final ConnectionPool connectionPool;

    private static final String INSERT_SQL = """
            INSERT INTO quantity_measurement_entity (
                this_value, this_unit, this_measurement_type,
                that_value, that_unit, that_measurement_type,
                operation, result_value, result_unit, result_measurement_type,
                result_string, is_error, error_message
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BASE_SQL = """
            SELECT id, this_value, this_unit, this_measurement_type,
                   that_value, that_unit, that_measurement_type,
                   operation, result_value, result_unit, result_measurement_type,
                   result_string, is_error, error_message, created_at
            FROM quantity_measurement_entity
            """;

    private static final String SELECT_ALL_SQL =
            SELECT_BASE_SQL + " ORDER BY id";

    private static final String SELECT_BY_OPERATION_SQL =
            SELECT_BASE_SQL + " WHERE operation = ? ORDER BY id";

    private static final String SELECT_BY_MEASUREMENT_TYPE_SQL =
            SELECT_BASE_SQL + " WHERE this_measurement_type = ? ORDER BY id";

    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM quantity_measurement_entity";

    private static final String DELETE_ALL_HISTORY_SQL =
            "DELETE FROM quantity_measurement_history";

    private static final String DELETE_ALL_SQL =
            "DELETE FROM quantity_measurement_entity";


    public QuantityMeasurementDatabaseRepository() {
        this(new ConnectionPool(ApplicationConfig.getInstance()));
    }

    public QuantityMeasurementDatabaseRepository(ConnectionPool connectionPool) {
        this.connectionPool = Objects.requireNonNull(connectionPool);

        String dbUrl = ApplicationConfig.getInstance().getDbUrl();
        logger.info("Initializing repository using DB: {}", dbUrl);

        initializeSchema();

        logger.info("Database repository initialized");
    }

    @Override
    public void save(QuantityMeasurementEntity entity) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDouble(1, entity.getThisValue());
            statement.setString(2, entity.getThisUnit());
            statement.setString(3, entity.getThisMeasurementType());

            if (entity.getThatUnit() == null) {
                statement.setNull(4, Types.DOUBLE);
                statement.setNull(5, Types.VARCHAR);
                statement.setNull(6, Types.VARCHAR);
            } else {
                statement.setDouble(4, entity.getThatValue());
                statement.setString(5, entity.getThatUnit());
                statement.setString(6, entity.getThatMeasurementType());
            }

            statement.setString(7, entity.getOperation());

            if (entity.getResultUnit() == null && entity.getResultString() == null) {
                statement.setNull(8, Types.DOUBLE);
                statement.setNull(9, Types.VARCHAR);
                statement.setNull(10, Types.VARCHAR);
            } else {
                statement.setDouble(8, entity.getResultValue());
                statement.setString(9, entity.getResultUnit());
                statement.setString(10, entity.getResultMeasurementType());
            }

            statement.setString(11, entity.getResultString());
            statement.setBoolean(12, entity.isError());
            statement.setString(13, entity.getErrorMessage());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save measurement", e);
        }
    }

    @Override
    public List<QuantityMeasurementEntity> getAllMeasurements() {
        return executeQuery(SELECT_ALL_SQL, null);
    }

    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByOperation(String operation) {
        return executeQuery(SELECT_BY_OPERATION_SQL,
                statement -> statement.setString(1, operation));
    }

    @Override
    public List<QuantityMeasurementEntity> getMeasurementsByMeasurementType(String measurementType) {
        return executeQuery(SELECT_BY_MEASUREMENT_TYPE_SQL,
                statement -> statement.setString(1, measurementType));
    }

    @Override
    public long getMeasurementCount() {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_SQL);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to count measurements", e);
        }
        return 0;
    }

    @Override
    public void deleteAllMeasurements() {
        try (Connection connection = connectionPool.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement deleteHistory =
                         connection.prepareStatement(DELETE_ALL_HISTORY_SQL);
                 PreparedStatement deleteAll =
                         connection.prepareStatement(DELETE_ALL_SQL)) {

                deleteHistory.executeUpdate();
                deleteAll.executeUpdate();

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete measurements", e);
        }
    }

    @Override
    public Map<String, Integer> getPoolStatistics() {
        return connectionPool.getPoolStatistics();
    }

    @Override
    public void releaseResources() {
        connectionPool.close();
    }

    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private List<QuantityMeasurementEntity> executeQuery(
            String sql,
            StatementBinder binder
    ) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (binder != null) binder.bind(statement);

            try (ResultSet rs = statement.executeQuery()) {

                List<QuantityMeasurementEntity> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(mapEntity(rs));
                }

                return results;
            }

        } catch (SQLException e) {
            throw new DatabaseException("Query failed", e);
        }
    }

    private QuantityMeasurementEntity mapEntity(ResultSet rs) throws SQLException {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        entity.setId(rs.getLong("id"));
        entity.setThisValue(rs.getDouble("this_value"));
        entity.setThisUnit(rs.getString("this_unit"));
        entity.setThisMeasurementType(rs.getString("this_measurement_type"));

        double thatValue = rs.getDouble("that_value");
        if (!rs.wasNull()) entity.setThatValue(thatValue);

        entity.setThatUnit(rs.getString("that_unit"));
        entity.setThatMeasurementType(rs.getString("that_measurement_type"));

        entity.setOperation(rs.getString("operation"));

        double resultValue = rs.getDouble("result_value");
        if (!rs.wasNull()) entity.setResultValue(resultValue);

        entity.setResultUnit(rs.getString("result_unit"));
        entity.setResultMeasurementType(rs.getString("result_measurement_type"));
        entity.setResultString(rs.getString("result_string"));

        entity.setError(rs.getBoolean("is_error"));
        entity.setErrorMessage(rs.getString("error_message"));

        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null)
            entity.setCreatedAt(createdAt.toLocalDateTime());
        else
            entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    private void initializeSchema() {

        String schemaFile = resolveSchemaFile();

        String schema = readSchemaScript(schemaFile);

        List<String> statements = splitStatements(schema);

        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : statements) {
                statement.execute(sql);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize schema", e);
        }
    }

    private String resolveSchemaFile() {

        String dbUrl = ApplicationConfig.getInstance().getDbUrl();

        if (dbUrl.startsWith("jdbc:postgresql:"))
            return "db/schema-postgresql.sql";

        if (dbUrl.startsWith("jdbc:mysql:"))
            return "db/schema.sql";

        if (dbUrl.startsWith("jdbc:h2:"))
            return "db/schema.sql";

        throw new DatabaseException("Unsupported database URL: " + dbUrl);
    }

    private String readSchemaScript(String path) {

        InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream(path);

        if (inputStream == null)
            throw new DatabaseException("Schema not found: " + path);

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (IOException e) {
            throw new DatabaseException("Failed reading schema", e);
        }
    }

    private List<String> splitStatements(String sqlContent) {

        String cleaned = sqlContent.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("--"))
                .collect(Collectors.joining("\n"));

        return Arrays.stream(cleaned.split(";"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}