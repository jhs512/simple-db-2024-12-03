package com.ll.simpleDb;

public class Sql {
    public Sql append(String sqlBit, Object... params) {
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
}
