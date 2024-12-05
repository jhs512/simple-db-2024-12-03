package com.ll.simpleDb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder sqlFormat;

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
        this.sqlFormat = new StringBuilder();
    }

    public Sql append(String sqlBit, Object... params) {
        this.sqlFormat.append(" " + sqlBit);
        return this;
    }

    public long insert() {
        return 1;
    }

    public int update() {
        return 3;
    }

    public int delete() {
        return 2;
    }

    public List<Map<String, Object>> selectRows() {
        return simpleDb.selectRows(sqlFormat.toString().trim());
    }

    public Map<String, Object> selectRow() {
        return simpleDb.selectRow(sqlFormat.toString().trim());
    }

    public LocalDateTime selectDatetime() {
        return simpleDb.selectDatetime(sqlFormat.toString().trim());
    }

    public long selectLong() {
        return simpleDb.selectLong(sqlFormat.toString().trim());
    }

    public String selectString() {
        return simpleDb.selectString(sqlFormat.toString().trim());
    }

    public boolean selectBoolean() {
        return simpleDb.selectBoolean(sqlFormat.toString().trim());
    }
}
