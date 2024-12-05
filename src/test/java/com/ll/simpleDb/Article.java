package com.ll.simpleDb;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Article {
    private long id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String title;
    private String body;
    private boolean isBlind;
}
