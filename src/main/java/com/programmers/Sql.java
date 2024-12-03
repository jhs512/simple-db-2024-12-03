package com.programmers;

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

public class Sql {
    private final Connection connection;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();

    public Sql(Connection connection) {
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

    public long insert(){
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
        }
    }

    private <T> T select(Class<T> type){
        try(PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return type.cast(resultSet.getObject(1));
            }else {
                return null;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public LocalDateTime selectDatetime(){
        return select(LocalDateTime.class);
    }

    public Long selectLong(){
        return select(Long.class);
    }

    public String selectString(){
        return select(String.class);
    }

    public Boolean selectBoolean(){
        return select(Boolean.class);
    }
}
