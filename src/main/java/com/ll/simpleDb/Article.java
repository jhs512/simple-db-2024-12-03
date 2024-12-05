package com.ll.simpleDb;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class Article {
    private long id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String title;
    private String body;
    private boolean isBlind;

    public Article(Map<String, Object> row) {
        this.id = (long) row.get("id");
        this.createdDate = (LocalDateTime) row.get("createdDate");
        this.modifiedDate = (LocalDateTime) row.get("modifiedDate");
        this.title = (String) row.get("title");
        this.body = (String) row.get("body");
        this.isBlind = (boolean) row.get("isBlind");
    }
}
