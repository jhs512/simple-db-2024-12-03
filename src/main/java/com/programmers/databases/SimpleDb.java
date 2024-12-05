package com.programmers.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleDb {
    private final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();

    public SimpleDb(String localhost, String id, String password, String database) {
        int POOL_SIZE = 100;
        try {
            for(int i = 0; i < POOL_SIZE; i++){
                connectionPool.add(DriverManager.getConnection(String.format("jdbc:mysql://%s:3306/%s", localhost, database), id, password));
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void setDevMode(boolean devMode) {

    }

    public Queue<Connection> getConnectionPool() {
        return connectionPool;
    }

    public synchronized Sql genSql(){
        int chance = 0;
        while(connectionPool.isEmpty()){
            try {
                chance++;
                if(chance > 10){
                    throw new RuntimeException("Connection Time out");
                }
                Thread.sleep(500);
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }
        return new Sql(this, connectionPool.poll());
    }


    public void run(Object... sql){
        Connection connection = connectionPool.poll();
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
        }finally {
            connectionPool.add(connection);
        }
    }

    public void closeConnection(){
        connectionPool
                .forEach(c ->{
                    try {
                        if(!c.isClosed()){
                            c.close();
                        }
                    }catch (SQLException e){
                        connectionPool.remove(c);
                        throw new RuntimeException(e);
                    }
                });
    }
}
