package com.ll.simpleDb;

import java.time.LocalDateTime;

public class Article {

    private Long id;      // 게시물 고유 ID
    private String title; // 게시물 제목
    private String body;  // 게시물 내용
    private LocalDateTime createdDate;  // 게시물 생성 일자
    private LocalDateTime modifiedDate; // 게시물 수정 날짜
    private boolean isBlind; // 게시물 공개 여부

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isBlind() {
        return isBlind;
    }

    public void setBlind(boolean blind) {
        isBlind = blind;
    }
}