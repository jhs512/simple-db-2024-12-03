package com.programmers.databases;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sql {
    private final Connection connection;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();
    private final SimpleDb simpleDb;

    public Sql(SimpleDb simpleDb, Connection connection) {
        this.simpleDb = simpleDb;
        this.connection = connection;
    }

    public Sql append(String query){
        stringBuilder.append(" ").append(query);
        return this;
    }

    public Sql append(String query, Object... args){
        stringBuilder.append(" ").append(query);
        parameters.addAll(Arrays.asList(args));
        return this;
    }

    public Sql appendIn(String query, Object... args){
        String in = Arrays.stream(args).map(Object::toString).collect(Collectors.joining(","));
        stringBuilder.append(" ").append(query.replace("?", in));
        return this;
    }

    private void returnConnection(){
        simpleDb.getConnectionPool().offer(connection);
    }

    public synchronized long insert(){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString(),
                PreparedStatement.RETURN_GENERATED_KEYS)){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {  // 커서가 유효한지 확인
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("No generated key obtained.");
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    private long modify(){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            return preparedStatement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public long update(){
       return modify();
    }

    public long delete(){
        return modify();
    }

    private void getData(Map<String, Object> row, ResultSet resultSet) throws SQLException {
        row.put("id", resultSet.getLong(1));
        row.put("createdDate", resultSet.getTimestamp(2).toLocalDateTime());
        row.put("modifiedDate", resultSet.getTimestamp(3).toLocalDateTime());
        row.put("title", resultSet.getString(4));
        row.put("body", resultSet.getString(5));
        row.put("isBlind", resultSet.getBoolean(6));
    }

    public List<Map<String, Object>> selectRows(){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> rows = new ArrayList<>();

            while(resultSet.next()){
                Map<String, Object> row = new HashMap<>();
                getData(row, resultSet);
                rows.add(row);
            }
            return rows;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public <T> List<T> selectRows(Class<T> type){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> list = new ArrayList<>();

            while(resultSet.next()){
                T instance = type.getDeclaredConstructor().newInstance();

                for (Field field : type.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = resultSet.getObject(field.getName());
                    field.set(instance, value);
                    field.setAccessible(false);
                }

                list.add(instance);  // 리스트
            }
            return list;
        }catch (SQLException | ReflectiveOperationException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public synchronized  <T> T selectRow(Class<T> type){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                T instance = type.getDeclaredConstructor().newInstance();

                for (Field field : type.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = resultSet.getObject(field.getName());
                    field.set(instance, value);
                    field.setAccessible(false);
                }
                return instance;
            }
            return null;
        }catch (SQLException | ReflectiveOperationException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public Map<String, Object> selectRow(){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> row = new HashMap<>();

            if(resultSet.next()){
               getData(row, resultSet);
            }
            return row;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }



    private <T> T select(Class<T> type){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())) {
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                Object result = resultSet.getObject(1);
                if(type == Boolean.class && result instanceof Long){
                    return type.cast(result.equals(1L));
                }
                return type.cast(resultSet.getObject(1));
            }else {
                return null;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public LocalDateTime selectDatetime(){
        return select(LocalDateTime.class);
    }

    public Long selectLong(){
        return select(Long.class);
    }

    public List<Long> selectLongs(){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())){
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Long> rows = new ArrayList<>();
            while(resultSet.next()){
                rows.add(resultSet.getLong(1));
            }
            return rows;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            returnConnection();
        }
    }

    public String selectString(){
        return select(String.class);
    }

    public Boolean selectBoolean(){
        return select(Boolean.class);
    }

}
