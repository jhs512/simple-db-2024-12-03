package com.ll.simpleDb;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class SimpleDb {
    private final String host;
    private final String username;
    private final String password;
    private final String dbName;
    private Connection connection;

    // 데이터베이스 연결 초기화
    private void connect() {
        if (connection == null) {
            String url = String.format("jdbc:mysql://%s/%s?useSSL=false", host, dbName);
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database", e);
            }
        }
    }

    // SQL 실행 메서드
    public void run(String sql) {
        connect(); // 연결 초기화
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql, e);
        }
    }

    // 자원 해제
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close database connection", e);
            }
        }
    }
}
