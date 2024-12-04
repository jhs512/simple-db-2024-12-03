package com.ll.simpleDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  `Sql` 클래스는 데이터베이스와의 상호작용을 간소화하기 위해 설계되었습니다.

  주요 기능:
  - 동적 SQL 쿼리 작성
  - INSERT, UPDATE, DELETE, SELECT 등 기본 SQL 작업 지원
  - 결과 매핑을 통한 객체화 지원
  - 프로시저 호출 추가
 */

public class Sql {
    private final Connection connection;
    private final boolean devMode;
    private final StringBuilder queryBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();


    /**

     `Sql` 객체를 생성합니다.
     // @param connection 데이터베이스와의 연결 객체
     // @param devMode 개발 모드 여부 (디버깅용)

     */
    public Sql(Connection connection, boolean devMode) {
        this.connection = connection;
        this.devMode = devMode;
    }

    /**
      SQL 쿼리를 동적으로 추가합니다.

      @param sql 추가할 SQL 쿼리 문자열
      @param params 쿼리에 바인딩할 매개변수
      @return 현재 `Sql` 객체
     */

    public Sql append(String sql, Object... params) {
        queryBuilder.append(sql).append(" ");
        for (Object param : params) {
            this.params.add(param);
        }
        return this;
    }

    /**
      SQL의 IN 절에 사용할 플레이스홀더를 동적으로 생성하고 추가합니다.

      @param sql SQL 쿼리 문자열
      @param inParams IN 절에 전달할 매개변수
      @return 현재 `Sql` 객체 (체이닝 가능)
     */

    public Sql appendIn(String sql, Object... inParams) {
        String inClause = String.join(", ", "?".repeat(inParams.length).split(""));
        append(sql.replace("?", inClause), inParams);
        return this;
    }

    /**
      작성된 INSERT 쿼리를 실행하고, 생성된 키를 반환합니다.

      @return 생성된 AUTO_INCREMENT 키 값
      @throws RuntimeException INSERT 실패 시
     */

    public long insert() {
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {
            setParams(stmt);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }


    /**
      작성된 UPDATE 쿼리를 실행하고, 영향을 받은 행 수를 반환합니다.

      @return 업데이트된 행의 개수
      @throws RuntimeException UPDATE 실패 시
     */

    public long update() {
        return executeUpdate();
    }

    /**
      작성된 DELETE 쿼리를 실행하고, 영향을 받은 행 수를 반환합니다.

      @return 삭제된 행의 개수
      @throws RuntimeException DELETE 실패 시
     */

    public long delete() {
        return executeUpdate();
    }


    /**
      작성된 SELECT 쿼리를 실행하고, 단일 행을 반환합니다.

      @return 단일 행 데이터 (컬럼 이름과 값의 Map)
     */
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
      작성된 SELECT 쿼리를 실행하고, 단일 행을 지정된 클래스 타입의 객체로 반환합니다.

      @param clazz 매핑할 클래스 타입
      @param <T> 매핑된 객체의 타입
      @return 매핑된 객체
     */

    public <T> T selectRow(Class<T> clazz) {
        return selectRows(clazz).stream().findFirst().orElse(null);
    }

    /**
      작성된 SELECT 쿼리를 실행하고, 여러 행을 지정된 클래스 타입의 객체 리스트로 반환합니다.

    // @param clazz 매핑할 클래스 타입
    // @param <T> 매핑된 객체의 타입
      @return 매핑된 객체 리스트
     */

    public List<Map<String, Object>> selectRows() {
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            setParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Select failed: " + e.getMessage(), e);
        }
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        List<Map<String, Object>> rows = selectRows();
        List<T> results = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            results.add(Mapper.map(row, clazz));
        }
        return results;
    }


    /**
      단일 열의 값을 Long 타입으로 반환합니다.

      @return Long 값
     */
    public Long selectLong() {
        return (Long) selectRow().values().stream().findFirst().orElse(null);
    }

    /**
      단일 열의 값을 String 타입으로 반환합니다.

      @return String 값
     */
    public String selectString() {
        return (String) selectRow().values().stream().findFirst().orElse(null);
    }

    /**
      단일 열의 값을 Boolean 타입으로 반환합니다.

      @return Boolean 값
     */

    public Boolean selectBoolean() {
        Object value = selectRow().values().stream().findFirst().orElse(null);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            // SQL에서 1은 true, 0은 false로 변환
            return ((Number) value).intValue() != 0;
        }

        throw new IllegalArgumentException("Cannot convert value to Boolean: " + value);
    }

    private long executeUpdate() {
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            setParams(stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update/Delete failed: " + e.getMessage(), e);
        }
    }




    /**
      단일 열의 값을 LocalDateTime 타입으로 반환합니다.

      @return LocalDateTime 값
     */
    public LocalDateTime selectDatetime() {
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            setParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp(1).toLocalDateTime();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select datetime: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Long> selectLongs() {
        List<Long> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            setParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to select longs: " + e.getMessage(), e);
        }
        return results;
    }


    /**
      데이터베이스 프로시저를 호출합니다.

      @param procedureName 호출할 프로시저 이름
      @param params 프로시저에 전달할 매개변수
      @throws RuntimeException 호출 실패 시
     */
    // 프로시저 호출 메서드 추가
    public void callProcedure(String procedureName, Object... params) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL " + procedureName + "(" + getPlaceholders(params.length) + ")}")) {
            // IN 매개변수 설정
            for (int i = 0; i < params.length; i++) {
                callableStatement.setObject(i + 1, params[i]);
            }

            // 프로시저 실행
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to call procedure '" + procedureName + "': " + e.getMessage(), e);
        }
    }


    /* 헬퍼 메서드  */


    // Helper 메서드: 매개변수 수에 맞는 플레이스홀더 생성
    private String getPlaceholders(int count) {
        return String.join(",", "?".repeat(count).split(""));
    }

    /**
     PreparedStatement에 매개변수를 설정합니다.

     @param stmt 매개변수를 설정할 PreparedStatement
     @throws SQLException 매개변수 설정 실패 시
     */
    private void setParams(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    /**
     ResultSet 데이터를 Map 리스트로 변환합니다.
     @param rs 변환할 ResultSet
     @return 변환된 Map 리스트
     @throws SQLException 변환 실패 시

     기능:
     ResultSet 데이터를 List<Map<String, Object>> 형태로 변환합니다.

     필요한 이유:

     데이터베이스 쿼리 결과를 코드에서 다루기 쉽게 변환해야 합니다.
     반복적으로 ResultSet에서 데이터를 읽고, 각 컬럼의 값을 가져오는 작업은 번거롭고 실수할 가능성이 큽니다.
     효율성:
     헬퍼 메서드를 사용하면:

     컬럼 수나 ResultSet 구조에 상관없이 동일한 방식으로 데이터를 처리.
     SELECT 쿼리 결과를 재사용 가능하게 만듦.

     */
    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }

        return rows;
    }




}