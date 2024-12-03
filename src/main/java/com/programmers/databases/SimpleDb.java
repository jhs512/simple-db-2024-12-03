package com.programmers.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleDb {
    private final Connection connection;

    public SimpleDb(String localhost, String id, String password, String database) {
        try{
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:3306/%s", localhost, database), id, password);
        }catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setDevMode(boolean devMode) {

    }

    public Sql genSql(){
        return new Sql(connection);
    }

    public void run(Object... sql){
        String query = String.valueOf(sql[0]);
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            if(sql.length > 1){
                for (int i = 1; i < sql.length; i++) {
                    preparedStatement.setObject(i, sql[i]);
                }
            }
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
