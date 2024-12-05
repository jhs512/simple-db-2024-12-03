package com.ll.simpleDb;

import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SimpleDb {
    private final String host;
    private final String username;
    private final String password;
    private final String dbName;
    private Connection connection;

    // 데이터베이스 연결 초기화
    private void connect() {
        if (connection != null) {
            return; // 이미 연결되어 있으면 아무 작업도 하지 않음
        }

        String url = String.format("jdbc:mysql://%s/%s?useSSL=false", host, dbName);
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // 자원 해제
    public void close() {
        if (connection == null) {
            return; // 연결이 없는 경우 아무 작업도 하지 않음
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection: " + e.getMessage(), e);
        }
    }

    public Sql genSql() {
        return new Sql(this);
    }

    // PreparedStatement 파라미터 바인딩 분리
    private void bindParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    // SQL 실행 메서드
    private Map<String, Object> parseResultSetToMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Object> row = new LinkedHashMap<>();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value;

            switch (metaData.getColumnType(i)) {
                case Types.BIGINT:
                    value = resultSet.getLong(columnName);
                    break;
                case Types.TIMESTAMP:
                    Timestamp timestamp = resultSet.getTimestamp(columnName);
                    value = (timestamp != null) ? timestamp.toLocalDateTime() : null;
                    break;
                case Types.BOOLEAN:
                    value = resultSet.getBoolean(columnName);
                    break;
                default:
                    value = resultSet.getObject(columnName);
                    break;
            }

            row.put(columnName, value);
        }

        return row;
    }

    private <T> T parseResultSet(ResultSet resultSet, Class cls) throws SQLException {
        if (cls == String.class) {
            resultSet.next();
            return (T) resultSet.getString(1);
        } else if (cls == List.class) {
            List<Map<String, Object>> rows = new ArrayList<>();

            while (resultSet.next()) {
                rows.add(parseResultSetToMap(resultSet));
            }

            return (T) rows;
        } else if (cls == Map.class) {
            resultSet.next();

            return (T) parseResultSetToMap(resultSet);
        } else if (cls == LocalDateTime.class) {
            resultSet.next();

            return (T) resultSet.getTimestamp(1).toLocalDateTime();
        } else if (cls == Long.class) {
            resultSet.next();

            return (T) (Long) resultSet.getLong(1);
        } else if (cls == Boolean.class) {
            resultSet.next();

            return (T) (Boolean) resultSet.getBoolean(1);
        }

        throw new IllegalArgumentException("Unsupported class type: " + cls);
    }

    private <T> T _run(String sql, Class cls, Object... params) {
        connect();

        if (isNotProdMode()) {
            System.out.println("== rawSql ==");
            System.out.println(sql);
        }

        if (sql.startsWith("INSERT")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                bindParameters(preparedStatement, params);

                int affectedRows = preparedStatement.executeUpdate();

                if (cls == Integer.class) {
                    return (T) (Integer) affectedRows;
                }

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return (T) (Long) generatedKeys.getLong(1);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to execute SQL: " + sql + ". Error: " + e.getMessage(), e);
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);

            if (sql.startsWith("SELECT")) {
                ResultSet resultSet = preparedStatement.executeQuery();

                return parseResultSet(resultSet, cls);
            }

            return (T) (Integer) preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql + ". Error: " + e.getMessage(), e);
        }
    }

    private boolean isNotProdMode() {
        return true;
    }

    public int run(String sql, Object... params) {
        return _run(sql, Integer.class, params);
    }

    public boolean selectBoolean(String sql, Object... params) {
        return _run(sql, Boolean.class, params);
    }

    public String selectString(String sql, Object... params) {
        return _run(sql, String.class, params);
    }

    public long selectLong(String sql, Object... params) {
        return _run(sql, Long.class, params);
    }

    public LocalDateTime selectDatetime(String sql, Object... params) {
        return _run(sql, LocalDateTime.class, params);
    }

    public Map<String, Object> selectRow(String sql, Object... params) {
        return _run(sql, Map.class, params);
    }

    public List<Map<String, Object>> selectRows(String sql, Object... params) {
        return _run(sql, List.class, params);
    }

    public int delete(String sql, Object... params) {
        return _run(sql, Integer.class, params);
    }

    public int update(String sql, Object[] params) {
        return _run(sql, Integer.class, params);
    }

    public long insert(String sql, Object[] params) {
        return _run(sql, Long.class, params);
    }
}