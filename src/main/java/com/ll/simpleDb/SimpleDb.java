package com.ll.simpleDb;
import java.sql.*;

public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private boolean devMode = false;

    public SimpleDb(String host, String user, String password, String database) {
        this.url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
        this.user = user;
        this.password = password;
    }

    // 각 쓰레드마다 독립적으로 연결을 생성
    private Connection connect() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // 연결을 확인하고 필요 시 생성
    private void ensureConnection() {
        try {
            if (connectionHolder.get() == null || connectionHolder.get().isClosed()) {
                connectionHolder.set(connect());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure connection: " + e.getMessage(), e);
        }
    }

    // devMode 설정 메서드 추가
    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    // SQL 객체 생성
    public Sql genSql() {
        ensureConnection();
        return new Sql(connectionHolder.get(), devMode); // devMode 전달
    }

    // 쿼리 실행
    public void run(String query, Object... params) {
        ensureConnection();
        try (PreparedStatement stmt = connectionHolder.get().prepareStatement(query)) {
            setParams(stmt, params);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL Execution Failed: " + e.getMessage(), e);
        }
    }

    // 트랜잭션 시작
    public void startTransaction() {
        ensureConnection();
        try {
            connectionHolder.get().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start transaction: " + e.getMessage(), e);
        }
    }

    // 트랜잭션 커밋
    public void commit() {
        ensureConnection();
        try {
            connectionHolder.get().commit();
            connectionHolder.get().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction: " + e.getMessage(), e);
        }
    }

    // 트랜잭션 롤백
    public void rollback() {
        ensureConnection();
        try {
            connectionHolder.get().rollback();
            connectionHolder.get().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction: " + e.getMessage(), e);
        }
    }

    // 연결 종료
    public void closeConnection() {
        try {
            Connection connection = connectionHolder.get();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connectionHolder.remove();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection: " + e.getMessage(), e);
        }
    }

    // PreparedStatement 파라미터 설정
    private void setParams(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}