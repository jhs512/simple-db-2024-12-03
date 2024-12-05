package com.ll.simpleDb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder sqlFormat;
    private final List<Object> params;

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
        this.sqlFormat = new StringBuilder();
        this.params = new ArrayList<>();
    }

    public Sql append(String sqlBit, Object... params) {
        this.sqlFormat.append(" " + sqlBit);

        for (Object param : params) {
            this.params.add(param);
        }

        return this;
    }

    private String toSql() {
        return sqlFormat.toString().trim();
    }

    public long insert() {
        return 1;
    }

    public int update() {
        return 3;
    }

    public int delete() {
        return simpleDb.delete(toSql(), params.toArray());
    }

    public List<Map<String, Object>> selectRows() {
        return simpleDb.selectRows(toSql(), params.toArray());
    }

    public Map<String, Object> selectRow() {
        return simpleDb.selectRow(toSql(), params.toArray());
    }

    public LocalDateTime selectDatetime() {
        return simpleDb.selectDatetime(toSql(), params.toArray());
    }

    public long selectLong() {
        return simpleDb.selectLong(toSql(), params.toArray());
    }

    public String selectString() {
        return simpleDb.selectString(toSql(), params.toArray());
    }

    public boolean selectBoolean() {
        return simpleDb.selectBoolean(toSql(), params.toArray());
    }
}
