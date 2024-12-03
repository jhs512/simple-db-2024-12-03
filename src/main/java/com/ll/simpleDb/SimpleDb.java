package com.ll.simpleDb;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleDb {
    private final String host;
    private final String username;
    private final String password;
    private final String dbName;
}
